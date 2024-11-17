package com.buaa01.illumineer_backend.service.email;
import com.buaa01.illumineer_backend.entity.Institution;
//import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionRepository {
    Optional<Institution> findByDomain(String domain);
}
