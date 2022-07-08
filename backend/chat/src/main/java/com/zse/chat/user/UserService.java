package com.zse.chat.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public User saveUser(UserController.UserDTO userDto) {
        String nick = userDto.getNick();
        Optional<User> userInDB = userRepository.findByNickname(nick);

        if (userInDB.isPresent()){
            throw new UserAlreadyExistsException(nick);
        }

        final User user = User.builder()
                .firstName(userDto.getName())
                .nickname(nick)
                .build();

        return userRepository.save(user);
    }

    public User getUserByNick(String nick) {
        return userRepository.findByNickname(nick).orElseThrow(() -> new UserNotFoundException(nick));
    }

    public User updateUserName(String nick, UserController.UserDTO userDTO) {
        Optional<User> user = userRepository.findByNickname(nick);

        User savedUser = user.orElseThrow(() -> new UserNotFoundException(nick));

        User updatedUser = User.builder()
                .id(savedUser.getId())
                .firstName(userDTO.getName())
                .nickname(savedUser.getNickname())
                .build();

        return userRepository.save(updatedUser);
    }
}
