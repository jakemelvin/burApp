package com.packt.blurApp.service.car;

import com.packt.blurApp.exceptions.ResourceNotFoundExceptions;
import com.packt.blurApp.model.Attribution;
import com.packt.blurApp.model.Car;
import com.packt.blurApp.model.Race;
import com.packt.blurApp.repository.AttributionRepository;
import com.packt.blurApp.repository.CarRepository;
import com.packt.blurApp.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ICarService implements CarService {

  private final CarRepository carRepository;
  private final AttributionRepository attributionRepository;
  private final RaceRepository raceRepository;

  // Mode 1 : Une voiture aléatoire pour tous les joueurs
  public Car assignRandomCar(long raceId) {
    List<Car> cars = carRepository.findAll();

    if (cars.isEmpty()) {
      throw new IllegalStateException("Aucune voiture disponible.");
    }

    Race race = raceRepository.findById(raceId).orElseThrow(() -> new ResourceNotFoundExceptions("Race not Found!"));
    Car car = cars.get(new Random().nextInt(cars.size()));

    race.setCar(car);
    raceRepository.save(race);
    return car;
  }

  // Mode 2 : Attribuer une voiture aléatoire à chaque joueur (unicité priorisée)
  public Set<Attribution> assignCarsPerUser(List<String> Users, long raceId) {
    try {
      List<Car> cars = carRepository.findAll();
      if (cars.isEmpty()) {
        throw new IllegalStateException("Aucune voiture disponible.");
      }
      Set<Attribution> attributionList = new HashSet<>();
      Random random = new Random();
      List<Car> carsCopy = new ArrayList<>(cars);
      Collections.shuffle(carsCopy, random);
      int carIndex = 0;
      for (int i = 0; i < Users.size(); i++) {
        String user = Users.get(i);
        Attribution userAttribution = new Attribution();
        Car randomCar;
        // Tant qu'il reste des voitures non attribuées, on évite les doublons
        if (i < carsCopy.size()) {
          randomCar = carsCopy.get(i);
        } else {
          // Si plus assez de voitures, on attribue aléatoirement (doublons possibles)
          randomCar = cars.get(random.nextInt(cars.size()));
        }
        userAttribution.setImageUrl(randomCar.getImageUrl());
        userAttribution.setName(randomCar.getName());
        userAttribution.setUserName(user);
        userAttribution.setRace(raceRepository.findById(raceId).orElseThrow(() -> new ResourceNotFoundExceptions("Race not Found!")));
        attributionRepository.save(userAttribution);
        attributionList.add(userAttribution);
      }
      return attributionList;
    } catch (ResourceNotFoundExceptions e) {
      throw new ResourceNotFoundExceptions(e.getMessage());
    } catch (IllegalStateException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  @Override
  public List<Car> getAllCars() {
    return carRepository.findAll();
  }
}