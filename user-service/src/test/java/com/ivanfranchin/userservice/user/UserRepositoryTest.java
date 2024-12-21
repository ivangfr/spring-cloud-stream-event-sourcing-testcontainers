package com.ivanfranchin.userservice.user;

import com.ivanfranchin.userservice.MySQLTestcontainers;
import com.ivanfranchin.userservice.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ImportTestcontainers(MySQLTestcontainers.class)
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