package com.ivanfranchin.eventservice.service;

import com.ivanfranchin.eventservice.model.UserEvent;

import java.util.List;

public interface UserEventService {

    List<UserEvent> getUserEvents(Long id);

    UserEvent saveUserEvent(UserEvent userEvent);
}
