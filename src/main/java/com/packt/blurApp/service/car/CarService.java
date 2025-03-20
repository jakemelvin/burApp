package com.packt.blurApp.service.car;

import com.packt.blurApp.model.Car;

import java.util.List;
import java.util.Map;

public interface CarService {
    Car assignRandomCar();
    Map<String, Car> assignCarsPerUser(List<String> Users);
    List<Car> getAllCars();
}
