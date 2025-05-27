package com.example.qrcodegenerator.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index; // For explicit index creation if needed

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "url_entries", indexes = {
    @Index(name = "idx_short_code", columnList = "shortCode", unique = true),
    @Index(name = "idx_original_url", columnList = "originalUrl", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048) // originalUrl is already indexed by @Table
    private String originalUrl;

    @Column(nullable = false, length = 10) // shortCode is already indexed by @Table
    private String shortCode;

    // Constructors, getters, and setters are handled by Lombok
}
