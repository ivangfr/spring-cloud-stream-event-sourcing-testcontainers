package com.mycompany.userservice.mapper;

import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.context.annotation.Configuration;

@Configuration
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    User toUser(CreateUserDto createUserDto);

    UserDto toUserDto(User user);

    void updateUserFromDto(UpdateUserDto updateUserDto, @MappingTarget User user);

}
