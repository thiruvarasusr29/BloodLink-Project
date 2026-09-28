package com.example.bloodlink.controller;

import com.example.bloodlink.entity.Donor;
import com.example.bloodlink.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    // Register a new donor
    @PostMapping
    public ResponseEntity<Donor> registerDonor(@Valid @RequestBody Donor donor) {
        Donor savedDonor = donorService.registerDonor(donor);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDonor);
    }

    // Get all donors
    @GetMapping
    public ResponseEntity<List<Donor>> getAllDonors() {
        return ResponseEntity.ok(donorService.getAllDonors());
    }

    // Get donor by ID
    @GetMapping("/{id}")
    public ResponseEntity<Donor> getDonorById(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.getDonorById(id));
    }

    // Search eligible donors by blood group and city
    @GetMapping("/search")
    public ResponseEntity<List<Donor>> searchDonors(
            @RequestParam Long bloodGroupId,
            @RequestParam String city) {
        List<Donor> eligibleDonors = donorService.searchDonors(bloodGroupId, city);
        return ResponseEntity.ok(eligibleDonors);
    }
}