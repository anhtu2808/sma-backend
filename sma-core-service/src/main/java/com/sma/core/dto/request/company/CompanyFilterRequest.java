package com.sma.core.dto.request.company;

import com.sma.core.enums.CompanyIndustry;
import com.sma.core.enums.CompanyStatus;
import com.sma.core.enums.CompanyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.EnumSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CompanyFilterRequest {

    String name;
    Set<String> location;
    CompanyType type;
    CompanyIndustry industry;

    @Schema(
            name = "Chỉ có admin, recruiter mới có quyền lọc theo status"
    )
    EnumSet<CompanyStatus> status;

    @Builder.Default
    Integer page = 0;

    @Builder.Default
    Integer size = 10;

}
