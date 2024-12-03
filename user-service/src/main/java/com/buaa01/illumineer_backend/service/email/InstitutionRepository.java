package com.buaa01.illumineer_backend.service.email;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import com.buaa01.illumineer_backend.entity.Institution;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByDomain(String domain);
}
