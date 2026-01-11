package gr.hua.dit.mycitygov.core.model;

import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Η ημερομηνία/ώρα του ραντεβού
    private LocalDateTime appointmentDateTime;

    // Κατάσταση ραντεβού (π.χ. REQUESTED/CONFIRMED/CANCELLED)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.REQUESTED;

    // Δημοτική υπηρεσία που αφορά το ραντεβού (ΚΕΠ/Τεχνική κλπ)
    @Enumerated(EnumType.STRING)
    private MunicipalService service;

    // Αποθηκεύουμε απλά IDs (πολίτης/υπάλληλος) αντί για @ManyToOne
    private Long citizenId;
    private Long employeeId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public MunicipalService getService() { return service; }
    public void setService(MunicipalService service) { this.service = service; }

    public Long getCitizenId() { return citizenId; }
    public void setCitizenId(Long citizenId) { this.citizenId = citizenId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
}
