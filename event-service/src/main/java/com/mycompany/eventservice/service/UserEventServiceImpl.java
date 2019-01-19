package com.mycompany.eventservice.service;

import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.repository.UserEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserEventServiceImpl implements UserEventService {

    private UserEventRepository userEventRepository;

    public UserEventServiceImpl(UserEventRepository userEventRepository) {
        this.userEventRepository = userEventRepository;
    }

    @Override
    public List<UserEvent> getAllUserEvents(Long id) {
        return userEventRepository.findByKeyUserId(id);
    }

    @Override
    public UserEvent saveUserEvent(UserEvent userEvent) {
        return userEventRepository.save(userEvent);
    }

}
