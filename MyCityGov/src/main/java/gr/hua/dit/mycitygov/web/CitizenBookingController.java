package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.service.AppointmentService;
import gr.hua.dit.mycitygov.core.service.AvailabilityService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/citizen/booking")
public class CitizenBookingController {

    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;

    public CitizenBookingController(AvailabilityService availabilityService,
                                    AppointmentService appointmentService) {
        this.availabilityService = availabilityService;
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public String step1(Model model) {
        // Booking flow  επιλογή δημοτικής υπηρεσίας
        model.addAttribute("services", MunicipalService.values());
        return "citizen/booking-step1";
    }

    @GetMapping("/times")
    public String step2(@RequestParam MunicipalService service,
                        @RequestParam String date,
                        Model model) {
        // Booking flow  εμφανίζει διαθέσιμες ώρες για υπηρεσία + ημερομηνία
        LocalDate selectedDate = LocalDate.parse(date);

        model.addAttribute("service", service);
        model.addAttribute("date", selectedDate);
        model.addAttribute("times", availabilityService.getAvailableTimes(service, selectedDate));

        return "citizen/booking-step2";
    }

    @PostMapping("/confirm")
    public String confirm(Authentication authentication,
                          @RequestParam MunicipalService service,
                          @RequestParam String date,
                          @RequestParam String time,
                          Model model) {
        // Booking confirm: δημιουργεί το ραντεβού
        LocalDate d = LocalDate.parse(date);
        LocalTime t = LocalTime.parse(time);

        try {
            Long citizenId = CurrentUserIds.currentUserId(authentication);
            appointmentService.book(citizenId, service, d, t);
            return "redirect:/citizen/appointments";
        } catch (Exception ex) {
            // Αν αποτύχει , ξαναδείχνει  με μήνυμα λάθους
            model.addAttribute("service", service);
            model.addAttribute("date", d);
            model.addAttribute("times", availabilityService.getAvailableTimes(service, d));
            model.addAttribute("error", ex.getMessage());
            return "citizen/booking-step2";
        }
    }
}
