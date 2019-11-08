package com.mycompany.eventservice.mapper;

import com.mycompany.eventservice.dto.UserEventDto;
import com.mycompany.eventservice.model.UserEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    @Mapping(source = "key.userId", target = "userId")
    @Mapping(source = "key.datetime", target = "datetime")
    UserEventDto toUserEventDto(UserEvent userEvent);

}
