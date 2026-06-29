package com.connectify.controller;

import com.connectify.entity.Role;
import com.connectify.entity.UserAccount;
import com.connectify.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Controller
public class SetupController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public SetupController(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/setup")
    public String setup() {
        if (userAccountRepository.existsByRole(Role.DEVELOPER)) {
            return "redirect:/login?setupExists";
        }
        return "setup/index";
    }

    @PostMapping("/setup")
    public String createInitialUser(@RequestParam String fullName,
                                    @RequestParam String email,
                                    @RequestParam String password,
                                    Model model) {
        if (userAccountRepository.existsByRole(Role.DEVELOPER)) {
            return "redirect:/login?setupExists";
        }

        String normalizedName = fullName == null ? "" : fullName.trim();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalizedName.length() < 3) {
            model.addAttribute("error", "Escribe el nombre de la cuenta técnica.");
            return "setup/index";
        }
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("error", "Ingresa un correo válido.");
            return "setup/index";
        }
        if (password == null || password.length() < 8) {
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "setup/index";
        }
        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            model.addAttribute("error", "Ese correo ya está asociado a una cuenta.");
            return "setup/index";
        }

        UserAccount account = new UserAccount();
        account.setFullName(normalizedName);
        account.setEmail(normalizedEmail);
        account.setCredentialHash(passwordEncoder.encode(password));
        account.setRole(Role.DEVELOPER);
        account.setActive(true);
        userAccountRepository.save(account);

        return "redirect:/login?setupSuccess";
    }
}
