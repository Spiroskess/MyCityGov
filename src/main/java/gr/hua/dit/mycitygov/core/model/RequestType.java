package gr.hua.dit.mycitygov.core.model;

import jakarta.persistence.*;

@Entity
@Table(name = "request_types")
public class RequestType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String label;

    @Column(length = 2000)
    private String description;

    public Long getId() { return id; }

    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
