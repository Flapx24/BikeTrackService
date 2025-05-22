package com.example.demo.servicesImpl;

import java.util.List;

import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;

@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    @Qualifier("userRepository")
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(User user) {
        if (!user.getPassword().startsWith("$2a$10$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getUsername() != null) {
            user.setUsername(user.getUsername().toLowerCase());
        }

        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().toLowerCase());
        }

        return userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElse(null);
    }

    public User findByEmail(String email) {
        if (email == null) {
            return null;
        }

        List<User> users = userRepository.findByEmailIgnoreCase(email);
        return users.isEmpty() ? null : users.get(0);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public boolean existsByEmailIgnoreCase(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByEmail(username);
        if (user == null)
            throw new UsernameNotFoundException("Usuario no encontrado");
        if (!user.getActive()) {
            throw new DisabledException("El usuario está desactivado");
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> findByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public boolean existsByUsernameIgnoreCase(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    public List<User> findByEmailIgnoreCase(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }
}
