package com.zse.chat.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public void saveUser(String name, int age){
        User user = new User();
        user.setName(name);
        user.setAge(age);
        userRepository.save(user);
    }
}
