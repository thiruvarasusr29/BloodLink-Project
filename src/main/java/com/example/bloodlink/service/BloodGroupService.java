package com.example.bloodlink.service;

import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.exception.BadRequestException;
import com.example.bloodlink.exception.DuplicateResourceException;
import com.example.bloodlink.exception.ResourceNotFoundException;
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
        if (bloodGroup == null || bloodGroup.getGroupName() == null || bloodGroup.getGroupName().trim().isEmpty()) {
            throw new BadRequestException("Blood group name cannot be empty");
        }

        String groupName = bloodGroup.getGroupName().trim().toUpperCase();
        if (bloodGroupRepository.existsByGroupNameIgnoreCase(groupName)) {
            throw new DuplicateResourceException("Blood group '" + groupName + "' already exists");
        }

        bloodGroup.setGroupName(groupName);
        return bloodGroupRepository.save(bloodGroup);
    }

    public List<BloodGroup> getAllBloodGroups() {
        return bloodGroupRepository.findAll();
    }

    public BloodGroup getBloodGroupById(Long id) {
        return bloodGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found with id: " + id));
    }
}