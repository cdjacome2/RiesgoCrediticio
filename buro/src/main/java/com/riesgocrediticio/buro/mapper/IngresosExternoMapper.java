package com.riesgocrediticio.buro.mapper;

import com.riesgocrediticio.buro.dto.IngresosExternoCreateDto;
import com.riesgocrediticio.buro.dto.IngresosExternoDto;
import com.riesgocrediticio.buro.dto.IngresosExternoUpdateDto;
import com.riesgocrediticio.buro.model.IngresosExterno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface IngresosExternoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    IngresosExterno toEntity(IngresosExternoCreateDto createDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cedulaCliente", ignore = true)
    @Mapping(target = "nombres", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget IngresosExterno entity, IngresosExternoUpdateDto updateDto);

    IngresosExternoDto toDto(IngresosExterno entity);

    List<IngresosExternoDto> toDtoList(List<IngresosExterno> entities);
}
