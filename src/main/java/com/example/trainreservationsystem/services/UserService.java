package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.User;
import com.example.trainreservationsystem.repositories.UserRepository;

import java.util.List;

public class UserService {

    private final UserRepository repo = new UserRepository();

    public List<User> getAllUsers() {
        return repo.getAllUsers();
    }

    public boolean updateUser(User user) {
        return repo.updateUser(user);
    }

    public boolean deactivateUser(int id) {
        return repo.deactivateUser(id);
    }

    public boolean deleteUser(int id) {
        return repo.deleteUser(id);
    }

    public User getUserById(int id) {
        return repo.getUserById(id);
    }
}
