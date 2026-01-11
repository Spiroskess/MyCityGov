package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.model.ServiceSchedule;
import gr.hua.dit.mycitygov.core.service.AdminScheduleService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.Map;

@Controller
@RequestMapping("/admin/schedules")
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    public AdminScheduleController(AdminScheduleService adminScheduleService) {
        this.adminScheduleService = adminScheduleService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String ok, Model model) {
        // Admin UI: προβολή + φόρμα διαχείρισης ωραρίων ραντεβού ανά υπηρεσία/ημέρα
        model.addAttribute("schedules", adminScheduleService.findAll());

        if (!model.containsAttribute("schedule")) {
            model.addAttribute("schedule", new ServiceSchedule()); // φόρμα δημιουργίας νέου ωραρίου
        }
        model.addAttribute("services", MunicipalService.values());
        model.addAttribute("days", DayOfWeek.values());

        // Labels για πιο ωραία εμφάνιση στο template
        model.addAttribute("serviceLabels", serviceLabels());
        model.addAttribute("dayLabels", dayLabels());

        if (ok != null) {
            model.addAttribute("success", "Το ωράριο αποθηκεύτηκε.");
        }

        return "admin/schedules";
    }

    @PostMapping
    public String save(@ModelAttribute("schedule") ServiceSchedule schedule, Model model) {
        // Save ωραρίου: το business validation/overlap check γίνεται στο AdminScheduleService
        try {
            adminScheduleService.create(schedule);
            return "redirect:/admin/schedules?ok=1";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());

            // Ξαναγεμίζει τα required data για να ξαναεμφανιστεί σωστά η σελίδα με error
            model.addAttribute("schedules", adminScheduleService.findAll());
            model.addAttribute("services", MunicipalService.values());
            model.addAttribute("days", DayOfWeek.values());
            model.addAttribute("serviceLabels", serviceLabels());
            model.addAttribute("dayLabels", dayLabels());

            return "admin/schedules";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        // Διαγραφή ωραρίου (admin action)
        adminScheduleService.delete(id);
        return "redirect:/admin/schedules?ok=1";
    }

    private Map<MunicipalService, String> serviceLabels() {
        // Map enum -> ελληνική ετικέτα
        return Map.of(
            MunicipalService.KEP, "ΚΕΠ",
            MunicipalService.TECHNICAL_SERVICE, "Τεχνική Υπηρεσία",
            MunicipalService.SOCIAL_SERVICE, "Κοινωνική Υπηρεσία",
            MunicipalService.FINANCIAL_SERVICE, "Οικονομική Υπηρεσία",
            MunicipalService.ENVIRONMENT_SERVICE, "Περιβάλλον / Καθαριότητα"
        );
    }

    private Map<DayOfWeek, String> dayLabels() {
        // Map DayOfWeek -> ελληνική ετικέτα
        return Map.of(
            DayOfWeek.MONDAY, "Δευτέρα",
            DayOfWeek.TUESDAY, "Τρίτη",
            DayOfWeek.WEDNESDAY, "Τετάρτη",
            DayOfWeek.THURSDAY, "Πέμπτη",
            DayOfWeek.FRIDAY, "Παρασκευή",
            DayOfWeek.SATURDAY, "Σάββατο",
            DayOfWeek.SUNDAY, "Κυριακή"
        );
    }
}
