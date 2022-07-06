package com.zse.chat.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

    public User updateUserById(int id, UserController.UserDTO userDto) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
