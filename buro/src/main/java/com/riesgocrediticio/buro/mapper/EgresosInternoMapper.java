package com.riesgocrediticio.buro.mapper;

import com.riesgocrediticio.buro.dto.EgresosInternoCreateDto;
import com.riesgocrediticio.buro.dto.EgresosInternoDto;
import com.riesgocrediticio.buro.dto.EgresosInternoUpdateDto;
import com.riesgocrediticio.buro.model.EgresosInterno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EgresosInternoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    EgresosInterno toEntity(EgresosInternoCreateDto createDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cedulaCliente", ignore = true)
    @Mapping(target = "nombres", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget EgresosInterno entity, EgresosInternoUpdateDto updateDto);

    EgresosInternoDto toDto(EgresosInterno entity);

    List<EgresosInternoDto> toDtoList(List<EgresosInterno> entities);
}