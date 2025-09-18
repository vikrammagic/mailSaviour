package com.mailSaviour30.repositories;

import com.mailSaviour30.entities.EmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<EmailEntity, Long> {
    List<EmailEntity> findAllByBody_Id(Long bodyId);
}
