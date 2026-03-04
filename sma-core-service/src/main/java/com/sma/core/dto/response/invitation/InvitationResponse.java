package com.sma.core.dto.response.invitation;

import com.sma.core.dto.response.candidate.CandidateInvitationResponse;
import com.sma.core.dto.response.company.CompanyInvitationResponse;
import com.sma.core.dto.response.job.JobInvitationResponse;
import com.sma.core.enums.InvitationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvitationResponse {

    Integer id;
    String content;
    CompanyInvitationResponse company;
    CandidateInvitationResponse candidate;
    JobInvitationResponse job;
    InvitationStatus status;

}
