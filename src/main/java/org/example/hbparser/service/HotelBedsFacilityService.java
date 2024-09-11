package org.example.hbparser.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hbparser.model.entity.Facility;
import org.example.hbparser.security.HBAuthorization;
import org.example.hbparser.service.util.HttpHeaderUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.example.hbparser.constant.HBConstant.HOTELBEDS_PETS_ENDPOINT_URL;

@Getter
@Slf4j
@Service
@RequiredArgsConstructor
public class HotelBedsFacilityService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HBAuthorization apiClient;

    private final List<Facility> petFriendlyFacilities = new ArrayList<>();
    private final List<Facility> wheelchairFacilities = new ArrayList<>();

    public void fetchFacilityCodes() throws Exception {
        String jsonResponse = fetchFacilityData();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        JsonNode facilitiesNode = rootNode.path("facilities");
        for (JsonNode facilityNode : facilitiesNode) {
            String description = facilityNode.path("description").path("content").asText().toLowerCase();
            int facilityCode = facilityNode.path("code").asInt();
            int facilityGroupCode = facilityNode.path("facilityGroupCode").asInt();

            Facility facility = new Facility(facilityCode, facilityGroupCode, description);

            if (description.contains("pets allowed")) {
                petFriendlyFacilities.add(facility);
            }

            if (description.contains("no wheelchair-accessible")) {
                continue;
            }

            if (description.contains("yes wheelchair-accessible") || description.contains("wheelchair-accessible")) {
                wheelchairFacilities.add(facility);
            }
        }

        log.info("Fetched pet-friendly facilities: {}", petFriendlyFacilities);
        log.info("Fetched wheelchair-accessible facilities: {}", wheelchairFacilities);
    }

    private String fetchFacilityData() {
        HttpEntity<String> entity = HttpHeaderUtil.createHttpEntityWithHeaders(apiClient);
        ResponseEntity<String> response = restTemplate.exchange(HOTELBEDS_PETS_ENDPOINT_URL, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }
}
