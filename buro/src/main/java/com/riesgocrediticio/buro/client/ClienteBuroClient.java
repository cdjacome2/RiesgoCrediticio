package com.riesgocrediticio.buro.client;

import com.riesgocrediticio.buro.dto.ClienteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "clientes", url = "${clientes.url}")
public interface ClienteBuroClient {

    @GetMapping("/api/v1/clientes/listar-por-tipo-entidad")
    List<ClienteDto> listarPorTipoEntidad(@RequestParam("tipoEntidad") String tipoEntidad);
}
