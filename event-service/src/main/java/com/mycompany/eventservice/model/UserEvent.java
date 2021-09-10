package com.mycompany.eventservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("user_events")
public class UserEvent {

    @PrimaryKey
    private UserEventKey key;

    private String type;
    private String data;
}
