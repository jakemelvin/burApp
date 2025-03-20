package com.packt.blurApp.controller;

import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Attribution;
import com.packt.blurApp.model.Car;
import com.packt.blurApp.response.ApiResponse;
import com.packt.blurApp.service.car.ICarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cars")
public class CarController {

  private final ICarService carService;

  @GetMapping
  public ResponseEntity<ApiResponse> getAllCars() {
    List<Car> cars = carService.getAllCars();
    return ResponseEntity.ok(new ApiResponse("Cars fetched", cars));
  }

  // Mode 1 : Même voiture pour tous les joueurs
  @GetMapping("/global-attribution")
  public ResponseEntity<ApiResponse> assignGlobalCar() {
    Car car = carService.assignRandomCar();
    return ResponseEntity.ok(new ApiResponse("Global car attributed", car));
  }

  // Mode 2 : Voiture aléatoire par joueur (doublons possibles)
  @GetMapping("/individual-attribution")
  public ResponseEntity<ApiResponse> assignIndividualCar(@RequestBody List<String> Users) {

    try {
      Set<Attribution> cars = carService.assignCarsPerUser(Users);
      return ResponseEntity.ok(new ApiResponse("Individual car attributed", cars));
    } catch (ResourceNotFoundExceptions e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
    }
  }
}