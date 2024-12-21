package com.ivanfranchin.eventservice.userevent;

import com.ivanfranchin.eventservice.userevent.model.UserEvent;

import java.util.List;

public interface UserEventService {

    List<UserEvent> getUserEvents(Long id);

    UserEvent saveUserEvent(UserEvent userEvent);
}
