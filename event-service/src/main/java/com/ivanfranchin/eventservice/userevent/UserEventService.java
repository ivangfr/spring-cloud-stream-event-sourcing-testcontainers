package com.ivanfranchin.eventservice.userevent;

import com.ivanfranchin.eventservice.userevent.model.UserEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
}
