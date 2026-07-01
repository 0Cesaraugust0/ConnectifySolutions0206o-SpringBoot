package com.connectify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * La demo pública por roles fue retirada para no mezclar el perfil Developer
 * con bandejas operativas de otros roles. El acceso técnico se concentra en
 * el panel Developer y cada rol usa su propia cuenta y dashboard.
 */
@Controller
public class PrototypeController {

    @GetMapping("/prototype")
    public String prototype() {
        return "redirect:/dashboard/developer";
    }

    @GetMapping("/prototype/{role}")
    public String rolePrototype(@PathVariable String role) {
        return "redirect:/dashboard/developer";
    }
}
