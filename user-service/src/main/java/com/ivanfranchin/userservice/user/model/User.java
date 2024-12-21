package com.ivanfranchin.userservice.user.model;

import com.ivanfranchin.userservice.user.dto.CreateUserRequest;
import com.ivanfranchin.userservice.user.dto.UpdateUserRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public User(String email, String fullName, Boolean active) {
        this.email = email;
        this.fullName = fullName;
        this.active = active;
    }

    @PrePersist
    public void onPrePersist() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = Instant.now();
    }

    public static User from(CreateUserRequest createUserRequest) {
        return new User(
                createUserRequest.email(),
                createUserRequest.fullName(),
                createUserRequest.active()
        );
    }

    public static void updateFrom(UpdateUserRequest updateUserRequest, User user) {
        if (updateUserRequest.email() != null) {
            user.setEmail(updateUserRequest.email());
        }
        if (updateUserRequest.fullName() != null) {
            user.setFullName(updateUserRequest.fullName());
        }
        if (updateUserRequest.active() != null) {
            user.setActive(updateUserRequest.active());
        }
    }
}
