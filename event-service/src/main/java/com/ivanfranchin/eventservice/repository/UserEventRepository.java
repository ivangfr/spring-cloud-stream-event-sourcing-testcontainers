package com.ivanfranchin.eventservice.repository;

import com.ivanfranchin.eventservice.model.UserEvent;
import com.ivanfranchin.eventservice.model.UserEventKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventRepository extends CassandraRepository<UserEvent, UserEventKey> {

    List<UserEvent> findByKeyUserId(Long id);
}
