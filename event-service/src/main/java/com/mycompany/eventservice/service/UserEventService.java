package com.mycompany.eventservice.service;

import com.mycompany.eventservice.model.UserEvent;

import java.util.List;

public interface UserEventService {

    List<UserEvent> getUserEvents(Long id);

    UserEvent saveUserEvent(UserEvent userEvent);

}
