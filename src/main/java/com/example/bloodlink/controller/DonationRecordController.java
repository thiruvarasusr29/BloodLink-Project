package com.example.bloodlink.controller;

import com.example.bloodlink.entity.DonationRecord;
import com.example.bloodlink.service.DonationRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationRecordController {

    private final DonationRecordService donationRecordService;

    public DonationRecordController(
            DonationRecordService donationRecordService) {

        this.donationRecordService = donationRecordService;
    }

    // Record a donation
    @PostMapping
    public ResponseEntity<DonationRecord> recordDonation(
            @Valid @RequestBody DonationRecord donationRecord) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(donationRecordService.recordDonation(donationRecord));
    }

    // Get all donation records
    @GetMapping
    public ResponseEntity<List<DonationRecord>> getAllDonationRecords() {

        return ResponseEntity.ok(
                donationRecordService.getAllDonationRecords());
    }
}