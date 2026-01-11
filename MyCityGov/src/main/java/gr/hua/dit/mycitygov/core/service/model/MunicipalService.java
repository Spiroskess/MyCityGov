package gr.hua.dit.mycitygov.core.service.model;

public enum MunicipalService {
    // Δημοτικές υπηρεσίες
    KEP("ΚΕΠ"),
    TECHNICAL_SERVICE("Τεχνική Υπηρεσία"),
    SOCIAL_SERVICE("Κοινωνική Υπηρεσία"),
    FINANCIAL_SERVICE("Οικονομική Υπηρεσία"),
    ENVIRONMENT_SERVICE("Υπηρεσία Περιβάλλοντος/Καθαριότητας");

    private final String labelEl;

    MunicipalService(String labelEl) {
        this.labelEl = labelEl;
    }

    public String label() {
        return labelEl;
    }

    @Override
    public String toString() {
        return labelEl;
    }
}
