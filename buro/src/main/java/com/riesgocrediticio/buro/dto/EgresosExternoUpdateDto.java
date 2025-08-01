package com.riesgocrediticio.buro.dto;

import com.riesgocrediticio.buro.enums.MoraEnum;
import com.riesgocrediticio.buro.enums.MoraTresMesesEnum;
import com.riesgocrediticio.buro.enums.ProductoExternoEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EgresosExternoUpdateDto {
    @NotNull(message = "El ID no puede ser nulo")
    private Long id;

    @NotBlank(message = "La cédula del cliente no puede estar vacía")
    @Size(min = 10, max = 10, message = "La cédula del cliente debe tener 10 caracteres")
    private String cedulaCliente;

    @NotBlank(message = "Los nombres no pueden estar vacíos")
    @Size(max = 80, message = "Los nombres no pueden exceder los 80 caracteres")
    private String nombres;

    @NotBlank(message = "La institución bancaria no puede estar vacía")
    @Size(max = 35, message = "La institución bancaria no puede exceder los 35 caracteres")
    private String institucionBancaria;

    @NotNull(message = "El producto no puede ser nulo")
    private ProductoExternoEnum producto;

    private BigDecimal saldoPendiente;

    @Min(value = 0, message = "El número de meses pendientes debe ser al menos 0")
    private Integer mesesPendientes;

    private BigDecimal cuotaPago;

    @NotNull(message = "La mora no puede ser nula")
    private MoraEnum mora;

    @NotNull(message = "La mora de los últimos 3 meses no puede ser nula")
    private MoraTresMesesEnum moraUltimosTresMeses;

    private LocalDate fechaActualizacion;
}
