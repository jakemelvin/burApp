package com.packt.blurApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class BlurAppApplication {

  public static void main(String[] args) {
    SpringApplication.run(BlurAppApplication.class, args);
  }

  @GetMapping("/")
  public String SayHello() {
    return "Hello World";
  }
}
