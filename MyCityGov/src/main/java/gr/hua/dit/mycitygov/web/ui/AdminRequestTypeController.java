package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.service.RequestTypeService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.service.model.RequestTypeView;
import gr.hua.dit.mycitygov.web.ui.model.RequestTypeForm;
import gr.hua.dit.mycitygov.web.ui.model.RequestTypeMappingForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/admin/request-types")
public class AdminRequestTypeController {

    private final RequestTypeService requestTypeService;

    public AdminRequestTypeController(RequestTypeService requestTypeService) {
        this.requestTypeService = requestTypeService;
    }

    // ===== LIST =====

    @GetMapping
    public String list(@RequestParam(name = "ok", required = false) String ok,
                       @RequestParam(name = "error", required = false) String error,
                       Model model) {

        model.addAttribute("types", requestTypeService.listAll());

        if (ok != null) {
            model.addAttribute("success", switch (ok) {
                case "created" -> "Ο τύπος αιτήματος δημιουργήθηκε.";
                case "updated" -> "Οι αλλαγές αποθηκεύτηκαν.";
                case "mapping" -> "Η αντιστοίχιση υπηρεσίας αποθηκεύτηκε.";
                default -> "Επιτυχία.";
            });
        }

        if (error != null) {
            model.addAttribute("error", error);
        }

        return "admin/request-types";
    }

    // ===== CREATE =====

    @GetMapping("/new")
    public String showCreate(Model model) {
        model.addAttribute("isNew", true);
        model.addAttribute("form", new RequestTypeForm());
        return "admin/request-type-edit";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") RequestTypeForm form,
                         BindingResult bindingResult,
                         Model model) {

        model.addAttribute("isNew", true);

        if (bindingResult.hasErrors()) {
            return "admin/request-type-edit";
        }

        try {
            requestTypeService.create(
                form.getCode().trim(),
                form.getTitle().trim(),
                form.getSlaDays(),
                form.isEnabled()
            );
            return "redirect:/admin/request-types?ok=created";
        } catch (Exception ex) {
            model.addAttribute("error", humanMessage(ex));
            return "admin/request-type-edit";
        }
    }

    // ===== EDIT =====

    @GetMapping("/{code}/edit")
    public String showEdit(@PathVariable String code, Model model) {
        RequestTypeView t = requestTypeService.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        RequestTypeForm form = new RequestTypeForm();
        form.setCode(t.code());
        form.setTitle(t.title());
        form.setSlaDays(t.slaDays());
        form.setEnabled(t.enabled());

        model.addAttribute("isNew", false);
        model.addAttribute("type", t);
        model.addAttribute("form", form);

        return "admin/request-type-edit";
    }

    @PostMapping("/{code}/edit")
    public String update(@PathVariable String code,
                         @Valid @ModelAttribute("form") RequestTypeForm form,
                         BindingResult bindingResult,
                         Model model) {

        // ensure code is consistent
        form.setCode(code);

        RequestTypeView t = requestTypeService.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("isNew", false);
        model.addAttribute("type", t);

        if (bindingResult.hasErrors()) {
            return "admin/request-type-edit";
        }

        try {
            requestTypeService.update(
                code,
                form.getTitle().trim(),
                form.getSlaDays(),
                form.isEnabled()
            );
            return "redirect:/admin/request-types?ok=updated";
        } catch (Exception ex) {
            model.addAttribute("error", humanMessage(ex));
            return "admin/request-type-edit";
        }
    }

    // Quick enable/disable from list
    @PostMapping("/{code}/toggle")
    public String toggle(@PathVariable String code,
                         @RequestParam("enabled") boolean enabled) {
        requestTypeService.setEnabled(code, enabled);
        return "redirect:/admin/request-types?ok=updated";
    }

    // ===== MAPPING =====

    @GetMapping("/{code}/mapping")
    public String showMapping(@PathVariable String code, Model model) {
        RequestTypeView t = requestTypeService.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        RequestTypeMappingForm form = new RequestTypeMappingForm();
        form.setMunicipalService(t.defaultService()); // μπορεί να είναι null

        model.addAttribute("type", t);
        model.addAttribute("form", form);
        model.addAttribute("services", MunicipalService.values());
        return "admin/request-type-mapping";
    }

    @PostMapping("/{code}/mapping")
    public String saveMapping(@PathVariable String code,
                              @ModelAttribute("form") RequestTypeMappingForm form,
                              Model model) {
        // ensure exists
        requestTypeService.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        try {
            requestTypeService.setDefaultService(code, form.getMunicipalService());
            return "redirect:/admin/request-types?ok=mapping";
        } catch (Exception ex) {
            model.addAttribute("type", requestTypeService.findByCode(code).orElse(null));
            model.addAttribute("services", MunicipalService.values());
            model.addAttribute("error", humanMessage(ex));
            return "admin/request-type-mapping";
        }
    }

    private String humanMessage(Exception ex) {
        String msg = (ex.getMessage() == null) ? "" : ex.getMessage();

        if (msg.contains("already exists")) {
            return "Υπάρχει ήδη τύπος με αυτόν τον κωδικό.";
        }
        if (msg.contains("CODE_REQUIRED")) return "Ο κωδικός είναι υποχρεωτικός.";
        if (msg.contains("TITLE_REQUIRED")) return "Ο τίτλος είναι υποχρεωτικός.";
        if (msg.contains("SLA_DAYS_MIN_1")) return "Το SLA πρέπει να είναι τουλάχιστον 1 ημέρα.";
        if (msg.contains("UNKNOWN_REQUEST_TYPE")) return "Ο τύπος αιτήματος δεν βρέθηκε.";

        return "Σφάλμα: " + (msg.isBlank() ? ex.getClass().getSimpleName() : msg);
    }
}
