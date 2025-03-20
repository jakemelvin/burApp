package com.packt.blurApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Attribution {
  private Long id;
  private String name;
  private String imageUrl;
  private String userName;
}
