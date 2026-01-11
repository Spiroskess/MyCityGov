package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.AppointmentService;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Profile UI controller: δείχνει προφίλ και στατιστικά ανά ρόλο (citizen/employee/admin).
 */
@Controller
public class ProfileController {

    private final CurrentUserProvider currentUserProvider;
    private final RequestService requestService;
    private final AppointmentService appointmentService;

    public ProfileController(CurrentUserProvider currentUserProvider,
                             RequestService requestService,
                             AppointmentService appointmentService) {
        this.currentUserProvider = currentUserProvider;
        this.requestService = requestService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        // /profile: φορτώνει τον current Person, κάνει mask σε ΑΦΜ/ΑΜΚΑ και φορτώνει stats ανά ρόλο
        final Person person = currentUserProvider.getCurrentPerson().orElse(null);
        if (person == null) {
            return "profile"; // fallback αν γίνει access χωρίς login
        }

        model.addAttribute("person", person);
        model.addAttribute("maskedAfm", maskRight(person.getAfm(), 3));
        model.addAttribute("maskedAmka", maskRight(person.getAmka(), 3));
        model.addAttribute("createdAtText", formatCreatedAt(person));

        if (person.getRole() == PersonRole.CITIZEN) {
            enrichCitizen(model, person);
            return "citizen/profile";
        }
        if (person.getRole() == PersonRole.EMPLOYEE) {
            enrichEmployee(model, person);
            return "employee/profile";
        }
        if (person.getRole() == PersonRole.ADMIN) {
            enrichAdmin(model, person);
            return "admin/profile";
        }

        return "profile";
    }

    private void enrichCitizen(Model model, Person citizen) {
        // Υπολογισμός counters για citizen profile
        final var requests = requestService.getRequestsOfCitizen(citizen);

        long activeRequests = requests.stream()
            .filter(r -> r.status() != RequestStatus.COMPLETED && r.status() != RequestStatus.REJECTED)
            .count();
        long completedRequests = requests.stream()
            .filter(r -> r.status() == RequestStatus.COMPLETED)
            .count();
        long rejectedRequests = requests.stream()
            .filter(r -> r.status() == RequestStatus.REJECTED)
            .count();

        final var appointments = appointmentService.listForCitizen(citizen.getId());
        long upcomingAppointments = appointments.stream()
            .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.COMPLETED)
            .count();

        model.addAttribute("activeRequestsCount", activeRequests);
        model.addAttribute("completedRequestsCount", completedRequests);
        model.addAttribute("rejectedRequestsCount", rejectedRequests);
        model.addAttribute("upcomingAppointmentsCount", upcomingAppointments);
    }

    private void enrichEmployee(Model model, Person employee) {
        // Counters για employee profile
        final var myRequests = requestService.getRequestsAssignedToEmployee(employee);
        long openAssigned = myRequests.stream()
            .filter(r -> r.status() != RequestStatus.COMPLETED && r.status() != RequestStatus.REJECTED)
            .count();
        long completedAssigned = myRequests.stream()
            .filter(r -> r.status() == RequestStatus.COMPLETED)
            .count();

        final var myAppointments = appointmentService.listForEmployee(employee.getId());
        long pendingAppointments = myAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.REQUESTED)
            .count();
        long upcomingAppointments = myAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.REQUESTED)
            .count();

        model.addAttribute("openAssignedRequestsCount", openAssigned);
        model.addAttribute("completedAssignedRequestsCount", completedAssigned);
        model.addAttribute("pendingAppointmentsCount", pendingAppointments);
        model.addAttribute("upcomingAppointmentsCount", upcomingAppointments);
    }

    private void enrichAdmin(Model model, Person admin) {
        // Admin profile: βασικά totals (requests + appointments)
        long allRequests = requestService.getAllRequests().size();
        long unassignedRequests = requestService.getUnassignedRequests().size();
        long assignedRequests = requestService.getAssignedRequests().size();

        long allAppointments = appointmentService.listForAdmin().size();

        model.addAttribute("allRequestsCount", allRequests);
        model.addAttribute("unassignedRequestsCount", unassignedRequests);
        model.addAttribute("assignedRequestsCount", assignedRequests);
        model.addAttribute("allAppointmentsCount", allAppointments);
    }

    private static String maskRight(String value, int keepRight) {
        // Mask helper (π.χ. ΑΦΜ/ΑΜΚΑ) κρατάει τα τελευταία N ψηφία
        if (value == null) return "-";
        final String v = value.trim();
        if (v.isEmpty()) return "-";
        if (keepRight <= 0) return "••••";
        if (v.length() <= keepRight) return v;
        int maskLen = v.length() - keepRight;
        return "•".repeat(maskLen) + v.substring(v.length() - keepRight);
    }

    private static String formatCreatedAt(Person person) {
        // Format createdAt σε human readable string
        try {
            var zone = ZoneId.of("Europe/Athens");
            var dt = person.getCreatedAt().atZone(zone);
            return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(dt);
        } catch (Exception e) {
            return "-";
        }
    }
}
