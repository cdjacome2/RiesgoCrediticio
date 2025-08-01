package com.riesgocrediticio.buro.mapper;

import com.riesgocrediticio.buro.dto.IngresosInternoCreateDto;
import com.riesgocrediticio.buro.dto.IngresosInternoDto;
import com.riesgocrediticio.buro.dto.IngresosInternoUpdateDto;
import com.riesgocrediticio.buro.model.IngresosInterno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface IngresosInternoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    IngresosInterno toEntity(IngresosInternoCreateDto createDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cedulaCliente", ignore = true)
    @Mapping(target = "nombres", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget IngresosInterno entity, IngresosInternoUpdateDto updateDto);

    IngresosInternoDto toDto(IngresosInterno entity);

    List<IngresosInternoDto> toDtoList(List<IngresosInterno> entities);
}