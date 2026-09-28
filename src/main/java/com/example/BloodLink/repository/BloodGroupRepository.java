package com.example.bloodlink.repository;

import com.example.bloodlink.entity.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodGroupRepository extends JpaRepository<BloodGroup, Long> {
}