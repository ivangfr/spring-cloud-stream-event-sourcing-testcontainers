package com.mycompany.eventservice.repository;

import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.model.UserEventKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEventRepository extends CassandraRepository<UserEvent, UserEventKey> {

    List<UserEvent> findByKeyUserId(Long id);

}
