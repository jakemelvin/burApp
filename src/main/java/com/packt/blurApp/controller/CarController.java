package com.packt.blurApp.controller;

import com.packt.blurApp.model.Car;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.car.ICarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cars")
public class CarController {
    private final ICarService carService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_CARS')")
    public ResponseEntity<ApiResponse<?>> getAllCars() {
        List<Car> cars = carService.getAllCars();
        return ResponseEntity.ok(ApiResponse.success("Cars fetched", cars));
    }
}
