package com.zse.chat.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public void saveUser(UserController.UserDto userDto) {
        final var user = new User();
        user.setName(userDto.getName());
        user.setNick(userDto.getNick());

        userRepository.save(user);
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    public void updateUserById(int id, UserController.UserDto userDto) {
        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) return;

        User updatedUser = user.get();
        updatedUser.setName(userDto.getName());
        updatedUser.setNick(userDto.getNick());
        userRepository.save(updatedUser);

    }
}
