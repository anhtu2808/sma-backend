package com.sma.core.service.impl;

import com.sma.core.dto.request.invitation.CreateInvitationRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.invitation.InvitationResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.InvitationStatus;
import com.sma.core.enums.NotificationType;
import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.InvitationMapper;
import com.sma.core.repository.*;
import com.sma.core.service.EmailService;
import com.sma.core.service.InvitationService;
import com.sma.core.service.NotificationService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    InvitationRepository invitationRepository;
    InvitationMapper invitationMapper;
    CandidateRepository candidateRepository;
    JobRepository jobRepository;
    RecruiterRepository recruiterRepository;
    NotificationService notificationService;
    EmailService emailService;

    @Override
    public PagingResponse<InvitationResponse> getMyInvitations(Integer candidateId, Integer size, Integer page) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Invitation> invitations = invitationRepository.findByCandidateId(candidateId, pageable);
        List<InvitationResponse> invitationResponses = invitations.getContent()
                .stream()
                .map(invitationMapper::toInvitationResponse)
                .toList();
        return PagingResponse.fromPage(invitations, invitationResponses);
    }

    @Override
    public PagingResponse<InvitationResponse> getMyCompanyInvitations(Integer recruiterId, Integer size, Integer page) {
        Company company = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED)).getCompany();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Invitation> invitations = invitationRepository.findByCompanyId(company.getId(), pageable);
        List<InvitationResponse> invitationResponses = invitations.getContent()
                .stream()
                .map(invitationMapper::toInvitationResponse)
                .toList();
        return PagingResponse.fromPage(invitations, invitationResponses);
    }

    @Override
    public InvitationResponse createInvitation(CreateInvitationRequest request) {
        Recruiter recruiter = recruiterRepository.findById(JwtTokenProvider.getCurrentRecruiterId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        if (recruiter.getCompany() == null) {
            throw new AppException(ErrorCode.RECRUITER_NOT_HAVE_COMPANY);
        }
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        String content = "You have received invitation from company "
                + recruiter.getCompany().getName()
                + " for position "
                + job.getName()
                + ". Click here for apply: ";
        Invitation invitation = Invitation.builder()
                .content(content)
                .candidate(candidate)
                .company(recruiter.getCompany())
                .job(job)
                .status(InvitationStatus.INVITED)
                .build();

        Invitation savedInvitation = invitationRepository.save(invitation);

        if (candidate.getUser() != null) {
            String notiTitle = "New Job Invitation!";
            String notiMessage = recruiter.getCompany().getName() + " has invited you to apply for: " + job.getName();

            notificationService.sendCandidateNotification(
                    candidate.getUser(),
                    NotificationType.INVITATION,
                    notiTitle,
                    notiMessage,
                    "INVITATION",
                    savedInvitation.getId()
            );
        }
        sendInvitationEmail(candidate, recruiter.getCompany(), job);
        return invitationMapper.toInvitationResponse(invitation);
    }

    @Override
    public InvitationResponse getInvitationById(Integer invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));
        Role role = JwtTokenProvider.getCurrentRole();
        if (role != null){
            if (role.equals(Role.CANDIDATE)){
                if (JwtTokenProvider.getCurrentCandidateId().equals(invitation.getCandidate().getId()))
                    throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
            }
            else if (role.equals(Role.RECRUITER)) {
                if (recruiterRepository.findById(JwtTokenProvider.getCurrentRecruiterId())
                        .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED))
                        .getCompany().getId().equals(invitation.getCompany().getId()))
                    throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
            }
        }
        return invitationMapper.toInvitationResponse(invitation);
    }
    private void sendInvitationEmail(Candidate candidate, Company company, Job job) {
        if (candidate.getUser() != null && candidate.getUser().getEmail() != null) {
            try {
                org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();

                context.setVariable("companyName", company.getName());
                context.setVariable("jobTitle", job.getName());
                context.setVariable("jobId", job.getId());

                String locationStr = "See details in job post";
                if (job.getLocations() != null && !job.getLocations().isEmpty()) {
                    locationStr = job.getLocations().stream()
                            .map(loc -> loc.getCity())
                            .collect(java.util.stream.Collectors.joining(", "));
                }
                context.setVariable("location", locationStr);

                String subject = "[SmartRecruit] You've Been Invited to Apply at " + company.getName();

                emailService.sendEmailWithTemplate(
                        candidate.getUser().getEmail(),
                        subject,
                        "job-invite",
                        context
                );
                log.info("Invitation email sent successfully to: {}", candidate.getUser().getEmail());
            } catch (Exception e) {
                log.error("Failed to send invitation email to candidate: {}", candidate.getUser().getEmail(), e);
            }
        }
    }
}
