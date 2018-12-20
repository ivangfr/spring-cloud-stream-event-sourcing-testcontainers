package com.mycompany.eventservice.controller;

import com.mycompany.eventservice.dto.UserEventDto;
import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.service.UserEventService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events/users")
public class UserEventController {

    private final ModelMapper modelMapper;

    private UserEventService userEventService;

    public UserEventController(ModelMapper modelMapper, UserEventService userEventService) {
        this.modelMapper = modelMapper;
        this.userEventService = userEventService;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public List<UserEventDto> getUserEventByUserId(@PathVariable Long id) {
        List<UserEvent> userEvents = userEventService.getAllUserEventsByUserId(id);
        return userEvents.stream()
                .map(userEvent -> modelMapper.map(userEvent, UserEventDto.class))
                .collect(Collectors.toList());
    }

}
