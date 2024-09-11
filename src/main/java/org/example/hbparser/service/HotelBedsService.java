package org.example.hbparser.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hbparser.model.entity.Facility;
import org.example.hbparser.model.entity.FacilityCodePair;
import org.example.hbparser.model.entity.Hotel;
import org.example.hbparser.security.HBAuthorization;
import org.example.hbparser.service.util.HttpHeaderUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.hbparser.constant.HBConstant.HOTELBEDS_API_URL;
import static org.example.hbparser.constant.HBConstant.INITIAL_FROM;
import static org.example.hbparser.constant.HBConstant.PAGE_SIZE;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelBedsService {

    private final HBAuthorization apiClient;
    private final HotelDataParser dataParser;
    private final CsvFileWriter csvWriter;
    private final RestTemplate restTemplate;
    private final HotelBedsFacilityService facilityService;
    private final RoomTypeService roomTypeService;

    public void fetchAndSaveHotelData() {
        try {
            log.info("Starting to fetch hotel data");

            roomTypeService.fetchRoomTypes();
            facilityService.fetchFacilityCodes();

            List<Hotel> allHotels = fetchAllHotelData();
            if (allHotels.isEmpty()) {
                log.warn("No hotels data to write");
            } else {
                csvWriter.writeToCsv(allHotels);
            }

            log.info("Completed fetching and saving hotel data");

        } catch (Exception e) {
            log.error("An error occurred while fetching and saving hotel data", e);
        }
    }

    private List<Hotel> fetchAllHotelData() throws Exception {
        List<Facility> petFriendlyFacilities = facilityService.getPetFriendlyFacilities();
        List<Facility> wheelchairFacilities = facilityService.getWheelchairFacilities();

        int from = INITIAL_FROM;
        int to = PAGE_SIZE;
        int total;
        List<Hotel> allHotels = new ArrayList<>();

        String initialApiUrl = String.format("%s?fields=all&language=ENG&useSecondaryLanguage=false&countryCode=NL&from=%d&to=%d", HOTELBEDS_API_URL, from, to);
        log.info("Fetching data from URL: {}", initialApiUrl);
        String initialJsonResponse = fetchHotelData(initialApiUrl);
        JsonNode initialRootNode = new ObjectMapper().readTree(initialJsonResponse);
        total = initialRootNode.path("total").asInt();
        log.info("{} Total", total);

        List<Hotel> initialHotels = dataParser.parseHotelData(initialJsonResponse);
        markPetFriendlyAndWheelchairAccessibleHotels(initialHotels, petFriendlyFacilities, wheelchairFacilities);
        replaceRoomCodesWithDescriptions(initialHotels, roomTypeService.getRoomCodeToNameMap());

        allHotels.addAll(initialHotels);

        for (from = PAGE_SIZE; from < total; from += PAGE_SIZE) {
            to = Math.min(from + PAGE_SIZE, total);
            String apiUrl = HOTELBEDS_API_URL + "?from=" + from + "&to=" + to + "&fields=all&language=ENG&useSecondaryLanguage=false&countryCode=NL";
            log.info("Fetching data from URL: {}", apiUrl);
            String jsonResponse = fetchHotelData(apiUrl);
            List<Hotel> hotels = dataParser.parseHotelData(jsonResponse);
            log.info("Fetched {} hotels", hotels.size());
            markPetFriendlyAndWheelchairAccessibleHotels(hotels, petFriendlyFacilities, wheelchairFacilities);
            replaceRoomCodesWithDescriptions(hotels, roomTypeService.getRoomCodeToNameMap());
            allHotels.addAll(hotels);
        }

        return allHotels;
    }

    private void markPetFriendlyAndWheelchairAccessibleHotels(
            List<Hotel> hotels,
            List<Facility> petFriendlyFacilities,
            List<Facility> wheelchairFacilities) {
        for (Hotel hotel : hotels) {
            boolean petFriendly = petFriendlyFacilities.stream()
                    .anyMatch(facility -> hotel.getFacilityCodes().stream()
                            .anyMatch(pair -> pair.getFacilityCode() == facility.getFacilityCode()));

            boolean wheelchairAccessible = wheelchairFacilities.stream()
                    .anyMatch(facility -> hotel.getFacilityCodes().stream()
                            .anyMatch(pair -> pair.getFacilityCode() == facility.getFacilityCode()));

            hotel.setPetFriendly(petFriendly);
            hotel.setWheelchairAccessible(wheelchairAccessible);

            assignFacilityDescriptions(hotel, petFriendlyFacilities);
        }
    }

    private void assignFacilityDescriptions(
            Hotel hotel,
            List<Facility> petFriendlyFacilities) {
        StringBuilder descriptions = new StringBuilder();
        boolean isPetFriendly = false;

        for (FacilityCodePair pair : hotel.getFacilityCodes()) {
            Facility petFacility = petFriendlyFacilities.stream()
                    .filter(facility -> facility.getFacilityCode() == pair.getFacilityCode())
                    .findFirst()
                    .orElse(null);

            if (petFacility != null) {
                descriptions.append("Pet-friendly: ").append(petFacility.getDescription()).append("\n");
                isPetFriendly = true;
            }
        }

        if (!isPetFriendly) {
            descriptions.append("Not pet-friendly");
        }

        hotel.setFacilityDescriptions(descriptions.toString().trim());
    }


    private String fetchHotelData(String apiUrl) {
        HttpEntity<String> entity = HttpHeaderUtil.createHttpEntityWithHeaders(apiClient);
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    private void replaceRoomCodesWithDescriptions(List<Hotel> hotels, Map<String, String> roomDescriptions) {
        for (Hotel hotel : hotels) {
            List<String> updatedRoomNames = new ArrayList<>();
            for (String roomCode : hotel.getRoomNames()) {
                if (roomDescriptions.containsKey(roomCode)) {
                    String roomDescription = roomDescriptions.get(roomCode);
                    updatedRoomNames.add(roomDescription);
                } else {
                    updatedRoomNames.add(roomCode);
                }
            }

            String formattedRoomNames = String.join(", ", updatedRoomNames);
            hotel.setRoomNames(List.of(formattedRoomNames));
        }
    }
}
