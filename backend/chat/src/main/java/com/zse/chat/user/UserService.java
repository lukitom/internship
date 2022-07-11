package com.zse.chat.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        String nickname = createUserDTO.getNickname();
        if(nickname == null){
            throw new MissingPayloadFieldException("nickname");
        }

        Optional<User> userInDB = userRepository.findByNickname(nickname);

        if (userInDB.isPresent()){
            throw new UserWithNickAlreadyExistsException(nickname);
        }

        String email = createUserDTO.getEmail();
        Optional<User> userEmail = userRepository.findByEmail(email);

        if (userEmail.isPresent()){
            throw new UserWithEmailAlreadyExistsExeption(email);
        }

        final User user = User.builder()
                .nickname(nickname)
                .firstName(createUserDTO.getFirstName())
                .lastName(createUserDTO.getLastName())
                .email(createUserDTO.getEmail())
                .phoneNumber(createUserDTO.getPhoneNumber())
                .country(createUserDTO.getCountry())
                .city(createUserDTO.getCity())
                .userLanguage(createUserDTO.getLanguage().orElse(Language.POLISH))
                .contentLanguage(createUserDTO.getContentLanguage().orElse(ContentLanguage.MY_LANGUAGE))
                .timeZone(TimeZone.getTimeZone("Europe/Warsaw"))
                .userStatus(UserStatus.OFFLINE)
                .showFirstNameAndLastName(false)
                .showEmail(false)
                .showPhoneNumber(false)
                .showAddress(false)
                .build();

        return userRepository.save(user);
    }

    public User getUserByNick(String nick) {
        return userRepository.findByNickname(nick).orElseThrow(() -> new UserNotFoundException(nick));
    }

    public User updateUser(UserController.UpdateUserDTO updateUserDTO) {
        String nick = updateUserDTO.getNickname();
        if (nick == null){
            throw new MissingPayloadFieldException("nickname");
        }
        Optional<User> user = userRepository.findByNickname(nick);

        User savedUser = user.orElseThrow(() -> new UserNotFoundException(nick));

        User updatedUser = User.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .userStatus(savedUser.getUserStatus())
                .firstName(updateUserDTO.getFirstName().orElse(savedUser.getFirstName()))
                .lastName(updateUserDTO.getLastName().orElse(savedUser.getLastName()))
                .phoneNumber(updateUserDTO.getPhoneNumber().orElse(savedUser.getPhoneNumber()))
                .country(updateUserDTO.getCountry().orElse(savedUser.getCountry()))
                .city(updateUserDTO.getCity().orElse(savedUser.getCity()))
                .userLanguage(updateUserDTO.getLanguage().orElse(savedUser.getUserLanguage()))
                .contentLanguage(updateUserDTO.getContentLanguage().orElse(savedUser.getContentLanguage()))
                .showFirstNameAndLastName(updateUserDTO.getShowFirstNameAndLastName()
                        .orElse(savedUser.getShowFirstNameAndLastName()))
                .showEmail(updateUserDTO.getShowEmail().orElse(savedUser.getShowEmail()))
                .showPhoneNumber(updateUserDTO.getShowPhoneNumber().orElse(savedUser.getShowPhoneNumber()))
                .showAddress(updateUserDTO.getShowAddress().orElse(savedUser.getShowAddress()))
                .build();

        return userRepository.save(updatedUser);
    }
}
