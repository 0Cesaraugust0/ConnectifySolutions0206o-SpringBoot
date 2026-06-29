package com.connectify.controller;

import com.connectify.entity.Role;
import com.connectify.entity.UserAccount;
import com.connectify.repository.UserAccountRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Controller
@RequestMapping("/dashboard/developer/accounts")
public class DeveloperAccountController {

    private static final Set<Role> MANAGED_ROLES = Set.of(Role.ADMIN, Role.ORGANIZER, Role.DESIGNER, Role.GATE_AGENT);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public DeveloperAccountController(UserAccountRepository userAccountRepository,
                                      PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String accounts(Model model, Authentication authentication) {
        populateModel(model, authentication, "", "", "", "", Role.ADMIN, null);
        return "dashboard/developer/accounts";
    }

    @PostMapping
    public String create(@RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam String dni,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam Role role,
                         Model model,
                         Authentication authentication) {
        String normalizedFirstName = clean(firstName);
        String normalizedLastName = clean(lastName);
        String normalizedDni = clean(dni);
        String normalizedEmail = clean(email).toLowerCase(Locale.ROOT);

        String error = validate(normalizedFirstName, normalizedLastName, normalizedDni, normalizedEmail, password, role);
        if (error != null) {
            populateModel(model, authentication, normalizedFirstName, normalizedLastName, normalizedDni, normalizedEmail, role, error);
            return "dashboard/developer/accounts";
        }
        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            populateModel(model, authentication, normalizedFirstName, normalizedLastName, normalizedDni, normalizedEmail, role,
                    "Ya existe una cuenta con este correo.");
            return "dashboard/developer/accounts";
        }

        UserAccount account = new UserAccount();
        account.setFullName(normalizedFirstName + " " + normalizedLastName);
        account.setDni(normalizedDni);
        account.setEmail(normalizedEmail);
        account.setCredentialHash(passwordEncoder.encode(password));
        account.setRole(role);
        account.setActive(true);
        userAccountRepository.save(account);

        return "redirect:/dashboard/developer/accounts?created=" + role.name();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        UserAccount account = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada."));

        if (!MANAGED_ROLES.contains(account.getRole())) {
            throw new AccessDeniedException("Solo se pueden eliminar cuentas operativas.");
        }
        if (authentication != null && account.getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("No puedes eliminar la cuenta con la que iniciaste sesión.");
        }

        userAccountRepository.delete(account);
        return "redirect:/dashboard/developer/accounts?deleted=true";
    }

    private void populateModel(Model model,
                               Authentication authentication,
                               String firstName,
                               String lastName,
                               String dni,
                               String email,
                               Role selectedRole,
                               String error) {
        List<UserAccount> accounts = userAccountRepository.findAll().stream()
                .filter(account -> MANAGED_ROLES.contains(account.getRole()))
                .sorted(Comparator.comparing(UserAccount::getRole).thenComparing(UserAccount::getFullName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        model.addAttribute("accounts", accounts);
        model.addAttribute("accountCount", accounts.size());
        model.addAttribute("developerEmail", authentication == null ? "" : authentication.getName());
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("dni", dni);
        model.addAttribute("email", email);
        model.addAttribute("selectedRole", selectedRole == null ? Role.ADMIN : selectedRole);
        model.addAttribute("error", error);
    }

    private String validate(String firstName, String lastName, String dni, String email, String password, Role role) {
        if (firstName.length() < 2) return "Escribe los nombres de la persona.";
        if (lastName.length() < 2) return "Escribe los apellidos de la persona.";
        if (!dni.matches("\\d{8}")) return "El DNI debe contener exactamente 8 dígitos.";
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return "Ingresa un correo válido.";
        if (password == null || password.length() < 8) return "La contraseña debe tener al menos 8 caracteres.";
        if (role == null || !MANAGED_ROLES.contains(role)) return "Selecciona un rol operativo válido.";
        return null;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
