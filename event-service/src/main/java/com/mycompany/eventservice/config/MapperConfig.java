package com.mycompany.eventservice.config;

import com.mycompany.eventservice.dto.UserEventDto;
import com.mycompany.eventservice.model.UserEvent;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    ModelMapper modelMapper() {
        PropertyMap<UserEvent, UserEventDto> userEventMap = new PropertyMap<UserEvent, UserEventDto>() {
            protected void configure() {
                map().setUserId(source.getKey().getUserId());
                map().setDatetime(source.getKey().getDatetime());
            }
        };

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.addMappings(userEventMap);
        return modelMapper;
    }

}