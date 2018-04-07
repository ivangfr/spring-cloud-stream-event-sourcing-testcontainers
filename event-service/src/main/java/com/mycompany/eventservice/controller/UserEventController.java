package com.mycompany.eventservice.controller;

import com.mycompany.eventservice.dto.UserEventDto;
import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.service.UserEventService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/events/users")
public class UserEventController {

    private final ModelMapper modelMapper;

    private UserEventService userEventService;

    public UserEventController(ModelMapper modelMapper, UserEventService userEventService) {
        this.modelMapper = modelMapper;
        this.userEventService = userEventService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<UserEventDto>> getUserEventByUserId(@PathVariable Long id) {
        List<UserEvent> userEvents = userEventService.getAllUserEventsByUserId(id);

        List<UserEventDto> userEventDtos = new ArrayList<>();
        for (UserEvent userEvent : userEvents) {
            UserEventDto userEventDto = modelMapper.map(userEvent, UserEventDto.class);
            userEventDtos.add(userEventDto);
        }

        return ResponseEntity.ok(userEventDtos);
    }

}
