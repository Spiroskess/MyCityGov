package gr.hua.dit.mycitygov.core.model;

/**
 * Κατάσταση αιτήματος με ελληνικό label + CSS class για εμφάνιση στο UI
 */
public enum RequestStatus {

    SUBMITTED("Υποβλήθηκε", "mc-badge-info"),
    RECEIVED("Παραλήφθηκε", "mc-badge-info"),
    IN_PROGRESS("Σε εξέλιξη", "mc-badge-warn"),
    WAITING_ADDITIONAL_INFO("Αναμονή στοιχείων", "mc-badge-neutral"),
    COMPLETED("Ολοκληρώθηκε", "mc-badge-success"),
    REJECTED("Απορρίφθηκε", "mc-badge-danger");

    private final String labelEl;
    private final String badgeClass;

    RequestStatus(String labelEl, String badgeClass) {
        this.labelEl = labelEl;
        this.badgeClass = badgeClass;
    }

    public String label() { return labelEl; }
    public String badgeClass() { return badgeClass; }

    @Override
    public String toString() { return labelEl; }
}
