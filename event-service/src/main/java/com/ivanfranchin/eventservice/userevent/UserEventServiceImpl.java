package com.ivanfranchin.eventservice.userevent;

import com.ivanfranchin.eventservice.userevent.model.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserEventServiceImpl implements UserEventService {

    private final UserEventRepository userEventRepository;

    @Override
    public List<UserEvent> getUserEvents(Long id) {
        return userEventRepository.findByKeyUserId(id);
    }

    @Override
    public UserEvent saveUserEvent(UserEvent userEvent) {
        return userEventRepository.save(userEvent);
    }
}
