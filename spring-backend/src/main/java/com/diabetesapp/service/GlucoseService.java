package com.diabetesapp.service;

import com.diabetesapp.dto.GlucoseRecordDto;
import com.diabetesapp.model.GlucoseRecord;
import com.diabetesapp.model.User;
import com.diabetesapp.repository.GlucoseRecordRepository;
import com.diabetesapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlucoseService {

    private final GlucoseRecordRepository glucoseRepo;
    private final UserRepository userRepo;

    @Transactional
    public GlucoseRecordDto.Response addReading(Long userId, GlucoseRecordDto.Request req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        GlucoseRecord record = new GlucoseRecord();
        record.setUser(user);
        record.setValue(req.getValue());
        record.setRecordedAt(req.getRecordedAt() != null ? req.getRecordedAt() : LocalDateTime.now());
        record.setMealContext(req.getMealContext());
        record.setNote(req.getNote());

        return GlucoseRecordDto.Response.from(glucoseRepo.save(record));
    }

    @Transactional(readOnly = true)
    public List<GlucoseRecordDto.Response> getAllReadings(Long userId) {
        return glucoseRepo.findByUserIdOrderByRecordedAtDesc(userId)
                .stream()
                .map(GlucoseRecordDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GlucoseRecordDto.Response> getReadingsInRange(
        Long userId,
        LocalDateTime from,
        LocalDateTime to) {
        return glucoseRepo
            .findByUserIdAndRecordedAtBetweenOrderByRecordedAtDesc(userId, from, to)
            .stream()
            .map(GlucoseRecordDto.Response::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReading(Long userId, Long recordId) {
        GlucoseRecord record = glucoseRepo.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Record not found: " + recordId));
        if (!record.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        glucoseRepo.delete(record);
    }

    @Transactional(readOnly = true)
    public GlucoseRecordDto.Stats getStats(Long userId, int targetLow, int targetHigh) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekAgo = now.minusDays(7);

        GlucoseRecordDto.Stats stats = new GlucoseRecordDto.Stats();

        stats.setTodayAvg(glucoseRepo.findAverageByUserIdSince(userId, todayStart).orElse(0.0));

        double weekAvg = glucoseRepo.findAverageByUserIdSince(userId, weekAgo).orElse(0.0);
        stats.setWeekAvg(weekAvg);

        long total = glucoseRepo.countByUserIdSince(userId, weekAgo);
        stats.setTotalReadings((int) total);

        long inRange = glucoseRepo.countInRangeByUserIdSince(
            userId, weekAgo,
            BigDecimal.valueOf(targetLow),
            BigDecimal.valueOf(targetHigh)
        );
        stats.setInRangeCount((int) inRange);
        stats.setInRangePercent(total > 0 ? (inRange * 100.0 / total) : 0.0);

        if (weekAvg > 0) {
            stats.setCurrentA1cEstimate(Math.round(((weekAvg + 46.7) / 28.7) * 10.0) / 10.0);
        }

        return stats;
    }
}
