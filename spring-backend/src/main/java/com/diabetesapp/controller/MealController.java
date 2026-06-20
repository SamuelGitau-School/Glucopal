package com.diabetesapp.controller;

import com.diabetesapp.dto.MealDto;
import com.diabetesapp.service.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

    @PostMapping
    public ResponseEntity<MealDto.Response> addMeal(
            @PathVariable Long userId,
            @Valid @RequestBody MealDto.Request request) {
        return ResponseEntity.ok(mealService.addMeal(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<MealDto.Response>> getMeals(@PathVariable Long userId) {
        return ResponseEntity.ok(mealService.getAllMeals(userId));
    }

    @DeleteMapping("/{mealId}")
    public ResponseEntity<Void> deleteMeal(
            @PathVariable Long userId,
            @PathVariable Long mealId) {
        mealService.deleteMeal(userId, mealId);
        return ResponseEntity.noContent().build();
    }
}