package com.packt.blurApp.service.car;

import com.packt.blurApp.model.Car;
import java.util.List;

public interface ICarService {
    List<Car> getAllCars();
    Car getCarById(Long id);
}
