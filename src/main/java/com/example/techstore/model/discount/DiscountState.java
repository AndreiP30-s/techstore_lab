package com.example.techstore.model.discount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "discount_state")
@Getter
@Setter
@NoArgsConstructor
public class DiscountState {
    @Id private Integer id;

    @Column(nullable = false)
    private boolean active;
}
