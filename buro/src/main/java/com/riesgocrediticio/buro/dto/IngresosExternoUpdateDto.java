package com.riesgocrediticio.buro.dto;

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
public class IngresosExternoUpdateDto {
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

    @Size(max = 16, message = "El producto no puede exceder los 16 caracteres")
    private String producto;

    private BigDecimal saldoPromedioMes;

    @Size(max = 20, message = "El número de cuenta no puede exceder los 20 caracteres")
    private String numeroCuenta;

    private LocalDate fechaActualizacion;
}
