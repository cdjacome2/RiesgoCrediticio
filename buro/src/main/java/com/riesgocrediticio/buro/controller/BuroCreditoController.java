package com.riesgocrediticio.buro.controller;

import com.riesgocrediticio.buro.dto.response.ConsultaBuroCreditoResponse;
import com.riesgocrediticio.buro.exception.ClienteNoEncontradoException;
import com.riesgocrediticio.buro.service.BuroCreditoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/buro", produces = "application/json")
@Tag(name = "Buró Crediticio", description = "API para consultar información de buró crediticio interno y externo")
@Validated
public class BuroCreditoController {

    private final BuroCreditoService buroCreditoService;

    public BuroCreditoController(BuroCreditoService buroCreditoService) {
        this.buroCreditoService = buroCreditoService;
    }

    @Operation(
        summary = "Consulta información de buró crediticio por cédula",
        description = "Retorna todos los ingresos y egresos internos/externos del cliente, con mock si no existe información registrada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta exitosa",
            content = @Content(schema = @Schema(implementation = ConsultaBuroCreditoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado en el core",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @GetMapping("/consulta-por-cedula/{cedula}")
    public ResponseEntity<ConsultaBuroCreditoResponse> consultarPorCedula(
        @Parameter(description = "Cédula del cliente a consultar", example = "0102030405", required = true)
        @PathVariable @NotBlank String cedula) {

        log.debug("Solicitud recibida → Consulta de buró por cédula={}", cedula);
        try {
            ConsultaBuroCreditoResponse response = buroCreditoService.consultarPorCedula(cedula);
            log.info("Consulta de buró crediticio exitosa para cédula={}", cedula);
            return ResponseEntity.ok(response);

        } catch (ClienteNoEncontradoException ex) {
            log.warn("Cliente no encontrado en core para cédula={}: {}", cedula, ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception ex) {
            log.error("Error inesperado en la consulta de buró para cédula={}: {}", cedula, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(
        summary = "Sincroniza todos los clientes tipo PERSONA desde el core al buró interno",
        description = "Carga masiva de clientes del core. Devuelve un mensaje con el total de registros creados."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sincronización exitosa",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @PostMapping("/sincronizar-core")
    public ResponseEntity<String> sincronizarDesdeCore() {
        log.info("Solicitud recibida → Sincronización masiva desde el core");
        int creados = buroCreditoService.sincronizarClientesDesdeCore();
        String mensaje = "Se guardaron " + creados + " clientes del buró interno";
        log.info("Sincronización finalizada: {}", mensaje);
        return ResponseEntity.ok(mensaje);
    }

    @Operation(
        summary = "Cuenta el número de clientes PERSONA en el core",
        description = "Retorna el total de registros de clientes tipo PERSONA existentes en el microservicio core."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conteo exitoso",
            content = @Content(schema = @Schema(implementation = Integer.class))),
        @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @GetMapping("/count-core-personas")
    public ResponseEntity<Integer> contarPersonasCore() {
        log.info("Solicitud recibida → Conteo de clientes tipo PERSONA en el core");
        int total = buroCreditoService.contarPersonasEnCore();
        log.info("Total de personas tipo PERSONA en core: {}", total);
        return ResponseEntity.ok(total);
    }
}
