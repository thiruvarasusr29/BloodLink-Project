package com.example.bloodlink.service;

import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.repository.BloodGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BloodGroupService {

    private final BloodGroupRepository bloodGroupRepository;

    public BloodGroupService(BloodGroupRepository bloodGroupRepository) {
        this.bloodGroupRepository = bloodGroupRepository;
    }

    public BloodGroup saveBloodGroup(BloodGroup bloodGroup) {
        return bloodGroupRepository.save(bloodGroup);
    }

    public List<BloodGroup> getAllBloodGroups() {
        return bloodGroupRepository.findAll();
    }
}