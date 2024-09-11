package org.example.hbparser.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hbparser.security.HBAuthorization;
import org.example.hbparser.service.util.HttpHeaderUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.example.hbparser.constant.HBConstant.HOTELBEDS_ROOM_URL;
import static org.example.hbparser.constant.HBConstant.INITIAL_FROM;
import static org.example.hbparser.constant.HBConstant.LARGE_PAGE_SIZE;
import static org.example.hbparser.constant.HBConstant.PAGE_SIZE;

@Getter
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final HBAuthorization apiClient;

    private final Map<String, String> roomCodeToNameMap = new HashMap<>();

    public void fetchRoomTypes() throws Exception {
        int from = INITIAL_FROM;
        int to = LARGE_PAGE_SIZE;
        int total;

        String initialApiUrl = String.format("%s/types/rooms?fields=all&language=ENG&useSecondaryLanguage=True&from=%d&to=%d",
                HOTELBEDS_ROOM_URL, from, to);
        log.info("Fetching data from URL: {}", initialApiUrl);
        String initialJsonResponse = fetchRoomData(initialApiUrl);

        JsonNode initialRootNode = objectMapper.readTree(initialJsonResponse);
        total = initialRootNode.path("total").asInt();
        log.info("Total rooms to fetch: {}", total);

        parseRoomTypes(initialRootNode);

        for (from = LARGE_PAGE_SIZE; from < total; from += PAGE_SIZE) {
            to = Math.min(from + PAGE_SIZE, total);
            String apiUrl = String.format("%s/types/rooms?fields=all&language=ENG&useSecondaryLanguage=True&from=%d&to=%d",
                    HOTELBEDS_ROOM_URL, from, to);
            log.info("Fetching data from URL: {}", apiUrl);
            String jsonResponse = fetchRoomData(apiUrl);

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            parseRoomTypes(rootNode);
        }

        log.info("Room types fetched and stored: {}", roomCodeToNameMap.size());
    }

    private String fetchRoomData(String apiUrl) {
        try {
            HttpEntity<String> entity = HttpHeaderUtil.createHttpEntityWithHeaders(apiClient);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("Failed to fetch data from API: HTTP status {}", response.getStatusCode());
                throw new RuntimeException("Failed to fetch data from API");
            }
        } catch (Exception e) {
            log.error("Error fetching room data from URL: {}", apiUrl, e);
            throw new RuntimeException("Error fetching room data", e);
        }
    }

    private void parseRoomTypes(JsonNode rootNode) {
        JsonNode roomsNode = rootNode.path("rooms");
        if (roomsNode.isArray()) {
            for (JsonNode roomNode : roomsNode) {
                String roomCode = roomNode.path("code").asText();
                String roomDescription = roomNode.path("description").asText();
                roomCodeToNameMap.put(roomCode, roomDescription);
            }
        } else {
            log.warn("No 'rooms' field found in the JSON response");
        }
    }

    public String getRoomTypeDescription(String roomCode) {
        return roomCodeToNameMap.getOrDefault(roomCode, roomCode);
    }
}
