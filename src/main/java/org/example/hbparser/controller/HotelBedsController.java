package org.example.hbparser.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.hbparser.service.HotelBedsService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hotels")
public class HotelBedsController {

    private final HotelBedsService hotelBedsService;

    @GetMapping("/hbparser")
    public String fetchAndSaveHotelData() {
        try {
            log.info("Received request to fetch and save hotel data");
            hotelBedsService.fetchAndSaveHotelData();
            return "Hotel data has been fetched and saved successfully.";
        } catch (Exception e) {
            log.error("Error occurred while fetching and saving hotel data", e);
            return "Failed to fetch and save hotel data.";
        }
    }
}

