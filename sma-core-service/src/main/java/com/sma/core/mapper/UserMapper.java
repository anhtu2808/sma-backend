package com.sma.core.mapper;

import com.sma.core.dto.request.auth.RegisterRequest;
import com.sma.core.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(RegisterRequest rqs);

}
