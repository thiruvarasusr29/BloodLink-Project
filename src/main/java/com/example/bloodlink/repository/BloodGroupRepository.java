package com.example.bloodlink.repository;

import com.example.bloodlink.entity.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BloodGroupRepository extends JpaRepository<BloodGroup, Long> {
    boolean existsByGroupNameIgnoreCase(String groupName);
    Optional<BloodGroup> findByGroupNameIgnoreCase(String groupName);
}