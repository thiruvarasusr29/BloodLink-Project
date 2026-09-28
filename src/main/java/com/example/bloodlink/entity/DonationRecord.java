package com.example.bloodlink.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Entity
@Table(name = "donation_records")
public class DonationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @PastOrPresent(message = "Donation date cannot be in the future")
    @Column(nullable = false)
    private LocalDate donationDate;

    @NotNull(message = "Donor is required")
    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    public DonationRecord() {
    }

    public DonationRecord(LocalDate donationDate, Donor donor) {
        this.donationDate = donationDate;
        this.donor = donor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }

    public Donor getDonor() {
        return donor;
    }

    public void setDonor(Donor donor) {
        this.donor = donor;
    }
}