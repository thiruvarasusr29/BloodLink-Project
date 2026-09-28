package com.example.bloodlink;

import com.example.bloodlink.controller.BloodGroupController;
import com.example.bloodlink.controller.DonationRecordController;
import com.example.bloodlink.controller.DonorController;
import com.example.bloodlink.entity.BloodGroup;
import com.example.bloodlink.entity.DonationRecord;
import com.example.bloodlink.entity.Donor;
import com.example.bloodlink.exception.DuplicateResourceException;
import com.example.bloodlink.exception.GlobalExceptionHandler;
import com.example.bloodlink.exception.ResourceNotFoundException;
import com.example.bloodlink.service.BloodGroupService;
import com.example.bloodlink.service.DonationRecordService;
import com.example.bloodlink.service.DonorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({BloodGroupController.class, DonorController.class, DonationRecordController.class})
@Import(GlobalExceptionHandler.class)
class ControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BloodGroupService bloodGroupService;

    @MockitoBean
    private DonorService donorService;

    @MockitoBean
    private DonationRecordService donationRecordService;

    @Test
    void testGetBloodGroups() throws Exception {
        BloodGroup bg = new BloodGroup("A+");
        bg.setId(1L);
        when(bloodGroupService.getAllBloodGroups()).thenReturn(List.of(bg));

        mockMvc.perform(get("/api/blood-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].groupName").value("A+"));
    }

    @Test
    void testCreateBloodGroupSuccess() throws Exception {
        BloodGroup bg = new BloodGroup("O+");
        bg.setId(2L);
        when(bloodGroupService.saveBloodGroup(any(BloodGroup.class))).thenReturn(bg);

        mockMvc.perform(post("/api/blood-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupName\": \"O+\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.groupName").value("O+"));
    }

    @Test
    void testCreateBloodGroupDuplicateConflict() throws Exception {
        when(bloodGroupService.saveBloodGroup(any(BloodGroup.class)))
                .thenThrow(new DuplicateResourceException("Blood group 'O+' already exists"));

        mockMvc.perform(post("/api/blood-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"groupName\": \"O+\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Blood group 'O+' already exists"));
    }

    @Test
    void testRegisterDonorValidationFailure() throws Exception {
        // missing required fields
        mockMvc.perform(post("/api/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testSearchDonors() throws Exception {
        BloodGroup bg = new BloodGroup("B+");
        bg.setId(1L);
        Donor donor = new Donor("Ravi", "9876543210", "Coimbatore", bg);
        donor.setId(10L);

        when(donorService.searchDonors(eq(1L), eq("Coimbatore"))).thenReturn(List.of(donor));

        mockMvc.perform(get("/api/donors/search")
                        .param("bloodGroupId", "1")
                        .param("city", "Coimbatore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ravi"))
                .andExpect(jsonPath("$[0].city").value("Coimbatore"));
    }

    @Test
    void testRecordDonationSuccess() throws Exception {
        BloodGroup bg = new BloodGroup("B+");
        bg.setId(1L);
        Donor donor = new Donor("Ravi", "9876543210", "Coimbatore", bg);
        donor.setId(10L);

        DonationRecord record = new DonationRecord(LocalDate.now(), donor);
        record.setId(100L);

        when(donationRecordService.recordDonation(any(DonationRecord.class))).thenReturn(record);

        mockMvc.perform(post("/api/donations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"donor\": {\"id\": 10}, \"donationDate\": \"" + LocalDate.now() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }
}
