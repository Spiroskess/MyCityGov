package gr.hua.dit.mycitygov.core.model;

/**
 * Κατάσταση αιτήματος.
 *
 * Περιλαμβάνει ελληνικές ετικέτες και UI κλάσεις ώστε να μην εμφανίζονται
 * αγγλικές τιμές (π.χ. "SUBMITTED") στα templates.
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

    /** Ελληνική ετικέτα για εμφάνιση στο UI. */
    public String label() {
        return labelEl;
    }

    /**
     * CSS class για το badge (π.χ. mc-badge-info).
     */
    public String badgeClass() {
        return badgeClass;
    }

    @Override
    public String toString() {
        return labelEl;
    }
}
