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
        if (userAccountRepository.count() > 0) {
            return "redirect:/login?setupExists";
        }
        return "setup/index";
    }

    @PostMapping("/setup")
    public String createInitialUser(@RequestParam String fullName,
                                    @RequestParam String email,
                                    @RequestParam String password,
                                    Model model) {
        if (userAccountRepository.count() > 0) {
            return "redirect:/login?setupExists";
        }

        if (password == null || password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "setup/index";
        }

        UserAccount account = new UserAccount();
        account.setFullName(fullName);
        account.setEmail(email);
        account.setCredentialHash(passwordEncoder.encode(password));
        account.setRole(Role.DEVELOPER);
        account.setActive(true);

        userAccountRepository.save(account);

        return "redirect:/login?setupSuccess";
    }
}
