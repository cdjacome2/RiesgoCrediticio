package com.riesgocrediticio.buro.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class IngresosExternoDto {
    private Long id;
    private String cedulaCliente;
    private String nombres;
    private String institucionBancaria;
    private String producto;
    private BigDecimal saldoPromedioMes;
    private String numeroCuenta;
    private LocalDate fechaActualizacion;
    private LocalDate fechaRegistro;
    private Long version;
}
