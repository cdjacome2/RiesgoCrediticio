package com.riesgocrediticio.buro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDto {
    private String id;
    private String tipoEntidad;
    private String idEntidad;
    private String nombre;
    private String nacionalidad;
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String tipoCliente;
    private String segmento;
    private String canalAfiliacion;
    private String comentarios;
    private String estado;
    private LocalDate fechaCreacion;
    private List<TelefonoClienteDto> telefonos;
    private List<DireccionClienteDto> direcciones;
    private ContactoTransaccionalClienteDto contactoTransaccional;

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TelefonoClienteDto {
        private String codigoArea;
        private String numero;
        private String tipo;
        private String estado;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DireccionClienteDto {
        private String codigoUbicacion;
        private String callePrincipal;
        private String numeracion;
        private String calleSecundaria;
        private String referencia;
        private String estado;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactoTransaccionalClienteDto {
        private String correo;
        private String telefono;
    }
}
