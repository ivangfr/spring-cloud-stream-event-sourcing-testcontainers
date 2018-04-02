package com.mycompany.eventservice.service;

import com.mycompany.eventservice.model.UserEvent;

import java.util.List;

public interface UserEventService {

    List<UserEvent> getAllUserEventsByUserId(Long id);

    UserEvent saveUserEvent(UserEvent userEvent);

}
