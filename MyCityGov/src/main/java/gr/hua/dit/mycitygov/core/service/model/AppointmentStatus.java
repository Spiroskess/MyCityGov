package gr.hua.dit.mycitygov.core.service.model;

public enum AppointmentStatus {
    // Κατάσταση ραντεβού με ελληνικό label
    REQUESTED("Σε αναμονή", "mc-badge-warn"),
    CONFIRMED("Επιβεβαιωμένο", "mc-badge-info"),
    CANCELLED("Ακυρώθηκε", "mc-badge-danger"),
    COMPLETED("Ολοκληρώθηκε", "mc-badge-success");

    private final String labelEl;
    private final String badgeClass;

    AppointmentStatus(String labelEl, String badgeClass) {
        this.labelEl = labelEl;
        this.badgeClass = badgeClass;
    }

    public String label() {
        return labelEl;
    }

    public String badgeClass() {
        return badgeClass;
    }

    @Override
    public String toString() {
        return labelEl;
    }
}
