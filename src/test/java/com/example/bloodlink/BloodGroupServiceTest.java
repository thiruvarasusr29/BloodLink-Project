package com.example.bloodlink;

import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.exception.BadRequestException;
import com.example.bloodlink.exception.DuplicateResourceException;
import com.example.bloodlink.repository.BloodGroupRepository;
import com.example.bloodlink.service.BloodGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloodGroupServiceTest {

    @Mock
    private BloodGroupRepository bloodGroupRepository;

    @InjectMocks
    private BloodGroupService bloodGroupService;

    private BloodGroup bloodGroup;

    @BeforeEach
    void setUp() {
        bloodGroup = new BloodGroup("B+");
        bloodGroup.setId(1L);
    }

    @Test
    void testSaveBloodGroupSuccess() {
        when(bloodGroupRepository.existsByGroupNameIgnoreCase("B+")).thenReturn(false);
        when(bloodGroupRepository.save(any(BloodGroup.class))).thenAnswer(i -> i.getArgument(0));

        BloodGroup saved = bloodGroupService.saveBloodGroup(new BloodGroup("b+"));

        assertNotNull(saved);
        assertEquals("B+", saved.getGroupName());
        verify(bloodGroupRepository).save(any(BloodGroup.class));
    }

    @Test
    void testSaveBloodGroupDuplicate() {
        when(bloodGroupRepository.existsByGroupNameIgnoreCase("B+")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> bloodGroupService.saveBloodGroup(new BloodGroup("B+")));
    }

    @Test
    void testSaveBloodGroupEmptyName() {
        assertThrows(BadRequestException.class, () -> bloodGroupService.saveBloodGroup(new BloodGroup("  ")));
    }

    @Test
    void testGetAllBloodGroups() {
        when(bloodGroupRepository.findAll()).thenReturn(List.of(bloodGroup));

        List<BloodGroup> list = bloodGroupService.getAllBloodGroups();
        assertEquals(1, list.size());
    }
}
