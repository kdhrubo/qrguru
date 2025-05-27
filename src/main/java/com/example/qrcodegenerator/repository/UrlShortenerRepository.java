package com.example.qrcodegenerator.repository;

import com.example.qrcodegenerator.model.UrlEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlShortenerRepository extends JpaRepository<UrlEntry, Long> {

    Optional<UrlEntry> findByShortCode(String shortCode);

    Optional<UrlEntry> findByOriginalUrl(String originalUrl);
}
