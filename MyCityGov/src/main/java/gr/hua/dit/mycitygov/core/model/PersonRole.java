package gr.hua.dit.mycitygov.core.model;

public enum PersonRole {
    CITIZEN("Πολίτης", "mc-badge-info"),
    EMPLOYEE("Υπάλληλος", "mc-badge-warn"),
    ADMIN("Διαχειριστής", "mc-badge-success");

    private final String labelEl;
    private final String badgeClass;

    PersonRole(String labelEl, String badgeClass) {
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
