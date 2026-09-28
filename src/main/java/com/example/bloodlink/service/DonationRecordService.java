package com.example.bloodlink.service;

import com.example.bloodlink.entity.DonationRecord;
import com.example.bloodlink.entity.Donor;
import com.example.bloodlink.exception.BadRequestException;
import com.example.bloodlink.exception.ResourceNotFoundException;
import com.example.bloodlink.repository.DonationRecordRepository;
import com.example.bloodlink.repository.DonorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DonationRecordService {

    private final DonationRecordRepository donationRecordRepository;
    private final DonorRepository donorRepository;

    public DonationRecordService(
            DonationRecordRepository donationRecordRepository,
            DonorRepository donorRepository) {

        this.donationRecordRepository = donationRecordRepository;
        this.donorRepository = donorRepository;
    }

    // Record a new donation
    public DonationRecord recordDonation(DonationRecord donationRecord) {
        if (donationRecord == null || donationRecord.getDonor() == null || donationRecord.getDonor().getId() == null) {
            throw new BadRequestException("Donor ID is required to record a donation");
        }

        Long donorId = donationRecord.getDonor().getId();
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id: " + donorId));

        LocalDate donationDate = donationRecord.getDonationDate();

        // If donation date is not provided, use today's date
        if (donationDate == null) {
            donationDate = LocalDate.now();
            donationRecord.setDonationDate(donationDate);
        } else if (donationDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Donation date cannot be in the future");
        }

        // Update donor information
        donor.setLastDonationDate(donationDate);

        // Donor becomes unavailable after donation
        donor.setAvailable(false);

        // Save updated donor
        donorRepository.save(donor);

        // Attach managed donor to donation record
        donationRecord.setDonor(donor);

        // Save donation record
        return donationRecordRepository.save(donationRecord);
    }

    // Get all donation records
    public List<DonationRecord> getAllDonationRecords() {
        return donationRecordRepository.findAll();
    }
}