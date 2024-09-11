package org.example.hbparser.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hbparser.model.entity.FacilityCodePair;
import org.example.hbparser.model.entity.Hotel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HotelDataParser {

    private final ObjectMapper objectMapper;

    public List<Hotel> parseHotelData(String jsonResponse) throws Exception {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        int total = rootNode.path("total").asInt();
        log.info("Total hotels available: {}", total);

        JsonNode hotelsNode = rootNode.path("hotels");

        if (!hotelsNode.isArray()) {
            throw new IllegalArgumentException("Expected an array of hotels");
        }

        List<Hotel> hotels = new ArrayList<>();
        for (JsonNode hotelNode : hotelsNode) {
            hotels.add(parseHotel(hotelNode));
        }

        return hotels;
    }

    private Hotel parseHotel(JsonNode hotelNode) {
        Hotel hotel = new Hotel();
        hotel.setHotelName(hotelNode.path("name").path("content").asText());
        hotel.setAddress(hotelNode.path("address").path("content").asText());
        hotel.setCity(hotelNode.path("city").path("content").asText());
        hotel.setPostalCode(hotelNode.path("postalCode").asText());
        hotel.setCountry(hotelNode.path("countryCode").asText());
        hotel.setState(hotelNode.path("stateCode").asText());
        hotel.setWebsite(hotelNode.path("website").asText());

        hotel.setEmails(parseEmails(hotelNode.path("email")));
        hotel.setTelephoneNumbers(parsePhoneNumbers(hotelNode.path("phones")));

        hotel.setPetFriendly(false);
        hotel.setWheelchairAccessible(false);

        List<String> roomNames = parseRoomNames(hotelNode.path("rooms"));
        hotel.setRoomNames(roomNames);

        List<FacilityCodePair> facilityCodes = new ArrayList<>(parseFacilityCodes(hotelNode.path("facilities")));
        hotel.setFacilityCodes(facilityCodes);

        return hotel;
    }

    private List<String> parseEmails(JsonNode emailNode) {
        List<String> emails = new ArrayList<>();
        if (emailNode.isTextual()) {
            emails.add(emailNode.asText());
        }
        return emails;
    }

    private List<String> parsePhoneNumbers(JsonNode phonesNode) {
        List<String> phoneNumbers = new ArrayList<>();
        for (JsonNode phoneNode : phonesNode) {
            phoneNumbers.add(phoneNode.path("phoneNumber").asText());
        }
        return phoneNumbers;
    }

    private List<String> parseRoomNames(JsonNode roomsNode) {
        List<String> roomNames = new ArrayList<>();
        for (JsonNode roomNode : roomsNode) {
            roomNames.add(roomNode.path("roomCode").asText());
        }
        return roomNames;
    }

    private List<FacilityCodePair> parseFacilityCodes(JsonNode facilitiesNode) {
        List<FacilityCodePair> facilityCodePairs = new ArrayList<>();
        for (JsonNode facilityNode : facilitiesNode) {
            int facilityCode = facilityNode.path("facilityCode").asInt();
            int facilityGroupCode = facilityNode.path("facilityGroupCode").asInt();
            facilityCodePairs.add(new FacilityCodePair(facilityCode, facilityGroupCode));
        }
        return facilityCodePairs;
    }

    private List<FacilityCodePair> parseRoomFacilityCodes(JsonNode roomsNode) {
        List<FacilityCodePair> facilityCodePairs = new ArrayList<>();
        for (JsonNode roomNode : roomsNode) {
            JsonNode roomFacilitiesNode = roomNode.path("roomFacilities");
            for (JsonNode facilityNode : roomFacilitiesNode) {
                int facilityCode = facilityNode.path("facilityCode").asInt();
                int facilityGroupCode = facilityNode.path("facilityGroupCode").asInt();
                facilityCodePairs.add(new FacilityCodePair(facilityCode, facilityGroupCode));
            }
        }
        return facilityCodePairs;
    }
}
