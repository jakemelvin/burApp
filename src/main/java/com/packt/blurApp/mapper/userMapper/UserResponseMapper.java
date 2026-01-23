package com.packt.blurApp.mapper.userMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.packt.blurApp.dto.User.UserGlobalResponseDto;
import com.packt.blurApp.dto.User.UserResponseDto;
import com.packt.blurApp.dto.User.UserSignInResponseDto;
import com.packt.blurApp.mapper.raceMapper.RaceMapper;
import com.packt.blurApp.model.User;
import org.hibernate.Hibernate;

public class UserResponseMapper {
    
    public static UserResponseDto toUserResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUsername());
        return dto;
    }

    public static UserSignInResponseDto toUserSignInResponseDto(User user) {
        UserSignInResponseDto dto = new UserSignInResponseDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().getName().name() : null);
        dto.setPermissions(user.getRole() != null && user.getRole().getPermissions() != null 
            ? user.getRole().getPermissions().stream()
                .map(Enum::name)
                .collect(Collectors.toSet())
            : null);
        return dto;
    }

    public static UserGlobalResponseDto toUserGlobalResponseDto(User user) {
        UserGlobalResponseDto dto = new UserGlobalResponseDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().getName().name() : null);
        dto.setPermissions(user.getRole() != null && user.getRole().getPermissions() != null 
            ? user.getRole().getPermissions().stream()
                .map(Enum::name)
                .collect(Collectors.toSet())
            : null);
        // Map races only if the collection is initialized to avoid LazyInitializationException
        if (user.getRaces() != null && Hibernate.isInitialized(user.getRaces())) {
            user.getRaces().forEach(race -> {
                dto.getRaces().add(RaceMapper.toRaceResponseDto(race));
            });
        }
        return dto;
    }

    public static Set<UserGlobalResponseDto> toUserGlobalResponseDtoList(List<User> users) {
        return users.stream()
                .map(UserResponseMapper::toUserGlobalResponseDto)
                .collect(Collectors.toSet());
    }
}
