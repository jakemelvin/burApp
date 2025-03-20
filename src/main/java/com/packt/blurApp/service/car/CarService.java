package com.packt.blurApp.service.car;

import com.packt.blurApp.model.Attribution;
import com.packt.blurApp.model.Car;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CarService {
  Car assignRandomCar();

  Set<Attribution> assignCarsPerUser(List<String> Users);

  List<Car> getAllCars();
}
