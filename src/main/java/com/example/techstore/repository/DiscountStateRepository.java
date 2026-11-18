package com.example.techstore.repository;

import com.example.techstore.model.discount.DiscountState;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DiscountStateRepository extends JpaRepository<DiscountState, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from DiscountState s where s.id=1")
    Optional<DiscountState> lockState();
}
