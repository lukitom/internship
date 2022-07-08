package com.zse.chat.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users")
@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @GetMapping
    public List<UserDTO> getUsers(){
        return userService.getAllUsers().stream()
                .map(this::createUserDTO)
                .toList();
    }

    @Operation(summary = "Get user by nick")
    @GetMapping("/{nick}")
    public UserDTO getUser(@PathVariable String nick){
        User user = userService.getUserById(nick);

        return createUserDTO(user);
    }

    @Operation(summary = "Create new User")
    @PostMapping
    public UserDTO createUser(@RequestBody UserDTO userDto){
        User savedUser = userService.saveUser(userDto);

        return createUserDTO(savedUser);
    }

    @Operation(summary = "Change User name")
    @PutMapping("/{nick}")
    public UserDTO updateUser(@PathVariable String nick, @RequestBody UserDTO userDto){
        User updatedUser = userService.updateUserName(nick, userDto);

        return createUserDTO(updatedUser);
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class UserDTO {
        private String name;
        private String nick;
    }

    private UserDTO createUserDTO(User user){
        return UserDTO.builder()
                .name(user.getName())
                .nick(user.getNick())
                .build();
    }
}
