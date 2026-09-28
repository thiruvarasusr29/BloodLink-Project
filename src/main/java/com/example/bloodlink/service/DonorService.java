package com.example.bloodlink.service;

import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.entity.Donor;
import com.example.bloodlink.exception.BadRequestException;
import com.example.bloodlink.exception.ResourceNotFoundException;
import com.example.bloodlink.repository.BloodGroupRepository;
import com.example.bloodlink.repository.DonorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DonorService {

    private final DonorRepository donorRepository;
    private final BloodGroupRepository bloodGroupRepository;

    public DonorService(DonorRepository donorRepository, BloodGroupRepository bloodGroupRepository) {
        this.donorRepository = donorRepository;
        this.bloodGroupRepository = bloodGroupRepository;
    }

    public Donor registerDonor(Donor donor) {
        if (donor == null) {
            throw new BadRequestException("Donor details cannot be null");
        }

        if (donor.getBloodGroup() == null || donor.getBloodGroup().getId() == null) {
            throw new BadRequestException("Valid blood group ID is required");
        }

        BloodGroup bloodGroup = bloodGroupRepository.findById(donor.getBloodGroup().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found with id: " + donor.getBloodGroup().getId()));

        donor.setBloodGroup(bloodGroup);
        donor.setAvailable(true);
        return donorRepository.save(donor);
    }

    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    public Donor getDonorById(Long id) {
        return donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id: " + id));
    }

    public List<Donor> searchDonors(Long bloodGroupId, String city) {
        if (bloodGroupId == null) {
            throw new BadRequestException("Blood group ID is required");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new BadRequestException("City is required");
        }

        BloodGroup bloodGroup = bloodGroupRepository.findById(bloodGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found with id: " + bloodGroupId));

        return searchDonors(bloodGroup, city.trim());
    }

    public List<Donor> searchDonors(
            com.example.bloodlink.entity.BloodGroup bloodGroup,
            String city) {

        List<Donor> donors =
                donorRepository.findByBloodGroupAndCityIgnoreCaseAndAvailableTrue(
                        bloodGroup, city);

        // Fallback to exact match if repository method was changed or empty
        if (donors.isEmpty()) {
            donors = donorRepository.findByBloodGroupAndCityAndAvailableTrue(bloodGroup, city);
        }

        LocalDate today = LocalDate.now();

        return donors.stream()
                .filter(donor -> donor.getLastDonationDate() == null ||
                        !donor.getLastDonationDate()
                                .plusDays(90)
                                .isAfter(today))
                .toList();
    }
}