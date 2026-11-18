package com.ark.funds.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "investors", uniqueConstraints = @UniqueConstraint(name = "uk_investor_email", columnNames = "email"))
public class Investor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false, length = 320)
    private String email;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Investor() {}
    public Investor(String name, String email) { this.name = name; this.email = email; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Instant getCreatedAt() { return createdAt; }
}
