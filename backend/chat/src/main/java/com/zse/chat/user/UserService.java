package com.zse.chat.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public User saveUser(UserController.CreateUserDTO createUserDTO) {
        final var nickname = createUserDTO.getNickname();
        if(nickname == null){
            throw new MissingPayloadFieldException("nickname");
        }

        final var email = createUserDTO.getEmail();
        if (email == null){
            throw new MissingPayloadFieldException("email");
        }

        final Optional<User> userInDB = userRepository.findByNickname(nickname);

        if (userInDB.isPresent()){
            throw new UserWithNickAlreadyExistsException(nickname);
        }

        final Optional<User> userEmail = userRepository.findByEmail(email);

        if (userEmail.isPresent()){
            throw new UserWithEmailAlreadyExistsExeption(email);
        }

        final var user = User.builder()
                .nickname(nickname)
                .firstName(createUserDTO.getFirstName())
                .lastName(createUserDTO.getLastName())
                .email(createUserDTO.getEmail())
                .phoneNumber(createUserDTO.getPhoneNumber())
                .country(createUserDTO.getCountry())
                .city(createUserDTO.getCity())
                .userLanguage(createUserDTO.getLanguage().orElse(User.Language.POLISH))
                .timeZone(TimeZone.getTimeZone("Europe/Warsaw"))
                .userStatus(User.UserStatus.OFFLINE)
                .showFirstNameAndLastName(false)
                .showEmail(false)
                .showPhoneNumber(false)
                .showAddress(false)
                .deleted(false)
                .build();

        return userRepository.save(user);
    }

    public User getUserByNick(String nick) {
        return userRepository.findByNickname(nick).orElseThrow(() -> new UserNotFoundException(nick));
    }

    public User updateUser(UserController.UpdateUserDTO updateUserDTO) {
        final var nickname = updateUserDTO.getNickname();
        if (!StringUtils.hasText(nickname)){
            throw new MissingPayloadFieldException("nickname");
        }
        final Optional<User> user = userRepository.findByNickname(nickname);

        final var savedUser = user.orElseThrow(() -> new UserNotFoundException(nickname));

        final var updatedUser = User.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .userStatus(savedUser.getUserStatus())
                .firstName(updateUserDTO.getFirstName().orElse(savedUser.getFirstName()))
                .lastName(updateUserDTO.getLastName().orElse(savedUser.getLastName()))
                .phoneNumber(updateUserDTO.getPhoneNumber().orElse(savedUser.getPhoneNumber()))
                .country(updateUserDTO.getCountry().orElse(savedUser.getCountry()))
                .city(updateUserDTO.getCity().orElse(savedUser.getCity()))
                .userStatus(updateUserDTO.getUserStatus().orElse(savedUser.getUserStatus()))
                .userLanguage(updateUserDTO.getLanguage().orElse(savedUser.getUserLanguage()))
                .timeZone(
                        updateUserDTO.getTimeZone().isPresent() ?
                                TimeZone.getTimeZone(updateUserDTO.getTimeZone().get()) :
                                savedUser.getTimeZone()
                )
                .showFirstNameAndLastName(updateUserDTO.getShowFirstNameAndLastName()
                        .orElse(savedUser.getShowFirstNameAndLastName()))
                .showEmail(updateUserDTO.getShowEmail().orElse(savedUser.getShowEmail()))
                .showPhoneNumber(updateUserDTO.getShowPhoneNumber().orElse(savedUser.getShowPhoneNumber()))
                .showAddress(updateUserDTO.getShowAddress().orElse(savedUser.getShowAddress()))
                .deleted(updateUserDTO.getDeleted().orElse(savedUser.getDeleted()))
                .build();

        return userRepository.save(updatedUser);
    }

}
