package com.zse.chat.user;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByNickname(String nick);

    Optional<User> findByEmail(String email);

}
