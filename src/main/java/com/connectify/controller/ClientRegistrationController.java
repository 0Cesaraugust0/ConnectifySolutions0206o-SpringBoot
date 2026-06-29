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
public class ClientRegistrationController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientRegistrationController(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(required = false) String phone,
                           Model model) {
        String normalizedName = fullName == null ? "" : fullName.trim();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        if (normalizedName.length() < 3) {
            return error(model, normalizedName, normalizedEmail, phone, "Escribe tu nombre completo.");
        }
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return error(model, normalizedName, normalizedEmail, phone, "Ingresa un correo válido.");
        }
        if (password == null || password.length() < 8) {
            return error(model, normalizedName, normalizedEmail, phone, "La contraseña debe tener al menos 8 caracteres.");
        }
        if (!password.equals(confirmPassword)) {
            return error(model, normalizedName, normalizedEmail, phone, "Las contraseñas no coinciden.");
        }
        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return error(model, normalizedName, normalizedEmail, phone, "Ya existe una cuenta con este correo. Inicia sesión.");
        }

        UserAccount account = new UserAccount();
        account.setFullName(normalizedName);
        account.setEmail(normalizedEmail);
        account.setPhone(phone == null ? "" : phone.trim());
        account.setCredentialHash(passwordEncoder.encode(password));
        account.setRole(Role.CLIENT);
        account.setActive(true);
        userAccountRepository.save(account);

        return "redirect:/login?registered";
    }

    private String error(Model model, String fullName, String email, String phone, String error) {
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone == null ? "" : phone);
        model.addAttribute("error", error);
        return "auth/register";
    }
}
