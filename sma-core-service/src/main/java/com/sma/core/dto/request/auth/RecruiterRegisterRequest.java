package com.sma.core.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class RecruiterRegisterRequest {
    // Recruiter info
    @NotBlank
    @Email
    private String recruiterEmail;
    @NotBlank
    @Size(min = 6, max = 100,message = "Password must be between 6 and 100 characters")
    private String password;

    // Company info
    @NotBlank
    private String companyName;
    @NotBlank
    private String description;
    @NotBlank
    private String companyIndustry;
    @NotBlank
    private String size;
    @NotBlank
    @Email
    private String companyEmail;
    @NotBlank
    private String phone;
    @NotBlank
    private String address;
    private String country;
    @NotBlank
    private String taxIdentificationNumber;
    @NotBlank
    private String erc;
    private String companyLink;
}
