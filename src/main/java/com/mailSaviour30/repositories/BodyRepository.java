package com.mailSaviour30.repositories;

import com.mailSaviour30.entities.BodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BodyRepository extends JpaRepository<BodyEntity, Long> {
    Optional<BodyEntity> findByBody(String body);
}
