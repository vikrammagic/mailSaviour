package com.mailSaviour30.repositories;

import com.mailSaviour30.entities.TestingIDsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestingIDsRepository extends JpaRepository<TestingIDsEntity, Long> {
    Optional<TestingIDsEntity> findByEmail(String email);
}
