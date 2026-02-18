package com.cartographie.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {
    @Id
    private String cle; // key, e.g., "ANNEE_SCOLAIRE"
    private String valeur; // value, e.g., "2023-2024"
}
