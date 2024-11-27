package com.buaa01.illumineer_backend.service.email;

import com.buaa01.illumineer_backend.entity.Institution;

import java.util.Optional;

public interface InstitutionRepository {
    Optional<Institution> findByDomain(String domain);
}
