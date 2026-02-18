package com.cartographie.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String acteur; // Email or Username of who performed the action
    private String action; // e.g., "LOGIN", "ROLE_CHANGE", "DELETE_PROJECT"

    @Column(length = 1000)
    private String details; // e.g., "Changed role of user X to ADMIN"

    private LocalDateTime timestamp;

    public AuditLog(String acteur, String action, String details) {
        this.acteur = acteur;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
