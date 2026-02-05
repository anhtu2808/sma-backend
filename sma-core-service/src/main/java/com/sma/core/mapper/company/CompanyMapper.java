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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring",
        uses = {
                CompanyLocationMapper.class,
                CompanyImageMapper.class,
                JobMapper.class,
        })
public interface CompanyMapper {

    @Mapping(target = "companyStatus", source = "status")
    @Named("baseCompany")
    BaseCompanyResponse toBaseCompanyResponse(Company company);

    @Mapping(target = "companyStatus", source = "status")
    @Named("clientCompanyDetail")
    CompanyDetailResponse toCompanyDetailResponse(Company company);

    @Mapping(target = "companyStatus", source = "status")
    @Named("fullCompanyDetail")
    CompanyDetailResponse toInternalCompanyResponse(Company company);

    Company updateToCompany(UpdateCompanyRequest request, @MappingTarget Company company);
    CompanyResponse toResponse(Company company);
    AdminCompanyResponse toAdminResponse(Company company);


    @Mapping(target = "images", expression = "java(mapImagesToUrls(company.getImages()))")
    @Mapping(target = "totalJobs", ignore = true)
    @Mapping(target = "rejectReason", source = "rejectReason")
    CompanyResponse toDetailResponse(Company company);

    default List<String> mapImagesToUrls(Set<CompanyImage> images) {
        if (images == null) return null;
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
