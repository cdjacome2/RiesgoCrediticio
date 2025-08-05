package com.riesgocrediticio.buro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.riesgocrediticio.buro.dto.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConsultaBuroCreditoResponse {
    private String nombreCliente;
    private String cedulaCliente;
    private List<IngresosInternoDto> ingresosInternos;
    private List<EgresosInternoDto> egresosInternos;
    private List<IngresosExternoDto> ingresosExternos;
    private List<EgresosExternoDto> egresosExternos;
    private String calificacionRiesgo;
    private BigDecimal capacidadPago;
}
