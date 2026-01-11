package gr.hua.dit.mycitygov.core.model;

import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
public class ServiceSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ωράριο ανά δημοτική υπηρεσία (ΚΕΠ/Τεχνική κλπ)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MunicipalService service;

    // Ημέρα εβδομάδας για την οποία ισχύει το ωράριο
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    // Ώρες λειτουργίας για ραντεβού
    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // Ενεργοποίηση/απενεργοποίηση ωραρίου
    @Column(nullable = false)
    private boolean enabled = true;

    // Διάρκεια slot (π.χ. 15 λεπτά)
    private int slotMinutes = 15;

    public Long getId() { return id; }

    public MunicipalService getService() { return service; }
    public void setService(MunicipalService service) { this.service = service; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public int getSlotMinutes() { return slotMinutes; }
    public void setSlotMinutes(int slotMinutes) { this.slotMinutes = slotMinutes; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
