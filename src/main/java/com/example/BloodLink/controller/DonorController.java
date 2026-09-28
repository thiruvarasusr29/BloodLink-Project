package com.example.bloodlink.controller;

import com.example.bloodlink.entity.Donor;
import com.example.bloodlink.service.DonorService;
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
    public ResponseEntity<Donor> registerDonor(@RequestBody Donor donor) {
        Donor savedDonor = donorService.registerDonor(donor);
        return ResponseEntity.ok(savedDonor);
    }

    // Get all donors
    @GetMapping
    public ResponseEntity<List<Donor>> getAllDonors() {
        return ResponseEntity.ok(donorService.getAllDonors());
    }
}