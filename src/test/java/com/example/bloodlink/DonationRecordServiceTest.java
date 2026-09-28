package com.example.bloodlink;

import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.entity.DonationRecord;
import com.example.bloodlink.entity.Donor;
import com.example.bloodlink.exception.BadRequestException;
import com.example.bloodlink.exception.ResourceNotFoundException;
import com.example.bloodlink.repository.DonationRecordRepository;
import com.example.bloodlink.repository.DonorRepository;
import com.example.bloodlink.service.DonationRecordService;
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
class DonationRecordServiceTest {

    @Mock
    private DonationRecordRepository donationRecordRepository;

    @Mock
    private DonorRepository donorRepository;

    @InjectMocks
    private DonationRecordService donationRecordService;

    private Donor donor;
    private DonationRecord record;

    @BeforeEach
    void setUp() {
        BloodGroup bloodGroup = new BloodGroup("O+");
        bloodGroup.setId(1L);

        donor = new Donor("Alice Smith", "9123456780", "Madurai", bloodGroup);
        donor.setId(50L);
        donor.setAvailable(true);

        record = new DonationRecord();
        Donor donorRef = new Donor();
        donorRef.setId(50L);
        record.setDonor(donorRef);
        record.setDonationDate(LocalDate.now());
    }

    @Test
    void testRecordDonationUpdatesDonorState() {
        when(donorRepository.findById(50L)).thenReturn(Optional.of(donor));
        when(donorRepository.save(any(Donor.class))).thenAnswer(i -> i.getArgument(0));
        when(donationRecordRepository.save(any(DonationRecord.class))).thenAnswer(i -> i.getArgument(0));

        DonationRecord saved = donationRecordService.recordDonation(record);

        assertNotNull(saved);
        assertEquals(LocalDate.now(), saved.getDonationDate());
        assertFalse(donor.isAvailable(), "Donor should become unavailable after donation");
        assertEquals(LocalDate.now(), donor.getLastDonationDate());
        verify(donorRepository, times(1)).save(donor);
        verify(donationRecordRepository, times(1)).save(record);
    }

    @Test
    void testRecordDonationDonorNotFound() {
        when(donorRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> donationRecordService.recordDonation(record));
    }

    @Test
    void testRecordDonationFutureDateRejected() {
        record.setDonationDate(LocalDate.now().plusDays(2));
        when(donorRepository.findById(50L)).thenReturn(Optional.of(donor));

        assertThrows(BadRequestException.class, () -> donationRecordService.recordDonation(record));
    }

    @Test
    void testGetAllDonationRecords() {
        when(donationRecordRepository.findAll()).thenReturn(List.of(record));

        List<DonationRecord> list = donationRecordService.getAllDonationRecords();
        assertEquals(1, list.size());
    }
}
