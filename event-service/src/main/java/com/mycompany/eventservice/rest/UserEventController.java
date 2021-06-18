package com.mycompany.eventservice.rest;

import com.mycompany.eventservice.mapper.UserMapper;
import com.mycompany.eventservice.rest.dto.UserEventDto;
import com.mycompany.eventservice.service.UserEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/events/users")
public class UserEventController {

    private final UserEventService userEventService;
    private final UserMapper userMapper;

    @GetMapping("/{id}")
    public List<UserEventDto> getUserEvents(@PathVariable Long id) {
        log.info("GET Request, id: {}", id);
        return userEventService.getUserEvents(id)
                .stream()
                .map(userMapper::toUserEventDto)
                .collect(Collectors.toList());
    }

}
