package com.sma.core.service.impl;

import com.sma.core.dto.request.auth.RecruiterRegisterRequest;
import com.sma.core.dto.request.user.CreateRecruiterMemberRequest;
import com.sma.core.dto.request.user.UpdateRecruiterMemberRequest;
import com.sma.core.dto.request.user.UpdateRecruiterMemberStatusRequest;
import com.sma.core.dto.response.myinfo.RecruiterMyInfoResponse;
import com.sma.core.dto.response.recruiter.RecruiterMemberResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.CompanyStatus;
import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.NotificationType;
import com.sma.core.enums.Role;
import com.sma.core.enums.UserStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.recruiter.RecruiterMapper;
import com.sma.core.repository.CompanyLocationRepository;
import com.sma.core.repository.CompanyRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.repository.UserRepository;
import com.sma.core.service.EmailService;
import com.sma.core.service.NotificationService;
import com.sma.core.service.QuotaService;
import com.sma.core.service.RecruiterService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class RecruiterServiceImpl implements RecruiterService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RecruiterRepository recruiterRepository;
    private final CompanyLocationRepository companyLocationRepository;
    private final PasswordEncoder passwordEncoder;
    private final RecruiterMapper recruiterMapper;
    private final NotificationService notificationService;
    private final QuotaService quotaService;
    final EmailService emailService;

    @Override
    public void registerRecruiter(RecruiterRegisterRequest request) {

        if (userRepository.findByEmail(request.getRecruiterEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }

        if (companyRepository.existsByTaxIdentificationNumber(
                request.getTaxIdentificationNumber())) {
            throw new AppException(ErrorCode.COMPANY_ALREADY_REGISTERED);
        }

        User user = User.builder()
                .email(request.getRecruiterEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.PENDING_VERIFICATION)
                .role(Role.RECRUITER)
                .build();
        userRepository.save(user);

        Company company = Company.builder()
                .name(request.getCompanyName())
                .description(request.getDescription())
                .companyIndustry(request.getCompanyIndustry())
                .minSize(request.getMinSize())
                .maxSize(request.getMaxSize())
                .email(request.getCompanyEmail())
                .phone(request.getPhone())
                .country(request.getCountry())
                .taxIdentificationNumber(request.getTaxIdentificationNumber())
                .erc(request.getErc())
                .link(request.getCompanyLink())
                .status(CompanyStatus.PENDING_VERIFICATION)
                .build();
        companyRepository.save(company);

        CompanyLocation location = CompanyLocation.builder()
                .company(company)
                .address(request.getAddress())
                .country(request.getCountry())
                .build();
        companyLocationRepository.save(location);

        Recruiter recruiter = Recruiter.builder()
                .user(user)
                .company(company)
                .isVerified(false)
                .isRootRecruiter(true)
                .build();
        recruiterRepository.save(recruiter);
        sendRegistrationEmail(user, company, CompanyStatus.PENDING_VERIFICATION, null);
        sendAdminNotificationEmail(company, user);
        Notification noti = Notification.builder()
                .notificationType(NotificationType.COMPANY_REGISTRATION)
                .title("New Company Registration")
                .message("A new company '" + company.getName() + "' has registered and is awaiting verification.")
                .relatedEntityType("COMPANY")
                .relatedEntityId(company.getId())
                .isRead(false)
                .build();
        notificationService.sendAdminNotification(noti);
    }

    @Override
    @Transactional(readOnly = true)
    public RecruiterMyInfoResponse getMyInfo() {
        Integer recruiterId = JwtTokenProvider.getCurrentRecruiterId();
        Recruiter recruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));

        return recruiterMapper.toRecruiterMyInfoResponse(recruiter);
    }

    @Override
    public RecruiterMyInfoResponse createMember(CreateRecruiterMemberRequest request) {
        Recruiter rootRecruiter = recruiterRepository
                .findById(JwtTokenProvider.getCurrentRecruiterId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        if (!Boolean.TRUE.equals(rootRecruiter.getIsRootRecruiter())) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTS);
        }
        quotaService.validateStateQuota(FeatureKey.TEAM_MEMBER_LIMIT, null);
        User user = User.builder()
                .fullName(request.getFullName())
                .gender(request.getGender())
                .email(request.getEmail())
                .role(Role.RECRUITER)
                .status(UserStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);
        Recruiter recruiterMember = Recruiter.builder()
                .company(rootRecruiter.getCompany())
                .isRootRecruiter(false)
                .isVerified(true)
                .verifiedAt(LocalDateTime.now())
                .user(user)
                .build();
        recruiterRepository.save(recruiterMember);
        return recruiterMapper.toRecruiterMyInfoResponse(recruiterMember);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecruiterMemberResponse> getMembers() {
        Integer recruiterId = JwtTokenProvider.getCurrentRecruiterId();
        Recruiter recruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        Integer companyId = recruiter.getCompany().getId();

        return recruiterRepository.findAllByCompanyId(companyId)
                .stream()
                .sorted(Comparator
                        .comparing((Recruiter r) -> Boolean.TRUE.equals(r.getIsRootRecruiter())).reversed()
                        .thenComparing(r -> r.getUser() != null ? r.getUser().getFullName() : null, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(Recruiter::getId))
                .map(recruiterMapper::toRecruiterMemberResponse)
                .toList();
    }

    @Override
    public RecruiterMemberResponse updateMember(Integer recruiterId, UpdateRecruiterMemberRequest request) {
        Recruiter rootRecruiter = recruiterRepository
                .findById(JwtTokenProvider.getCurrentRecruiterId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        if (!Boolean.TRUE.equals(rootRecruiter.getIsRootRecruiter())) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }

        Recruiter targetRecruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        if (!targetRecruiter.getCompany().getId().equals(rootRecruiter.getCompany().getId())) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }

        User user = targetRecruiter.getUser();
        user.setFullName(request.getFullName());
        user.setGender(request.getGender());

        return recruiterMapper.toRecruiterMemberResponse(targetRecruiter);
    }

    @Override
    public RecruiterMemberResponse updateMemberStatus(Integer recruiterId, UpdateRecruiterMemberStatusRequest request) {
        Recruiter rootRecruiter = recruiterRepository
                .findById(JwtTokenProvider.getCurrentRecruiterId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        if (!Boolean.TRUE.equals(rootRecruiter.getIsRootRecruiter())) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }

        if (rootRecruiter.getId().equals(recruiterId)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Root recruiter cannot change their own status");
        }

        Recruiter targetRecruiter = recruiterRepository.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        if (!targetRecruiter.getCompany().getId().equals(rootRecruiter.getCompany().getId())) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }

        UserStatus nextStatus = request.getStatus();
        if (nextStatus != UserStatus.ACTIVE && nextStatus != UserStatus.INACTIVE) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Status must be ACTIVE or INACTIVE");
        }

        if (nextStatus == UserStatus.ACTIVE
                && targetRecruiter.getUser().getStatus() != UserStatus.ACTIVE) {
            quotaService.validateStateQuota(FeatureKey.TEAM_MEMBER_LIMIT, null);
        }

        targetRecruiter.getUser().setStatus(nextStatus);

        return recruiterMapper.toRecruiterMemberResponse(targetRecruiter);
    }

    private void sendRegistrationEmail(User user, Company company, CompanyStatus status, String reason) {
        Context context = new Context();
        String displayName = (user.getFullName() != null && !user.getFullName().isEmpty())
                ? user.getFullName()
                : user.getEmail().split("@")[0];

        context.setVariable("recruiterName", displayName);
        context.setVariable("companyName", company.getName());
        context.setVariable("status", status.name());
        context.setVariable("rejectReason", reason);

        String subject = "[SmartRecruit] Registration Received - " + company.getName();

        emailService.sendEmailWithTemplate(
                user.getEmail(),
                subject,
                "company-registration-result",
                context
        );
    }

    private void sendAdminNotificationEmail(Company company, User recruiterUser) {
        List<User> adminUsers = userRepository.findAllByRole(Role.ADMIN);
        if (adminUsers.isEmpty()) {
            log.warn("No Admin found in system to send registration notification.");
            return;
        }

        Context context = new Context();
        context.setVariable("companyName", company.getName());
        context.setVariable("industry", company.getCompanyIndustry());
        context.setVariable("companyEmail", company.getEmail());
        context.setVariable("taxId", company.getTaxIdentificationNumber());
        context.setVariable("recruiterEmail", recruiterUser.getEmail());
        context.setVariable("companyId", company.getId());

        String formattedTime = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        );
        context.setVariable("registerTime", formattedTime);

        for (User admin : adminUsers) {
            try {
                emailService.sendEmailWithTemplate(
                        admin.getEmail(),
                        "[URGENT] New Company Registration: " + company.getName(),
                        "company-register",
                        context
                );
            } catch (Exception e) {
                log.error("Failed to send admin notification email to: {}", admin.getEmail(), e);
            }
        }
    }
}
