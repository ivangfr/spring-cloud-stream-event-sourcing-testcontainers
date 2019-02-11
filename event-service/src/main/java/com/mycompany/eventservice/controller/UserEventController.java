package com.mycompany.eventservice.controller;

import com.mycompany.eventservice.dto.UserEventDto;
import com.mycompany.eventservice.service.UserEventService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/events/users")
public class UserEventController {

    private final UserEventService userEventService;
    private final ModelMapper modelMapper;

    public UserEventController(UserEventService userEventService, ModelMapper modelMapper) {
        this.userEventService = userEventService;
        this.modelMapper = modelMapper;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/{id}")
    public List<UserEventDto> getUserEvents(@PathVariable Long id) {
        log.info("GET Request, id: {}", id);
        return userEventService.getAllUserEvents(id)
                .stream()
                .map(userEvent -> modelMapper.map(userEvent, UserEventDto.class))
                .collect(Collectors.toList());
    }

}
