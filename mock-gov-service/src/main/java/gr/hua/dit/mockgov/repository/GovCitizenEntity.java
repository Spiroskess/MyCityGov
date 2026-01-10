package gr.hua.dit.mockgov.repository;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "GOV_CITIZENS",
        uniqueConstraints = @UniqueConstraint(name = "UK_GOV_CITIZEN_AFM_AMKA", columnNames = {"afm", "amka"})
)
public class GovCitizenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 9)
    private String afm;

    @Column(nullable = false, length = 11)
    private String amka;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected GovCitizenEntity() {}

    public GovCitizenEntity(String afm, String amka, String firstName, String lastName) {
        this.afm = afm;
        this.amka = amka;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getAfm() { return afm; }
    public String getAmka() { return amka; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Instant getCreatedAt() { return createdAt; }

    public void setAfm(String afm) { this.afm = afm; }
    public void setAmka(String amka) { this.amka = amka; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
