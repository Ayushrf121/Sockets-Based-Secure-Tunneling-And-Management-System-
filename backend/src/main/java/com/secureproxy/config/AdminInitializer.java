package com.secureproxy.config;

import com.secureproxy.model.User;
import com.secureproxy.repository.UserRepository;
import com.secureproxy.security.PasswordHasher;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordHasher passwordHasher) {

        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(String... args) {

        String name = System.getenv("ADMIN_NAME");
        String password = System.getenv("ADMIN_PASSWORD");

        if (name == null || password == null) {
            System.out.println(
                    "Admin environment variables not configured."
            );
            return;
        }

        if (userRepository
                .findByNameAndRole(name, "ADMIN")
                .isPresent()) {

            return;
        }

        User admin = new User(
                name,
                passwordHasher.hash(password)
        );

        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");

        userRepository.save(admin);

        System.out.println(
                "Admin account created."
        );
    }
}