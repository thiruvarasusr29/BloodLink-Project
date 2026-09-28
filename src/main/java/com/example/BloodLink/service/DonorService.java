package com.example.bloodlink.service;

import com.example.bloodlink.entity.Donor;
import com.example.bloodlink.repository.DonorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DonorService {

    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    public Donor registerDonor(Donor donor) {
        donor.setAvailable(true);
        return donorRepository.save(donor);
    }

    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    public List<Donor> searchDonors(
            com.example.bloodlink.entity.BloodGroup bloodGroup,
            String city) {

        List<Donor> donors =
                donorRepository.findByBloodGroupAndCityAndAvailableTrue(
                        bloodGroup, city);

        LocalDate today = LocalDate.now();

        return donors.stream()
                .filter(donor -> donor.getLastDonationDate() == null ||
                        !donor.getLastDonationDate()
                                .plusDays(90)
                                .isAfter(today))
                .toList();
    }
}