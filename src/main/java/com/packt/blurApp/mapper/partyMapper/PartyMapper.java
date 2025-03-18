package com.packt.blurApp.mapper.partyMapper;

import com.packt.blurApp.dto.Party.AddPartyDto;
import com.packt.blurApp.model.Party;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class PartyMapper {
  public abstract Party toEntity(AddPartyDto addPartyDto);

  public abstract AddPartyDto toDto(Party entity);

  public abstract List<AddPartyDto> toDtoList(List<Party> entities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "racesPlayed", ignore = true)
  public abstract Party update(AddPartyDto source, @MappingTarget Party target);
}