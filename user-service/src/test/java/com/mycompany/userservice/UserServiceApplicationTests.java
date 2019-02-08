package com.mycompany.userservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith({SpringExtension.class, ContainersExtension.class})
@SpringBootTest
public class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
