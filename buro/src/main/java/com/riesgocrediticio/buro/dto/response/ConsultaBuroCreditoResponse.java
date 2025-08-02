package com.riesgocrediticio.buro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.riesgocrediticio.buro.dto.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaBuroCreditoResponse {
    private String nombreCliente;
    private String cedulaCliente;
    private List<IngresosInternoDto> ingresosInternos;
    private List<EgresosInternoDto> egresosInternos;
    private List<IngresosExternoDto> ingresosExternos;
    private List<EgresosExternoDto> egresosExternos;
}
