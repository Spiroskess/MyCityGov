package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.security.CurrentUser;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.AppointmentService;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Controller
public class DashboardController {

    private final CurrentUserProvider currentUserProvider;
    private final RequestService requestService;
    private final AppointmentService appointmentService;

    public DashboardController(CurrentUserProvider currentUserProvider,
                               RequestService requestService,
                               AppointmentService appointmentService) {
        this.currentUserProvider = currentUserProvider;
        this.requestService = requestService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/dashboard")
    public String dashboardRoot() {
        // Κοινό entry-point: κάνει redirect σε citizen/employee/admin dashboard ανάλογα με το role
        final CurrentUser me = currentUserProvider.requireCurrentUser();
        return switch (me.role()) {
            case CITIZEN -> "redirect:/citizen/dashboard";
            case EMPLOYEE -> "redirect:/employee/dashboard";
            case ADMIN -> "redirect:/admin/dashboard";
        };
    }

    @GetMapping("/citizen/dashboard")
    public String citizenDashboard(Model model) {
        // Citizen dashboard - counters + πρόσφατα αιτήματα + επερχόμενα ραντεβού
        final Person citizen = currentUserProvider.getCurrentPerson().orElseThrow();

        model.addAttribute("person", citizen);
        model.addAttribute("greeting", greeting());

        final List<RequestView> requests = requestService.getRequestsOfCitizen(citizen);

        long activeRequests = requests.stream()
            .filter(r -> r.status() != RequestStatus.COMPLETED && r.status() != RequestStatus.REJECTED)
            .count();
        long completedRequests = requests.stream()
            .filter(r -> r.status() == RequestStatus.COMPLETED)
            .count();
        long overdueRequests = requests.stream().filter(RequestView::overdue).count();

        model.addAttribute("activeRequestsCount", activeRequests);
        model.addAttribute("completedRequestsCount", completedRequests);
        model.addAttribute("overdueRequestsCount", overdueRequests);

        model.addAttribute("recentRequests",
            requests.stream()
                .sorted(Comparator.comparing(RequestView::createdAt).reversed())
                .limit(6)
                .toList()
        );

        final List<Appointment> activeAppointments = appointmentService.listActiveForCitizen(citizen.getId());
        model.addAttribute("activeAppointmentsCount", activeAppointments.size());

        model.addAttribute("upcomingAppointments",
            activeAppointments.stream()
                .filter(a -> a.getAppointmentDateTime() != null)
                .sorted(Comparator.comparing(Appointment::getAppointmentDateTime))
                .limit(5)
                .toList()
        );

        return "citizen/dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(Model model) {
        // Employee dashboard- δικά μου αιτήματα + ουρά υπηρεσίας + ενεργά ραντεβού
        final Person employee = currentUserProvider.getCurrentPerson().orElseThrow();

        model.addAttribute("person", employee);
        model.addAttribute("greeting", greeting());

        final MunicipalService service = employee.getMunicipalService();
        model.addAttribute("service", service);

        final List<RequestView> myRequests = requestService.getRequestsAssignedToEmployee(employee);
        long myOpen = myRequests.stream()
            .filter(r -> r.status() != RequestStatus.COMPLETED && r.status() != RequestStatus.REJECTED)
            .count();
        long myOverdue = myRequests.stream().filter(RequestView::overdue).count();

        model.addAttribute("myOpenRequestsCount", myOpen);
        model.addAttribute("myOverdueRequestsCount", myOverdue);

        model.addAttribute("recentMyRequests",
            myRequests.stream()
                .sorted(Comparator.comparing(RequestView::createdAt).reversed())
                .limit(6)
                .toList()
        );

        if (service != null) {
            List<RequestView> serviceQueue = requestService.getServiceQueue(service);
            model.addAttribute("serviceQueueCount", serviceQueue.size());
            model.addAttribute("serviceOverdueCount", serviceQueue.stream().filter(RequestView::overdue).count());
            model.addAttribute("topServiceQueue",
                serviceQueue.stream()
                    .sorted(Comparator.comparing(RequestView::createdAt).reversed())
                    .limit(6)
                    .toList()
            );
        } else {
            model.addAttribute("serviceQueueCount", 0);
            model.addAttribute("serviceOverdueCount", 0);
            model.addAttribute("topServiceQueue", List.of());
        }

        List<Appointment> employeeAppointments = appointmentService.listForEmployee(employee.getId());
        long activeAppointments = employeeAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.REQUESTED || a.getStatus() == AppointmentStatus.CONFIRMED)
            .count();
        model.addAttribute("activeAppointmentsCount", activeAppointments);

        model.addAttribute("upcomingAppointments",
            employeeAppointments.stream()
                .filter(a -> a.getAppointmentDateTime() != null)
                .filter(a -> a.getStatus() == AppointmentStatus.REQUESTED || a.getStatus() == AppointmentStatus.CONFIRMED)
                .sorted(Comparator.comparing(Appointment::getAppointmentDateTime))
                .limit(5)
                .toList()
        );

        return "employee/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        // Admin dashboard- συνολικά αιτήματα (assigned/unassigned) + SLA overdue + ραντεβού
        final Person admin = currentUserProvider.getCurrentPerson().orElseThrow();

        model.addAttribute("person", admin);
        model.addAttribute("greeting", greeting());

        final List<RequestView> allRequests = requestService.getAllRequests();
        final List<RequestView> unassignedRequests = requestService.getUnassignedRequests();
        final List<RequestView> assignedRequests = requestService.getAssignedRequests();

        model.addAttribute("allRequestsCount", allRequests.size());
        model.addAttribute("unassignedRequestsCount", unassignedRequests.size());
        model.addAttribute("assignedRequestsCount", assignedRequests.size());
        model.addAttribute("overdueRequestsCount", allRequests.stream().filter(RequestView::overdue).count());

        model.addAttribute("recentRequests",
            allRequests.stream()
                .sorted(Comparator.comparing(RequestView::createdAt).reversed())
                .limit(8)
                .toList()
        );

        List<Appointment> allAppointments = appointmentService.listForAdmin();
        long activeAppointments = allAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.REQUESTED || a.getStatus() == AppointmentStatus.CONFIRMED)
            .count();

        model.addAttribute("allAppointmentsCount", allAppointments.size());
        model.addAttribute("activeAppointmentsCount", activeAppointments);

        model.addAttribute("upcomingAppointments",
            allAppointments.stream()
                .filter(a -> a.getAppointmentDateTime() != null)
                .filter(a -> a.getStatus() == AppointmentStatus.REQUESTED || a.getStatus() == AppointmentStatus.CONFIRMED)
                .sorted(Comparator.comparing(Appointment::getAppointmentDateTime))
                .limit(6)
                .toList()
        );

        return "admin/dashboard";
    }

    private static String greeting() {
        // Μικρό helper για greeting ανά ώρα
        try {
            LocalTime now = LocalTime.now(ZoneId.of("Europe/Athens"));
            int h = now.getHour();
            if (h < 12) return "Καλημέρα";
            if (h < 18) return "Καλό μεσημέρι";
            return "Καλησπέρα";
        } catch (Exception e) {
            return "Καλωσόρισες";
        }
    }
}
