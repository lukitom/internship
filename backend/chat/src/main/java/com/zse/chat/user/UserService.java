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
        Optional<User> userInDB = userRepository.findByNick(nick);

        if (userInDB.isPresent()){
            throw new UserAlreadyExistsException(nick);
        }

        final User user = User.builder()
                .name(userDto.getName())
                .nick(nick)
                .build();

        return userRepository.save(user);
    }

    public User getUserByNick(String nick) {
        return userRepository.findByNick(nick).orElseThrow(() -> new UserNotFoundException(nick));
    }

    public User updateUserName(String nick, UserController.UserDTO userDTO) {
        Optional<User> user = userRepository.findByNick(nick);

        User savedUser = user.orElseThrow(() -> new UserNotFoundException(nick));

        User updatedUser = User.builder()
                .id(savedUser.getId())
                .name(userDTO.getName())
                .nick(savedUser.getNick())
                .build();

        return userRepository.save(updatedUser);
    }
}
