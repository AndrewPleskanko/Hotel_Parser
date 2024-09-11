package org.example.hbparser.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Facility {
    private int facilityCode;
    private int facilityGroupCode;
    private String description;
}
