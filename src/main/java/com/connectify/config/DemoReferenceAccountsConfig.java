package com.connectify.config;

import com.connectify.entity.Role;
import com.connectify.entity.UserAccount;
import com.connectify.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Configuration
@Profile("!prod")
public class DemoReferenceAccountsConfig {

    @Bean
    @Order(4)
    CommandLineRunner referenceAccounts(UserAccountRepository accounts) {
        return args -> {
            seed(accounts, "Valeria Torres", "73918425", "organizador.demo@connectify.local", "$2a$10$Pyb/pkXGtnNgw8UO1Isy6O/6IswXMIk5p4FiIwMjUHMn9IjloPg5.", Role.ORGANIZER);
            seed(accounts, "Valeria Torres", "73918425", "organizador.altura@connectify.local", "$2a$10$Pyb/pkXGtnNgw8UO1Isy6O/6IswXMIk5p4FiIwMjUHMn9IjloPg5.", Role.ORGANIZER);
            seed(accounts, "Camila Rojas", "70451286", "admin.demo@connectify.local", "$2a$10$9TazJtJfKT6U161IYw6yPOTTsuOwDOtAF22O7C2QokQjVsIZN34ai", Role.ADMIN);
            seed(accounts, "Andrea Salazar", "75610394", "cliente.demo@connectify.local", "$2a$10$5V40br1jHqY8Ev8ofwtuzOwYaH.l7GeI3dfP5jznmDW2LCDj4zCqy", Role.CLIENT);
            seed(accounts, "Diego Morales", "78152643", "disenador.demo@connectify.local", "$2a$10$qLIiK8GjnglwXThRBn3y9uxfkIBhfsnyqlqzIwvQvFVkxOQ5QOf1G", Role.DESIGNER);
        };
    }

    private void seed(UserAccountRepository accounts, String fullName, String dni, String email, String hash, Role role) {
        if (accounts.existsByEmailIgnoreCase(email)) return;
        UserAccount account = new UserAccount();
        account.setFullName(fullName);
        account.setDni(dni);
        account.setEmail(email);
        account.setCredentialHash(hash);
        account.setRole(role);
        account.setActive(true);
        accounts.save(account);
    }
}
