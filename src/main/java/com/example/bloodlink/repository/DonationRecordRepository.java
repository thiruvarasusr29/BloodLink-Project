package com.example.bloodlink.repository;

import com.example.bloodlink.entity.DonationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRecordRepository extends JpaRepository<DonationRecord, Long> {
}