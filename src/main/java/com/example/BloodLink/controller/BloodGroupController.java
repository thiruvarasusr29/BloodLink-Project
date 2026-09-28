package com.example.bloodlink.controller;

import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.service.BloodGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blood-groups")
public class BloodGroupController {

    private final BloodGroupService bloodGroupService;

    public BloodGroupController(BloodGroupService bloodGroupService) {
        this.bloodGroupService = bloodGroupService;
    }

    // Add a blood group
    @PostMapping
    public ResponseEntity<BloodGroup> saveBloodGroup(
            @RequestBody BloodGroup bloodGroup) {

        return ResponseEntity.ok(
                bloodGroupService.saveBloodGroup(bloodGroup));
    }

    // Get all blood groups
    @GetMapping
    public ResponseEntity<List<BloodGroup>> getAllBloodGroups() {

        return ResponseEntity.ok(
                bloodGroupService.getAllBloodGroups());
    }
}