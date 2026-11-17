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
@Table(name = "brand")
@Getter
@Setter
@NoArgsConstructor
public class Brand extends AssignedIdEntity {
    @Id
    @Column(length = 100)
    private String name;

    public Brand(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return name;
    }
}
