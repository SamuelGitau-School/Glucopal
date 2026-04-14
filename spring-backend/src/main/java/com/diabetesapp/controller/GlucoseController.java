package com.diabetesapp.controller;

import com.diabetesapp.dto.GlucoseRecordDto;
import com.diabetesapp.service.GlucoseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/glucose")
@RequiredArgsConstructor
public class GlucoseController {

    private final GlucoseService glucoseService;

    /** Log a new reading */
    @PostMapping
    public ResponseEntity<GlucoseRecordDto.Response> addReading(
            @PathVariable Long userId,
            @Valid @RequestBody GlucoseRecordDto.Request request) {
        return ResponseEntity.ok(glucoseService.addReading(userId, request));
    }

    /** List all readings */
    @GetMapping
    public ResponseEntity<List<GlucoseRecordDto.Response>> getReadings(
            @PathVariable Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (from != null && to != null) {
            return ResponseEntity.ok(glucoseService.getReadingsInRange(userId, from, to));
        }
        return ResponseEntity.ok(glucoseService.getAllReadings(userId));
    }

    /** Dashboard Summary */
    @GetMapping("/stats")
    public ResponseEntity<GlucoseRecordDto.Stats> getStats(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "70") int targetLow,
            @RequestParam(defaultValue = "140") int targetHigh) {
        return ResponseEntity.ok(glucoseService.getStats(userId, targetLow, targetHigh));
    }

    /** DELETE */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteReading(
            @PathVariable Long userId,
            @PathVariable Long recordId) {
        glucoseService.deleteReading(userId, recordId);
        return ResponseEntity.noContent().build();
    }
}