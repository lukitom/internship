package com.zse.chat.user;

import com.zse.chat.login.VerifyJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

@Tag(name = "Users")
@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @GetMapping
    @VerifyJWT(withoutArgs = true)
    @SecurityRequirement(name = "JWT")
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
    @VerifyJWT(withoutArgs = true)
    @SecurityRequirement(name = "JWT")
    public UserResponseDTO getUser(@PathVariable String nick){
        final var user = userService.getUserByNick(nick);

        return createUserResponseDTO(user);
    }

    @GetMapping("/details")
    @VerifyJWT
    @SecurityRequirement(name = "JWT")
    public UserDetailResponseDTO getUserDetails(UserDetailRequestDTO userDetailRequestDTO){
        final var user = userService.getUserByNick(userDetailRequestDTO.getNickname());

        return createUserDetailResponseDTO(user);
    }

    @Operation(summary = "Create new User")
    @PostMapping
    public UserResponseDTO createUser(@RequestBody @Valid CreateUserDTO createUserDTO) {
        final var savedUser = userService.saveUser(createUserDTO);

        log.info("User with nickname: {} has been created", createUserDTO.getNickname());
        return createUserResponseDTO(savedUser);
    }

    @Operation(summary = "Update User details")
    @PutMapping
    @VerifyJWT
    @SecurityRequirement(name = "JWT")
    public UserResponseDTO updateUser(@RequestBody UpdateUserDTO updateUserDTO){
        final var updatedUser = userService.updateUser(updateUserDTO);

        return createUserResponseDTO(updatedUser);
    }

    //region DTOs
    @Value
    @Builder
    @Jacksonized
    static class UserResponseDTO {
        String nickname;
        String firstName;
        String lastName;
        String email;
        String phoneNumber;
        String country;
        String city;
    }

    @Value
    @Builder
    @Jacksonized
    static class CreateUserDTO implements UserNickname {
        @Setter
        @NonFinal
        @Size(min = 3, message = "Nickname length should be at least 3 characters")
        String nickname;
        @Size(min = 3, message = "First name length should be at least 3 characters")
        String firstName;
        @Size(min = 3, message = "Last name length should be at least 3 characters")
        String lastName;
        @Email(message = "Email should be correctly formatted")
        String email;
        @Pattern(regexp = "\\d{9}", message = "Provide phone number in format 111222333 (9 numbers without space)")
        String phoneNumber;
        @Pattern(regexp = "[A-Z][a-z]*", message = "Country should be starting with capital letter then all small letters")
        String country;
        @Pattern(regexp = "[A-Z][a-z]*", message = "City should be starting with capital letter then all small letters")
        String city;
        Optional<User.Language> language;
    }

    @Value
    @Builder
    @Jacksonized
    static class UpdateUserDTO implements UserNickname {
        @Setter
        @NonFinal
        String nickname;

        Optional<String> firstName;
        Optional<String> lastName;
        Optional<String> phoneNumber;
        Optional<String> country;
        Optional<String> city;
        Optional<User.UserStatus> userStatus;
        Optional<User.Language> language;
        Optional<String> timeZone;

        Optional<Boolean> showFirstNameAndLastName;
        Optional<Boolean> showEmail;
        Optional<Boolean> showPhoneNumber;
        Optional<Boolean> showAddress;

        Optional<Boolean> deleted;
    }

    @Value
    @Builder
    @Jacksonized
    static class UserDetailRequestDTO implements UserNickname {
        @Setter
        @NonFinal
        String nickname;
    }

    @Builder
    @Jacksonized
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
