package com.ivanfranchin.eventservice.rest;

import com.ivanfranchin.eventservice.rest.dto.UserEventResponse;
import com.ivanfranchin.eventservice.service.UserEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/events")
public class UserEventController {

    private final UserEventService userEventService;

    @GetMapping
    public List<UserEventResponse> getUserEvents(@RequestParam(name = "userId") Long id) {
        log.info("GET Request, id: {}", id);
        return userEventService.getUserEvents(id)
                .stream()
                .map(UserEventResponse::from)
                .toList();
    }
}
