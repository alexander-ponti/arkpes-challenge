package com.ark.funds.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "funds", uniqueConstraints = @UniqueConstraint(name = "uk_fund_name", columnNames = "name"))
public class Fund {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(length = 2000)
    private String description;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Fund() {}
    public Fund(String name, String description) { this.name = name; this.description = description; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
}
