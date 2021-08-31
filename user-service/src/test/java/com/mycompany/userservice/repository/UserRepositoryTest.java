package com.mycompany.userservice.repository;

import com.mycompany.userservice.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindUserByEmailWhenThereNone() {
        Optional<User> userOptional = userRepository.findUserByEmail("email@test");

        assertThat(userOptional).isNotNull();
        assertThat(userOptional.isPresent()).isFalse();
    }

    @Test
    void testFindUserByEmailWhenThereIsOne() {
        userRepository.save(new User("email@test", "fullName", true));

        Optional<User> userOptional = userRepository.findUserByEmail("email@test");
        assertThat(userOptional).isNotNull();
        assertThat(userOptional.isPresent()).isTrue();
        assertThat(userOptional.get().getId()).isEqualTo(1);
        assertThat(userOptional.get().getEmail()).isEqualTo("email@test");
        assertThat(userOptional.get().getFullName()).isEqualTo("fullName");
        assertThat(userOptional.get().getActive()).isTrue();
        assertThat(userOptional.get().getCreatedAt()).isNotNull();
        assertThat(userOptional.get().getUpdatedAt()).isNotNull();
    }
}