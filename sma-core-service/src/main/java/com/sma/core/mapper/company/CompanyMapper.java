package com.sma.core.mapper.company;

import com.sma.core.dto.request.company.UpdateCompanyRequest;
import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyDetailResponse;
import com.sma.core.dto.response.company.*;
import com.sma.core.dto.response.recruiter.RecruiterShortResponse;
import com.sma.core.entity.Company;
import com.sma.core.entity.CompanyImage;
import com.sma.core.entity.CompanyLocation;
import com.sma.core.entity.Recruiter;
import com.sma.core.mapper.job.JobMapper;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {
                CompanyLocationMapper.class,
                CompanyImageMapper.class,
                JobMapper.class,
})
public interface CompanyMapper {

        @Mapping(target = "companyStatus", source = "status")
        @Named("baseCompany")
        @Mapping(target = "recruiterCount", expression = "java(company.getRecruiters() != null ? Integer.valueOf(company.getRecruiters().size()) : null)")
        BaseCompanyResponse toBaseCompanyResponse(Company company);

        @Mapping(target = "companyStatus", source = "status")
        @Mapping(target = "taxIdentificationNumber", ignore = true)
        @Mapping(target = "signCommitment", ignore = true)
        @Mapping(target = "erc", ignore = true)
        @Mapping(target = "recruiterCount", ignore = true)
        @Mapping(target = "totalJobs", ignore = true)
        @Mapping(target = "recruiters", ignore = true)
        @Named("clientCompanyDetail")
        CompanyDetailResponse toCompanyDetailResponse(Company company);

        @Mapping(target = "companyStatus", source = "status")
        @Mapping(target = "recruiterCount", expression = "java(company.getRecruiters() != null ? company.getRecruiters().size() : 0)")
        @Mapping(target = "totalJobs", ignore = true) // Set manually in service
        @Named("fullCompanyDetail")
        CompanyDetailResponse toInternalCompanyResponse(Company company);

        @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        Company updateToCompany(UpdateCompanyRequest request, @MappingTarget Company company);

        default List<String> mapImagesToUrls(Set<CompanyImage> images) {
                if (images == null)
                        return null;
                return images.stream()
                                .map(CompanyImage::getUrl)
                                .toList();
        }

        @Mapping(target = "fullName", source = "user.fullName")
        @Mapping(target = "email", source = "user.email")
        @Mapping(target = "avatar", source = "user.avatar")
        RecruiterShortResponse toRecruiterShortResponse(Recruiter recruiter);

        LocationShortResponse toLocationShortResponse(CompanyLocation location);

}
