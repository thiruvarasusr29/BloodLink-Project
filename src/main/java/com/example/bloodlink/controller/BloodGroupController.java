package com.example.bloodlink.controller;

import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.service.BloodGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
            @Valid @RequestBody BloodGroup bloodGroup) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bloodGroupService.saveBloodGroup(bloodGroup));
    }

    // Get all blood groups
    @GetMapping
    public ResponseEntity<List<BloodGroup>> getAllBloodGroups() {

        return ResponseEntity.ok(
                bloodGroupService.getAllBloodGroups());
    }

    // Get blood group by id
    @GetMapping("/{id}")
    public ResponseEntity<BloodGroup> getBloodGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(bloodGroupService.getBloodGroupById(id));
    }
}