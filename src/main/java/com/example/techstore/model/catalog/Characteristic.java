package com.example.techstore.model.catalog;

import com.example.techstore.model.AssignedIdEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "specification")
@Getter
@Setter
@NoArgsConstructor
public class Characteristic extends AssignedIdEntity {
    @Id
    @Column(length = 100)
    private String name;

    @Column(name = "value_type", nullable = false, length = 10)
    private String valueType;

    @Column(length = 20)
    private String unit;

    @Override
    public String getId() {
        return name;
    }
}
