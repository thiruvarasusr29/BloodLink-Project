package com.example.bloodlink.repository;

import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonorRepository extends JpaRepository<Donor, Long> {

    List<Donor> findByBloodGroupAndCityAndAvailableTrue(
            BloodGroup bloodGroup,
            String city
    );
}