package com.packt.blurApp.service.car;

import com.packt.blurApp.model.Attribution;
import com.packt.blurApp.model.Car;

import java.util.List;
import java.util.Set;

public interface CarService {
  Car assignRandomCar(long raceId);

  Set<Attribution> assignCarsPerUser(List<String> Users, long raceId);

  List<Car> getAllCars();
}
