package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.model.RequestType;
import gr.hua.dit.mycitygov.core.service.model.ServiceSchedule;
import gr.hua.dit.mycitygov.core.repository.ServiceScheduleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;

@Controller
@RequestMapping("/admin/schedules")
public class AdminScheduleController {

    private final ServiceScheduleRepository repo;

    public AdminScheduleController(ServiceScheduleRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("schedules", repo.findAll());
        return "admin/schedules";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("schedule", new ServiceSchedule());
        model.addAttribute("services", RequestType.values());
        model.addAttribute("days", DayOfWeek.values());
        return "admin/schedule-form";
    }

    @PostMapping
    public String save(@ModelAttribute ServiceSchedule schedule) {
        repo.save(schedule);
        return "redirect:/admin/schedules";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/admin/schedules";
    }
}
