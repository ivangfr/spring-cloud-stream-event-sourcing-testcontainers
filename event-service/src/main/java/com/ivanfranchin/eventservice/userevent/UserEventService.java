package com.ivanfranchin.eventservice.userevent;

import com.ivanfranchin.eventservice.userevent.model.UserEvent;
import com.ivanfranchin.eventservice.userevent.model.UserEventKey;
import com.ivanfranchin.userservice.messages.UserEventMessage;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserEventService {

  private final UserEventRepository userEventRepository;

  public List<UserEvent> getUserEvents(Long id) {
    return userEventRepository.findByKeyUserId(id);
  }

  public UserEvent saveUserEvent(UserEvent userEvent) {
    return userEventRepository.save(userEvent);
  }

  public UserEvent saveUserEvent(Message<UserEventMessage> message) {
    UserEventMessage payload = message.getPayload();
    UserEvent userEvent = new UserEvent();
    userEvent.setKey(new UserEventKey(payload.getUserId(), new Date(payload.getEventTimestamp())));
    userEvent.setType(payload.getEventType().toString());
    CharSequence userJson = payload.getUserJson();
    if (userJson != null) {
      userEvent.setData(userJson.toString());
    }
    return saveUserEvent(userEvent);
  }
}
