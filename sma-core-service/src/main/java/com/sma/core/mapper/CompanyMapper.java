package com.sma.core.mapper;

import com.sma.core.dto.response.company.AdminCompanyResponse;
import com.sma.core.dto.response.company.CompanyResponse;
import com.sma.core.dto.response.company.LocationShortResponse;
import com.sma.core.dto.response.recruiter.RecruiterShortResponse;
import com.sma.core.entity.Company;
import com.sma.core.entity.CompanyImage;
import com.sma.core.entity.CompanyLocation;
import com.sma.core.entity.Recruiter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponse toResponse(Company company);
    AdminCompanyResponse toAdminResponse(Company company);


    @Mapping(target = "images", expression = "java(mapImagesToUrls(company.getImages()))")
    @Mapping(target = "totalJobs", ignore = true)
    CompanyResponse toDetailResponse(Company company);

    default List<String> mapImagesToUrls(Set<CompanyImage> images) {
        if (images == null) return null;
        return images.stream()
                .map(CompanyImage::getUrl)
                .toList();
    }

    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    RecruiterShortResponse toRecruiterShortResponse(Recruiter recruiter);

    LocationShortResponse toLocationShortResponse(CompanyLocation location);
}
