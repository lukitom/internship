package com.zse.chat.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @Operation(summary = "Change User name")
    @PutMapping
    public UserResponseDTO updateUser(@RequestBody UpdateUserDTO updateUserDTO){
        User updatedUser = userService.updateUser(updateUserDTO);

        return createUserResponseDTO(updatedUser);
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class UserResponseDTO {
        private String nick;
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
        private Optional<Language> language;
        private Optional<ContentLanguage> contentLanguage;
    }

    @Data
    @AllArgsConstructor
    @Builder
    static class UpdateUserDTO{
        private String nickname;

        private Optional<String> firstName;
        private Optional<String> lastName;
        private Optional<String> phoneNumber;
        private Optional<String> country;
        private Optional<String> city;
        private Optional<Language> language;
        private Optional<ContentLanguage> contentLanguage;

        private Optional<Boolean> showFirstNameAndLastName;
        private Optional<Boolean> showEmail;
        private Optional<Boolean> showPhoneNumber;
        private Optional<Boolean> showAddress;
    }

    private UserResponseDTO createUserResponseDTO(User user){
        return UserResponseDTO.builder()
                .nick(user.getNickname())
                .firstName(user.getShowFirstNameAndLastName() ? user.getFirstName() : null)
                .lastName(user.getShowFirstNameAndLastName() ? user.getLastName() : null)
                .email(user.getShowEmail() ? user.getEmail() : null)
                .city(user.getShowAddress() ? user.getCity() : null)
                .country(user.getShowAddress() ? user.getCountry() : null)
                .phoneNumber(user.getShowPhoneNumber() ? user.getPhoneNumber() : null)
                .build();
    }
}
