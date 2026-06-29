package com.connectify.controller;

import com.connectify.entity.Role;
import com.connectify.entity.UserAccount;
import com.connectify.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Controller
@RequestMapping("/dashboard/developer/accounts/organizer")
public class DeveloperOrganizerProvisionController {

    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public DeveloperOrganizerProvisionController(UserAccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String form() {
        return "dashboard/developer/organizer-account";
    }

    @PostMapping
    public String create(@RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam String dni,
                         @RequestParam String email,
                         @RequestParam String password,
                         Model model) {
        String names = clean(firstName);
        String lastNames = clean(lastName);
        String document = clean(dni);
        String address = clean(email).toLowerCase(Locale.ROOT);
        if (names.length() < 2 || lastNames.length() < 2 || !document.matches("\\d{8}")
                || !address.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password == null || password.length() < 8) {
            model.addAttribute("error", "Completa nombres, apellidos, DNI válido, correo y una contraseña de al menos 8 caracteres.");
            return "dashboard/developer/organizer-account";
        }
        if (accounts.existsByEmailIgnoreCase(address)) {
            model.addAttribute("error", "Ya existe una cuenta con ese correo.");
            return "dashboard/developer/organizer-account";
        }
        UserAccount account = new UserAccount();
        account.setFullName(names + " " + lastNames);
        account.setDni(document);
        account.setEmail(address);
        account.setCredentialHash(passwordEncoder.encode(password));
        account.setRole(Role.ORGANIZER);
        account.setActive(true);
        accounts.save(account);
        return "redirect:/dashboard/developer/accounts/organizer?created=true";
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
