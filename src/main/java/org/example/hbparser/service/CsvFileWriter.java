package org.example.hbparser.service;

import lombok.extern.slf4j.Slf4j;
import org.example.hbparser.model.entity.Hotel;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.example.hbparser.constant.HBConstant.OUTPUT_CSV_FILE;

@Slf4j
@Component
public class CsvFileWriter {

    public void writeToCsv(List<Hotel> hotels) throws Exception {
        if (hotels == null || hotels.isEmpty()) {
            log.warn("No hotels data to write");
            return;
        }

        log.info("Writing hotel data to CSV file: {}", OUTPUT_CSV_FILE);

        Path path = Path.of(OUTPUT_CSV_FILE);
        if (!Files.exists(path)) {
            String header = "Hotel name, Full address, City, Official hotel website link, Contacts, Is pet friendly, Facility descriptions, Is wheelchair accessible, Room names\n";
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
                writer.write(header);
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            for (Hotel hotel : hotels) {
                String contactsString = convertContactsToString(hotel);
                String roomNamesString = String.join(",", hotel.getRoomNames());

                String csvLine = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%b\",\"%s\",\"%b\",\"%s\"\n",
                        hotel.getHotelName(),
                        hotel.getAddress(),
                        hotel.getCity(),
                        hotel.getWebsite(),
                        contactsString,
                        hotel.isPetFriendly(),
                        hotel.getFacilityDescriptions(),
                        hotel.isWheelchairAccessible(),
                        roomNamesString
                );
                writer.write(csvLine);
            }
        }

        log.info("Successfully wrote {} hotels to CSV file.", hotels.size());
    }

    private String convertContactsToString(Hotel hotel) {
        List<String> formattedContacts = new ArrayList<>();

        if (!hotel.getEmails().isEmpty()) {
            formattedContacts.add("Email: " + String.join(", ", hotel.getEmails()));
        }

        if (!hotel.getTelephoneNumbers().isEmpty()) {
            formattedContacts.add(" Phone: " + String.join(", ", hotel.getTelephoneNumbers()));
        }

        return String.join(", ", formattedContacts);
    }
}
