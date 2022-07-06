package com.zse.chat.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public User saveUser(UserController.UserDTO userDto) {
        final User user = User.builder()
                .name(userDto.getName())
                .nick(userDto.getNick())
                .build();

        userRepository.save(user);
        return user;
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User updateUserById(int id, UserController.UserDTO userDTO) {
        User updatedUser = User.builder()
                .id(id)
                .name(userDTO.getName())
                .nick(userDTO.getNick())
                .build();
        return userRepository.save(updatedUser);
    }
}
