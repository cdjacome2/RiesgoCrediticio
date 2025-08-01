package com.riesgocrediticio.buro.mapper;

import com.riesgocrediticio.buro.dto.EgresosExternoCreateDto;
import com.riesgocrediticio.buro.dto.EgresosExternoDto;
import com.riesgocrediticio.buro.dto.EgresosExternoUpdateDto;
import com.riesgocrediticio.buro.model.EgresosExterno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EgresosExternoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    EgresosExterno toEntity(EgresosExternoCreateDto createDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cedulaCliente", ignore = true)
    @Mapping(target = "nombres", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget EgresosExterno entity, EgresosExternoUpdateDto updateDto);

    EgresosExternoDto toDto(EgresosExterno entity);

    List<EgresosExternoDto> toDtoList(List<EgresosExterno> entities);
}
