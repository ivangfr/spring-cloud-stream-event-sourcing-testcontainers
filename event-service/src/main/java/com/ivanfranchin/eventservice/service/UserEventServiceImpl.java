package com.ivanfranchin.eventservice.service;

import com.ivanfranchin.eventservice.model.UserEvent;
import com.ivanfranchin.eventservice.repository.UserEventRepository;
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
