package com.springshop.springshop.config;

import com.springshop.springshop.entity.User;
import com.springshop.springshop.entity.enums.Role;
import com.springshop.springshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password) {
        return args -> {
            if (!userRepository.existsByEmail(email)) {
                userRepository.save(User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .fullName("Administrateur")
                        .role(Role.ADMIN)
                        .build());
            }
        };
    }
}
