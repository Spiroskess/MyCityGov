package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.service.AdminScheduleService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.model.ServiceSchedule;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;

@Controller
@RequestMapping("/admin/schedules")
public class AdminScheduleController {

    private final AdminScheduleService adminScheduleService;

    public AdminScheduleController(AdminScheduleService adminScheduleService) {
        this.adminScheduleService = adminScheduleService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("schedules", adminScheduleService.findAll());

        // inline form (create) στο ίδιο view
        model.addAttribute("schedule", new ServiceSchedule());
        model.addAttribute("services", MunicipalService.values());
        model.addAttribute("days", DayOfWeek.values());

        return "admin/schedules";
    }

    @PostMapping
    public String save(@ModelAttribute("schedule") ServiceSchedule schedule, Model model) {
        try {
            adminScheduleService.create(schedule);
            return "redirect:/admin/schedules";
        } catch (Exception ex) {
            // σωστή εμφάνιση σφαλμάτων UI μένουμε στην ίδια σελίδα
            model.addAttribute("error", ex.getMessage());

            // ξαναγεμίζουμε τη σελίδα
            model.addAttribute("schedules", adminScheduleService.findAll());
            model.addAttribute("services", MunicipalService.values());
            model.addAttribute("days", DayOfWeek.values());

            return "admin/schedules";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        adminScheduleService.delete(id);
        return "redirect:/admin/schedules";
    }
}
