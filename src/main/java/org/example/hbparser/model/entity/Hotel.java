package org.example.hbparser.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {
    private String hotelName;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private String state;
    private String website;
    private List<String> emails;
    private List<String> telephoneNumbers;
    private boolean isPetFriendly;
    private boolean isWheelchairAccessible;
    private List<String> roomNames;
    private List<FacilityCodePair> facilityCodes;
    private String facilityDescriptions;
}
