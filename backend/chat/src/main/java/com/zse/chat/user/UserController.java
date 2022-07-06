package com.zse.chat.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/users")
@RestController
public class UserController {

    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public List<User> getUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id){
        Optional<User> user = userService.getUserById(id);
        return user.orElseGet(User::new);
    }

    @PostMapping("")
    public void createUser(@RequestBody UserDto userDto){
        userService.saveUser(userDto);
    }

    @PutMapping("/{id}")
    public void updateUser(@PathVariable int id, @RequestBody UserDto userDto){
        userService.updateUserById(id, userDto);
    }

    @Data
    @AllArgsConstructor
    static class UserDto{
        private String name;
        private String nick;
    }
}
