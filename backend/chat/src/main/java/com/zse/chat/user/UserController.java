package com.zse.chat.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/users")
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public List<UserDTO> getUsers(){
        List<User> users = userService.getAllUsers();
        List<UserDTO> usersDTO = new ArrayList<>();

        for (User user : users){
            UserDTO userDTO = UserDTO.builder()
                    .name(user.getName())
                    .nick(user.getNick())
                    .build();
            usersDTO.add(userDTO);
        }

        return usersDTO;
    }

    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable int id){
        User user = userService.getUserById(id);

        return UserDTO.builder()
                .name(user.getName())
                .nick(user.getNick())
                .build();
    }

    @PostMapping("")
    public UserDTO createUser(@RequestBody UserDTO userDto){
        User savedUser = userService.saveUser(userDto);

        return UserDTO.builder()
                .name(savedUser.getName())
                .nick(savedUser.getNick())
                .build();
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable int id, @RequestBody UserDTO userDto){
        User updatedUser = userService.updateUserById(id, userDto);

        return UserDTO.builder()
                .name(updatedUser.getName())
                .nick(updatedUser.getNick())
                .build();
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class UserDTO {
        private String name;
        private String nick;
    }
}
