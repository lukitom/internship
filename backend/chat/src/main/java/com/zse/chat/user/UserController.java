package com.zse.chat.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/user")
    public User test(){
        return new User(1L, "testImie", 24);
    }

    @PostMapping("/user")
    public void test2(){
    }
}
