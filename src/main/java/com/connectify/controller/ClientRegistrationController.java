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
    public String registerForm(@RequestParam(required = false, defaultValue = "client") String type, Model model) {
        model.addAttribute("accountType", normalizeAccountType(type));
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam(required = false, defaultValue = "client") String accountType,
                           @RequestParam String firstName,
                           @RequestParam String paternalLastName,
                           @RequestParam(required = false) String maternalLastName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(required = false) String dni,
                           @RequestParam(required = false) String phone,
                           @RequestParam(required = false) String companyName,
                           @RequestParam(required = false) String ruc,
                           Model model) {
        String normalizedType = normalizeAccountType(accountType);
        String normalizedFirstName = clean(firstName);
        String normalizedPaternalLastName = clean(paternalLastName);
        String normalizedMaternalLastName = clean(maternalLastName);
        String normalizedName = buildFullName(normalizedFirstName, normalizedPaternalLastName, normalizedMaternalLastName);
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String normalizedDni = clean(dni);
        String normalizedPhone = clean(phone);
        String normalizedCompanyName = clean(companyName);
        String normalizedRuc = clean(ruc);

        String validationError = validate(normalizedType, normalizedFirstName, normalizedPaternalLastName,
                normalizedEmail, normalizedCompanyName, normalizedRuc, password, confirmPassword);
        if (validationError != null) {
            return error(model, normalizedType, normalizedFirstName, normalizedPaternalLastName, normalizedMaternalLastName,
                    normalizedEmail, normalizedDni, normalizedPhone, normalizedCompanyName, normalizedRuc, validationError);
        }
        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return error(model, normalizedType, normalizedFirstName, normalizedPaternalLastName, normalizedMaternalLastName,
                    normalizedEmail, normalizedDni, normalizedPhone, normalizedCompanyName, normalizedRuc,
                    "Ya existe una cuenta con este correo. Inicia sesión.");
        }

        UserAccount account = new UserAccount();
        account.setFullName(normalizedName);
        account.setFirstName(normalizedFirstName);
        account.setPaternalLastName(normalizedPaternalLastName);
        account.setMaternalLastName(normalizedMaternalLastName);
        account.setEmail(normalizedEmail);
        account.setDni(normalizedDni);
        account.setPhone(normalizedPhone);
        account.setCompanyName(normalizedCompanyName);
        account.setRuc(normalizedRuc);
        account.setCredentialHash(passwordEncoder.encode(password));
        account.setRole("organizer".equals(normalizedType) ? Role.ORGANIZER : Role.CLIENT);
        account.setActive(true);
        userAccountRepository.save(account);

        return "organizer".equals(normalizedType)
                ? "redirect:/login?organizerRegistered"
                : "redirect:/login?registered";
    }

    private String validate(String accountType,
                            String firstName,
                            String paternalLastName,
                            String email,
                            String companyName,
                            String ruc,
                            String password,
                            String confirmPassword) {
        if (firstName.length() < 2) return "Escribe tus nombres.";
        if (paternalLastName.length() < 2) return "Escribe tu apellido paterno.";
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return "Ingresa un correo válido.";
        if (password == null || password.length() < 8) return "La contraseña debe tener al menos 8 caracteres.";
        if (!password.equals(confirmPassword)) return "Las contraseñas no coinciden.";
        if ("organizer".equals(accountType) && companyName.length() < 3) {
            return "Escribe la razón social o nombre comercial del organizador.";
        }
        if ("organizer".equals(accountType) && !ruc.matches("\\d{11}")) {
            return "El RUC debe contener 11 dígitos.";
        }
        return null;
    }

    private String error(Model model,
                         String accountType,
                         String firstName,
                         String paternalLastName,
                         String maternalLastName,
                         String email,
                         String dni,
                         String phone,
                         String companyName,
                         String ruc,
                         String error) {
        model.addAttribute("accountType", accountType);
        model.addAttribute("firstName", firstName);
        model.addAttribute("paternalLastName", paternalLastName);
        model.addAttribute("maternalLastName", maternalLastName);
        model.addAttribute("email", email);
        model.addAttribute("dni", dni);
        model.addAttribute("phone", phone);
        model.addAttribute("companyName", companyName);
        model.addAttribute("ruc", ruc);
        model.addAttribute("error", error);
        return "auth/register";
    }

    private String normalizeAccountType(String type) {
        return "organizer".equalsIgnoreCase(type) ? "organizer" : "client";
    }

    private String buildFullName(String firstName, String paternalLastName, String maternalLastName) {
        return (firstName + " " + paternalLastName + " " + maternalLastName).trim().replaceAll("\\s+", " ");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
