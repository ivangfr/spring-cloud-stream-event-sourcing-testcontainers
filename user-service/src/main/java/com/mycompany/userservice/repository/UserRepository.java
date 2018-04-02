package com.mycompany.userservice.repository;

import com.mycompany.userservice.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

    User findUserById(Long id);

    User findUserByEmail(String email);

}
