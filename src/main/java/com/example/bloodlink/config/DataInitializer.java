package com.example.bloodlink.config;

import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.entity.DonationRecord;
import com.example.bloodlink.entity.Donor;
import com.example.bloodlink.repository.BloodGroupRepository;
import com.example.bloodlink.repository.DonationRecordRepository;
import com.example.bloodlink.repository.DonorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BloodGroupRepository bloodGroupRepository;
    private final DonorRepository donorRepository;
    private final DonationRecordRepository donationRecordRepository;

    public DataInitializer(BloodGroupRepository bloodGroupRepository,
                           DonorRepository donorRepository,
                           DonationRecordRepository donationRecordRepository) {
        this.bloodGroupRepository = bloodGroupRepository;
        this.donorRepository = donorRepository;
        this.donationRecordRepository = donationRecordRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Initialize Blood Groups
        List<String> defaultGroups = List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        for (String groupName : defaultGroups) {
            if (!bloodGroupRepository.existsByGroupNameIgnoreCase(groupName)) {
                bloodGroupRepository.save(new BloodGroup(groupName));
            }
        }

        // Cache blood groups by name for easy mapping
        Map<String, BloodGroup> bgMap = new HashMap<>();
        bloodGroupRepository.findAll().forEach(bg -> bgMap.put(bg.getGroupName().toUpperCase(), bg));

        // 2. Initialize 18 Sample Donors with diverse locations, blood groups, and eligibility
        if (donorRepository.count() < 15) {
            LocalDate today = LocalDate.now();

            List<DonorSeed> seeds = List.of(
                    // Chennai Donors
                    new DonorSeed("Rahul Sharma", "9840112233", "Chennai", "A+", null, true),
                    new DonorSeed("Ananya Iyer", "9840223344", "Chennai", "O+", today.minusDays(110), true),
                    new DonorSeed("Karthik Subramanian", "9840334455", "Chennai", "B+", today.minusDays(25), false),
                    new DonorSeed("Divya Krishnan", "9840445566", "Chennai", "AB+", today.minusDays(150), true),
                    new DonorSeed("Mohammed Riyaz", "9840556677", "Chennai", "O-", null, true),
                    new DonorSeed("Sneha Patel", "9840667788", "Chennai", "A-", today.minusDays(40), false),

                    // Coimbatore Donors
                    new DonorSeed("Arvind Swaminathan", "9841122334", "Coimbatore", "B+", null, true),
                    new DonorSeed("Pooja Nair", "9841233445", "Coimbatore", "O+", today.minusDays(95), true),
                    new DonorSeed("Vikramaditya Rao", "9841344556", "Coimbatore", "A+", today.minusDays(30), false),
                    new DonorSeed("Meera Sundaram", "9841455667", "Coimbatore", "AB-", null, true),

                    // Madurai Donors
                    new DonorSeed("Harish Kumar", "9842122334", "Madurai", "O+", null, true),
                    new DonorSeed("Deepa Venkat", "9842233445", "Madurai", "A+", today.minusDays(120), true),
                    new DonorSeed("Suresh Babu", "9842344556", "Madurai", "B-", today.minusDays(15), false),

                    // Salem Donors
                    new DonorSeed("Nandini Reddy", "9843122334", "Salem", "B+", null, true),
                    new DonorSeed("Vigneshwaran S", "9843233445", "Salem", "A-", today.minusDays(105), true),

                    // Trichy Donors
                    new DonorSeed("Kousalya R", "9844122334", "Trichy", "AB+", null, true),
                    new DonorSeed("Naveen Prasad", "9844233445", "Trichy", "O-", today.minusDays(130), true),
                    new DonorSeed("Swetha Ramesh", "9844344556", "Trichy", "B+", today.minusDays(60), false)
            );

            for (DonorSeed seed : seeds) {
                BloodGroup bg = bgMap.get(seed.bloodGroupName);
                if (bg != null) {
                    Donor donor = new Donor();
                    donor.setName(seed.name);
                    donor.setPhone(seed.phone);
                    donor.setCity(seed.city);
                    donor.setBloodGroup(bg);
                    donor.setLastDonationDate(seed.lastDonationDate);
                    donor.setAvailable(seed.available);

                    Donor savedDonor = donorRepository.save(donor);

                    // If donor has a previous donation date, log a donation record history
                    if (seed.lastDonationDate != null) {
                        DonationRecord record = new DonationRecord(seed.lastDonationDate, savedDonor);
                        donationRecordRepository.save(record);
                    }
                }
            }
        }
    }

    private static class DonorSeed {
        String name;
        String phone;
        String city;
        String bloodGroupName;
        LocalDate lastDonationDate;
        boolean available;

        DonorSeed(String name, String phone, String city, String bloodGroupName, LocalDate lastDonationDate, boolean available) {
            this.name = name;
            this.phone = phone;
            this.city = city;
            this.bloodGroupName = bloodGroupName;
            this.lastDonationDate = lastDonationDate;
            this.available = available;
        }
    }
}
