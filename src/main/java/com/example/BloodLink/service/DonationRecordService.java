package com.example.bloodlink.service;

import com.example.bloodlink.entity.DonationRecord;
import com.example.bloodlink.entity.Donor;
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

        Donor donor = donationRecord.getDonor();

        LocalDate donationDate = donationRecord.getDonationDate();

        // If donation date is not provided, use today's date
        if (donationDate == null) {
            donationDate = LocalDate.now();
            donationRecord.setDonationDate(donationDate);
        }

        // Update donor information
        donor.setLastDonationDate(donationDate);

        // Donor becomes unavailable after donation
        donor.setAvailable(false);

        // Save updated donor
        donorRepository.save(donor);

        // Save donation record
        return donationRecordRepository.save(donationRecord);
    }

    // Get all donation records
    public List<DonationRecord> getAllDonationRecords() {
        return donationRecordRepository.findAll();
    }
}