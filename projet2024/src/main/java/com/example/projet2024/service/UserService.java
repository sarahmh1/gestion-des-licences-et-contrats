package com.example.projet2024.service;

import com.example.projet2024.DTO.UserDTO;
import com.example.projet2024.DTO.UserUpdateRequest;
import com.example.projet2024.Enum.Role_Enum;
import com.example.projet2024.Security.Jwt.JwtUtils;
import com.example.projet2024.entite.User;
import com.example.projet2024.mapper.UserMapper;
import com.example.projet2024.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    private UserMapper userMapper;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUserProfileById(Long id, UserUpdateRequest updateRequest) {
        return userRepository.findById(id)
                .map(user -> {
                    // Update basic profile fields
                    user.setFirstname(updateRequest.getFirstname());
                    user.setLastname(updateRequest.getLastname());
                    user.setPhoneNumber(updateRequest.getPhoneNumber());
                    user.setSex(updateRequest.getSex());
                    user.setDateOfBirth(updateRequest.getDateOfBirth());
                    user.setVerified(true);

                    // Handle password update if provided
                    if (updateRequest.getNewPassword() != null && !updateRequest.getNewPassword().isEmpty()) {
                        String encodedPassword = passwordEncoder.encode(updateRequest.getNewPassword());
                        user.setPassword(encodedPassword);
                    }

                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFirstname(updatedUser.getFirstname());
                    user.setLastname(updatedUser.getLastname());
                    user.setEmail(updatedUser.getEmail());
                    user.setPassword(updatedUser.getPassword());
                    user.setSex(updatedUser.getSex());
                    user.setPhoneNumber(updatedUser.getPhoneNumber());
                    user.setDateOfBirth(updatedUser.getDateOfBirth());
                    user.setRole(updatedUser.getRole());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User assignUserRole(Long id, Role_Enum newRole) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé.");
        }

        User user = optionalUser.get();
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public boolean verifyUser(String token) {
        Optional<User> optionalUser = userRepository.findByVerificationToken(token);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setVerified(true);
            user.setVerificationToken(null); // Clear the verification token after verification
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public String Login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return jwt;

    }

    public List<UserDTO> getAllusers() {
        List<User> usersList = userRepository.findAll();
        return userMapper.UserListToUserDTOList(usersList);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }

}
