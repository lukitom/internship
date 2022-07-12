package com.zse.chat.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Tag(name = "Users")
@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @GetMapping
    public List<UserResponseDTO> getUsers(){
        return userService.getAllUsers().stream()
                .map(this::createUserResponseDTO)
                .toList();
    }

    @Operation(
            summary = "Get user by nick",
            parameters = {@Parameter(name = "nick", description = "User nick")}
    )
    @GetMapping("/{nick}")
    public UserResponseDTO getUser(@PathVariable String nick){
        User user = userService.getUserByNick(nick);

        return createUserResponseDTO(user);
    }

    @Operation(summary = "Create new User")
    @PostMapping
    public UserResponseDTO createUser(@RequestBody CreateUserDTO createUserDTO){
        User savedUser = userService.saveUser(createUserDTO);

        return createUserResponseDTO(savedUser);
    }

    @Operation(summary = "Update User details")
    @PutMapping
    public UserResponseDTO updateUser(@RequestBody UpdateUserDTO updateUserDTO){
        User updatedUser = userService.updateUser(updateUserDTO);

        return createUserResponseDTO(updatedUser);
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class UserResponseDTO {
        private String nickname;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String country;
        private String city;
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class CreateUserDTO {
        private String nickname;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String country;
        private String city;
        private Optional<User.Language> language;
    }

    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class UpdateUserDTO{
        String nickname;

        Optional<String> firstName;
        Optional<String> lastName;
        Optional<String> phoneNumber;
        Optional<String> country;
        Optional<String> city;
        Optional<User.Language> language;

        Optional<Boolean> showFirstNameAndLastName;
        Optional<Boolean> showEmail;
        Optional<Boolean> showPhoneNumber;
        Optional<Boolean> showAddress;

        Optional<Boolean> deleted;
    }

    private UserResponseDTO createUserResponseDTO(User user){
        return UserResponseDTO.builder()
                .nickname(user.getNickname())
                .firstName(user.getShowFirstNameAndLastName() ? user.getFirstName() : null)
                .lastName(user.getShowFirstNameAndLastName() ? user.getLastName() : null)
                .email(user.getShowEmail() ? user.getEmail() : null)
                .city(user.getShowAddress() ? user.getCity() : null)
                .country(user.getShowAddress() ? user.getCountry() : null)
                .phoneNumber(user.getShowPhoneNumber() ? user.getPhoneNumber() : null)
                .build();
    }
}
