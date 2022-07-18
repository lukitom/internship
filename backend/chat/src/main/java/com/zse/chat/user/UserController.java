package com.zse.chat.user;

import com.zse.chat.login.VerifyJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.experimental.FieldDefaults;
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

    @GetMapping("/details")
    @VerifyJWT
    public UserDetailResponseDTO getUserDetails(UserDetailRequestDTO userDetailRequestDTO){
        User user = userService.getUserByNick(userDetailRequestDTO.getNickname());

        return createUserDetailResponseDTO(user);
    }

    @Operation(summary = "Create new User")
    @PostMapping
    public UserResponseDTO createUser(@RequestBody CreateUserDTO createUserDTO){
        User savedUser = userService.saveUser(createUserDTO);

        return createUserResponseDTO(savedUser);
    }

    @Operation(summary = "Update User details")
    @PutMapping
    @VerifyJWT
    public UserResponseDTO updateUser(@RequestBody UpdateUserDTO updateUserDTO){
        User updatedUser = userService.updateUser(updateUserDTO);

        return createUserResponseDTO(updatedUser);
    }

    //region DTOs
    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class UserResponseDTO {
        String nickname;
        String firstName;
        String lastName;
        String email;
        String phoneNumber;
        String country;
        String city;
    }

    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class CreateUserDTO implements UserNickname {
        String nickname;
        String firstName;
        String lastName;
        String email;
        String phoneNumber;
        String country;
        String city;
        Optional<User.Language> language;
    }

    @Data
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class UpdateUserDTO implements UserNickname {
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

    @Data
    @AllArgsConstructor
    static class UserDetailRequestDTO implements UserNickname {
        String nickname;
    }

    @Builder
    record UserDetailResponseDTO(
            String nickname,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String country,
            String city,
            String userStatus,
            String userLanguage,
            String timeZone,
            Boolean showFirstNameAndLastName,
            Boolean showEmail,
            Boolean showPhoneNumber,
            Boolean showAddress,
            Boolean deleted
    ) {}
    //endregion

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

    private UserDetailResponseDTO createUserDetailResponseDTO(User user) {
        return UserDetailResponseDTO.builder()
                .nickname(user.getNickname())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .country(user.getCountry())
                .city(user.getCity())
                .userStatus(String.valueOf(user.getUserStatus()))
                .userLanguage(String.valueOf(user.getUserLanguage()))
                .timeZone(String.valueOf(user.getTimeZone().getID()))
                .showFirstNameAndLastName(user.getShowFirstNameAndLastName())
                .showEmail(user.getShowEmail())
                .showPhoneNumber(user.getShowPhoneNumber())
                .showAddress(user.getShowAddress())
                .deleted(user.getDeleted())
                .build();
    }
}
