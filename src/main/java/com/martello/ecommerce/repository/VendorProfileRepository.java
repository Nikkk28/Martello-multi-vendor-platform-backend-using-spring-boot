package com.martello.ecommerce.repository;

import com.martello.ecommerce.model.entity.VendorProfile;
import com.martello.ecommerce.model.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
    Optional<VendorProfile> findByUserId(Long userId);
    List<VendorProfile> findByStatus(ApprovalStatus status);
}
