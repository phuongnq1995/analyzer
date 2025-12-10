package org.phuongnq.analyzer.controller;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.service.IngestDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/csv/upload")
@RequiredArgsConstructor
public class CSVController {

    private final IngestDataService ingestDataService;

    @PostMapping(value="/click", consumes = {MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> uploadCsvClickFile(@RequestParam("file") MultipartFile file) {
        String message = "";
        try {
            ingestDataService.ingestClicks(file);

            message = "Successfully uploaded and processed the file: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(message);

        } catch (RuntimeException e) {
            message = "Could not upload/process the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }

    @PostMapping(value="/order", consumes = {MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> uploadCsvOrderFile(@RequestParam("file") MultipartFile file) {
        String message = "";
        try {
            ingestDataService.ingestOrders(file);

            message = "Successfully uploaded and processed the file: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(message);

        } catch (RuntimeException e) {
            message = "Could not upload/process the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }

    @PostMapping(value="/ad", consumes = {MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> uploadCsvAdFile(@RequestParam("file") MultipartFile file) {
        String message = "";
        try {
            ingestDataService.ingestAds(file);

            message = "Successfully uploaded and processed the file: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(message);

        } catch (RuntimeException e) {
            message = "Could not upload/process the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }
}