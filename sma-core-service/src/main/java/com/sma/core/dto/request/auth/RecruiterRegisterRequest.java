package com.sma.core.dto.request.auth;

import com.sma.core.enums.CompanyIndustry;
import com.sma.core.enums.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "Password is not blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    // Company info
    @NotBlank(message = "Company name is not blank")
    @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
    private String companyName;
    @NotBlank
    private String description;
    @Schema(
            description = """
                    Industry gồm:
                    - INFORMATION_TECHNOLOGY
                    - FINTECH
                    - ECOMMERCE
                    - HEALTHCARE
                    - EDUCATION
                    - LOGISTICS
                    - MANUFACTURING
                    - REAL_ESTATE
                    - GAMING
                    - TELECOMMUNICATION
                    - ARTIFICIAL_INTELLIGENCE
                    - CYBER_SECURITY
                    - BLOCKCHAIN
                    - IOT
                    """
    )
    @NotBlank
    private CompanyIndustry companyIndustry;
    @NotBlank
    @Schema(
            description = """
                    Type gồm:
                    PRODUCT,
                    OUTSOURCING,
                    CONSULTING,
                    SERVICE,
                    SOLUTION,
                    SYSTEM_INTEGRATOR,
                    AGENCY
                    """
    )
    private CompanyType companyType;
    @NotBlank
    private Integer minSize;
    @NotBlank
    private Integer maxSize;
    @NotBlank
    @Email
    private String companyEmail;
    @NotBlank(message = "Phone is not blank")
    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Invalid phone number"
    )
    private String phone;
    @NotBlank
    private String address;
    private String district;
    private String city;
    private String country;
    @NotBlank
    private String taxIdentificationNumber;
    @NotBlank
    private String erc;
    private String companyLink;
}
