package com.ivanfranchin.eventservice.userevent;

import com.ivanfranchin.eventservice.userevent.model.UserEvent;
import com.ivanfranchin.eventservice.userevent.model.UserEventKey;
import java.util.List;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEventRepository extends CassandraRepository<UserEvent, UserEventKey> {

  List<UserEvent> findByKeyUserId(Long id);
}
