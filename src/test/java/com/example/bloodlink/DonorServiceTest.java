package com.example.bloodlink;

import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.entity.Donor;
import com.example.bloodlink.exception.BadRequestException;
import com.example.bloodlink.exception.ResourceNotFoundException;
import com.example.bloodlink.repository.BloodGroupRepository;
import com.example.bloodlink.repository.DonorRepository;
import com.example.bloodlink.service.DonorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorServiceTest {

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private BloodGroupRepository bloodGroupRepository;

    @InjectMocks
    private DonorService donorService;

    private BloodGroup bloodGroupA;
    private Donor donor1;
    private Donor donor2;

    @BeforeEach
    void setUp() {
        bloodGroupA = new BloodGroup("A+");
        bloodGroupA.setId(1L);

        donor1 = new Donor("John Doe", "9876543210", "Chennai", bloodGroupA);
        donor1.setId(101L);
        donor1.setAvailable(true);

        donor2 = new Donor("Jane Smith", "9876543211", "Chennai", bloodGroupA);
        donor2.setId(102L);
        donor2.setAvailable(true);
    }

    @Test
    void testRegisterDonorSuccess() {
        when(bloodGroupRepository.findById(1L)).thenReturn(Optional.of(bloodGroupA));
        when(donorRepository.save(any(Donor.class))).thenAnswer(i -> i.getArgument(0));

        Donor result = donorService.registerDonor(donor1);

        assertNotNull(result);
        assertTrue(result.isAvailable());
        assertEquals("John Doe", result.getName());
        verify(donorRepository, times(1)).save(donor1);
    }

    @Test
    void testRegisterDonorThrowsWhenBloodGroupNotFound() {
        when(bloodGroupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> donorService.registerDonor(donor1));
    }

    @Test
    void testSearchDonorsEligibility90Days() {
        // donor1: last donation was 100 days ago -> eligible
        donor1.setLastDonationDate(LocalDate.now().minusDays(100));

        // donor2: last donation was 30 days ago -> ineligible (< 90 days)
        donor2.setLastDonationDate(LocalDate.now().minusDays(30));

        // donor3: never donated before (null) -> eligible
        Donor donor3 = new Donor("Bob", "9876543212", "Chennai", bloodGroupA);
        donor3.setId(103L);
        donor3.setLastDonationDate(null);
        donor3.setAvailable(true);

        when(bloodGroupRepository.findById(1L)).thenReturn(Optional.of(bloodGroupA));
        when(donorRepository.findByBloodGroupAndCityIgnoreCaseAndAvailableTrue(bloodGroupA, "Chennai"))
                .thenReturn(List.of(donor1, donor2, donor3));

        List<Donor> eligibleDonors = donorService.searchDonors(1L, "Chennai");

        assertEquals(2, eligibleDonors.size());
        assertTrue(eligibleDonors.contains(donor1));
        assertTrue(eligibleDonors.contains(donor3));
        assertFalse(eligibleDonors.contains(donor2));
    }

    @Test
    void testSearchDonorsThrowsWhenMissingParams() {
        assertThrows(BadRequestException.class, () -> donorService.searchDonors((Long) null, "Chennai"));
        assertThrows(BadRequestException.class, () -> donorService.searchDonors(1L, "   "));
    }
}
