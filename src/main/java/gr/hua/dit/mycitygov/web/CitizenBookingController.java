package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.service.AvailabilityService;
import gr.hua.dit.mycitygov.core.service.model.Appointment;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.repository.AppointmentRepository;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/citizen/booking")
public class CitizenBookingController {

    private final AvailabilityService availabilityService;
    private final AppointmentRepository appointmentRepo;

    public CitizenBookingController(AvailabilityService availabilityService, AppointmentRepository appointmentRepo) {
        this.availabilityService = availabilityService;
        this.appointmentRepo = appointmentRepo;
    }

    @GetMapping
    public String step1(Model model) {
        model.addAttribute("services", MunicipalService.values());
        return "citizen/booking-step1";
    }

    @GetMapping("/times")
    public String step2(@RequestParam MunicipalService service,
                        @RequestParam String date,
                        Model model) {

        LocalDate selectedDate = LocalDate.parse(date);
        model.addAttribute("service", service);
        model.addAttribute("date", selectedDate);

        model.addAttribute("times", availabilityService.getAvailableTimes(service, selectedDate));
        return "citizen/booking-step2";
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam MunicipalService service,
                          @RequestParam String date,
                          @RequestParam String time) {

        LocalDate d = LocalDate.parse(date);
        LocalTime t = LocalTime.parse(time);

        Appointment a = new Appointment();
        a.setService(service); // ΠΡΟΣΟΧΗ: θέλει αντίστοιχο field στον Appointment
        a.setAppointmentDateTime(LocalDateTime.of(d, t));
        a.setStatus(AppointmentStatus.REQUESTED);

        // TODO: βάλε citizenId από logged-in user (προς το παρόν hardcode)
        a.setCitizenId(1L);

        appointmentRepo.save(a);
        return "redirect:/citizen/appointments";
    }
}
