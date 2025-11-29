package gr.hua.dit.mycitygov.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(
    name = "persons",
    indexes = {
        @Index(name = "idx_person_email", columnList = "email_address", unique = true),
        @Index(name = "idx_person_afm", columnList = "afm", unique = true),
        @Index(name = "idx_person_amka", columnList = "amka", unique = true)
    }
)
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 16)
    private PersonRole role;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Email
    @Column(name = "email_address", nullable = false, length = 100, unique = true)
    private String emailAddress;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotNull
    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotNull
    @NotBlank
    @Size(max = 18)
    @Column(name = "mobile_phone_number", nullable = false, length = 18)
    private String mobilePhoneNumber; // E164

    @NotNull
    @NotBlank
    @Size(max = 11)
    @Column(name = "afm", nullable = false, length = 11, unique = true)
    private String afm;

    @NotNull
    @NotBlank
    @Size(max = 11)
    @Column(name = "amka", nullable = false, length = 11, unique = true)
    private String amka;

    @NotNull
    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Person(){
    }
    public Person(Long id,
                  PersonRole role,
                  String emailAddress,
                  String firstName,
                  String lastName,
                  String mobilePhoneNumber,
                  String afm, String amka,
                  String passwordHash,
                  Instant createdAt) {
        this.id = id;
        this.role = role;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.afm = afm;
        this.amka = amka;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonRole getRole() {
        return role;
    }

    public void setRole(PersonRole role) {
        this.role = role;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public String getAfm() {
        return afm;
    }

    public void setAfm(String afm) {
        this.afm = afm;
    }

    public String getAmka() {
        return amka;
    }

    public void setAmka(String amka) {
        this.amka = amka;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Person{" +
            ", emailAddress='" + emailAddress + '\'' +
            ", lastName='" + lastName + '\'' +
            ", mobilePhoneNumber='" + mobilePhoneNumber + '\'' +
            ", afm='" + afm + '\'' +
            ", amka='" + amka + '\'';
    }
}
