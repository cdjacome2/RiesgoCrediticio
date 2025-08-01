package com.riesgocrediticio.buro.dto;

import com.riesgocrediticio.buro.enums.MoraEnum;
import com.riesgocrediticio.buro.enums.MoraTresMesesEnum;
import com.riesgocrediticio.buro.enums.ProductoInternoEnum;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EgresosInternoDto {
    private Long id;
    private String cedulaCliente;
    private String nombres;
    private String institucionBancaria;
    private ProductoInternoEnum producto;
    private BigDecimal saldoPendiente;
    private Integer mesesPendientes;
    private BigDecimal cuotaPago;
    private MoraEnum mora;
    private MoraTresMesesEnum moraUltimosTresMeses;
    private LocalDate fechaActualizacion;
    private LocalDate fechaRegistro;
    private Long version;
}
