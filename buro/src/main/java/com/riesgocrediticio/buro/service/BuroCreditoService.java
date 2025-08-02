package com.riesgocrediticio.buro.service;

import com.riesgocrediticio.buro.client.ClienteBuroClient;
import com.riesgocrediticio.buro.dto.ClienteDto;
import com.riesgocrediticio.buro.dto.response.ConsultaBuroCreditoResponse;
import com.riesgocrediticio.buro.enums.MoraEnum;
import com.riesgocrediticio.buro.enums.MoraTresMesesEnum;
import com.riesgocrediticio.buro.enums.ProductoExternoEnum;
import com.riesgocrediticio.buro.enums.ProductoInternoEnum;
import com.riesgocrediticio.buro.exception.ClienteNoEncontradoException;
import com.riesgocrediticio.buro.mapper.*;
import com.riesgocrediticio.buro.model.*;
import com.riesgocrediticio.buro.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class BuroCreditoService {

    private final ClienteBuroClient clienteBuroClient;
    private final IngresosInternoRepository ingresosInternoRepository;
    private final EgresosInternoRepository egresosInternoRepository;
    private final IngresosExternoRepository ingresosExternoRepository;
    private final EgresosExternoRepository egresosExternoRepository;
    private final IngresosInternoMapper ingresosInternoMapper;
    private final EgresosInternoMapper egresosInternoMapper;
    private final IngresosExternoMapper ingresosExternoMapper;
    private final EgresosExternoMapper egresosExternoMapper;

    public BuroCreditoService(
            ClienteBuroClient clienteBuroClient,
            IngresosInternoRepository ingresosInternoRepository,
            EgresosInternoRepository egresosInternoRepository,
            IngresosExternoRepository ingresosExternoRepository,
            EgresosExternoRepository egresosExternoRepository,
            IngresosInternoMapper ingresosInternoMapper,
            EgresosInternoMapper egresosInternoMapper,
            IngresosExternoMapper ingresosExternoMapper,
            EgresosExternoMapper egresosExternoMapper
    ) {
        this.clienteBuroClient = clienteBuroClient;
        this.ingresosInternoRepository = ingresosInternoRepository;
        this.egresosInternoRepository = egresosInternoRepository;
        this.ingresosExternoRepository = ingresosExternoRepository;
        this.egresosExternoRepository = egresosExternoRepository;
        this.ingresosInternoMapper = ingresosInternoMapper;
        this.egresosInternoMapper = egresosInternoMapper;
        this.ingresosExternoMapper = ingresosExternoMapper;
        this.egresosExternoMapper = egresosExternoMapper;
    }

    @Transactional
    public ConsultaBuroCreditoResponse consultarPorCedula(String cedula) {
        log.info("Iniciando consulta de buró de crédito para cédula {}", cedula);

        try {
            // 1. Obtener cliente PERSONA del core
            log.debug("Consultando cliente en microservicio core");
            List<ClienteDto> personas = clienteBuroClient.listarPorTipoEntidad("PERSONA");
            ClienteDto cliente = personas.stream()
                    .filter(p -> p.getNumeroIdentificacion().equals(cedula))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("Cliente no encontrado en core con cédula {}", cedula);
                        return new ClienteNoEncontradoException("Cliente no encontrado con cédula: " + cedula);
                    });

            // 2. Buscar y/o poblar ingresos y egresos internos
            List<IngresosInterno> ingresosInternos;
            try {
                ingresosInternos = ingresosInternoRepository.findAllByCedulaCliente(cedula);
                if (ingresosInternos.isEmpty()) {
                    log.info("No existen ingresos internos para {}, generando mock y guardando...", cedula);
                    ingresosInternos = mockIngresosInternos(cedula, cliente.getNombre());
                    ingresosInternos = ingresosInternoRepository.saveAll(ingresosInternos);
                }
            } catch (Exception e) {
                log.error("Error accediendo a ingresos internos: {}", e.getMessage(), e);
                throw e;
            }

            List<EgresosInterno> egresosInternos;
            try {
                egresosInternos = egresosInternoRepository.findAllByCedulaCliente(cedula);
                if (egresosInternos.isEmpty()) {
                    log.info("No existen egresos internos para {}, generando mock y guardando...", cedula);
                    egresosInternos = mockEgresosInternos(cedula, cliente.getNombre());
                    egresosInternos = egresosInternoRepository.saveAll(egresosInternos);
                }
            } catch (Exception e) {
                log.error("Error accediendo a egresos internos: {}", e.getMessage(), e);
                throw e;
            }

            // 3. Buscar y/o poblar ingresos y egresos externos
            List<IngresosExterno> ingresosExternos;
            try {
                ingresosExternos = ingresosExternoRepository.findAllByCedulaCliente(cedula);
                if (ingresosExternos.isEmpty()) {
                    log.info("No existen ingresos externos para {}, generando mock y guardando...", cedula);
                    ingresosExternos = mockIngresosExternos(cedula, cliente.getNombre());
                    ingresosExternos = ingresosExternoRepository.saveAll(ingresosExternos);
                }
            } catch (Exception e) {
                log.error("Error accediendo a ingresos externos: {}", e.getMessage(), e);
                throw e;
            }

            List<EgresosExterno> egresosExternos;
            try {
                egresosExternos = egresosExternoRepository.findAllByCedulaCliente(cedula);
                if (egresosExternos.isEmpty()) {
                    log.info("No existen egresos externos para {}, generando mock y guardando...", cedula);
                    egresosExternos = mockEgresosExternos(cedula, cliente.getNombre());
                    egresosExternos = egresosExternoRepository.saveAll(egresosExternos);
                }
            } catch (Exception e) {
                log.error("Error accediendo a egresos externos: {}", e.getMessage(), e);
                throw e;
            }

            // 4. Mapear a DTOs y armar la respuesta final
            log.info("Consulta de buró exitosa para {}", cedula);
            return ConsultaBuroCreditoResponse.builder()
                    .nombreCliente(cliente.getNombre())
                    .cedulaCliente(cliente.getNumeroIdentificacion())
                    .ingresosInternos(ingresosInternoMapper.toDtoList(ingresosInternos))
                    .egresosInternos(egresosInternoMapper.toDtoList(egresosInternos))
                    .ingresosExternos(ingresosExternoMapper.toDtoList(ingresosExternos))
                    .egresosExternos(egresosExternoMapper.toDtoList(egresosExternos))
                    .build();

        } catch (ClienteNoEncontradoException e) {
            log.error("ClienteNoEncontradoException: {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error("Error inesperado en consulta de buró de crédito para cédula {}: {}", cedula, ex.getMessage(), ex);
            throw ex; // Se puede personalizar o envolver en excepción propia si lo deseas
        }
    }

    // ---------- MOCK GENERATORS -----------

    private List<IngresosInterno> mockIngresosInternos(String cedula, String nombre) {
        Random random = new Random();
        List<IngresosInterno> ingresos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            IngresosInterno ingreso = new IngresosInterno();
            ingreso.setCedulaCliente(cedula);
            ingreso.setNombres(nombre);
            ingreso.setInstitucionBancaria("BANCO BANQUITO");
            ingreso.setProducto("CUENTA DE AHORRO");
            ingreso.setSaldoPromedioMes(BigDecimal.valueOf(500 + random.nextInt(1500)));
            ingreso.setNumeroCuenta("100" + random.nextInt(9999999));
            ingreso.setFechaActualizacion(java.time.LocalDate.now());
            ingreso.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(100)));
            ingreso.setVersion(1L);
            ingresos.add(ingreso);
        }
        return ingresos;
    }

    private List<IngresosExterno> mockIngresosExternos(String cedula, String nombre) {
        Random random = new Random();
        List<IngresosExterno> ingresos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            IngresosExterno ingreso = new IngresosExterno();
            ingreso.setCedulaCliente(cedula);
            ingreso.setNombres(nombre);
            ingreso.setInstitucionBancaria("BANCO DEL PACIFICO");
            ingreso.setProducto("CUENTA DE AHORRO");
            ingreso.setSaldoPromedioMes(BigDecimal.valueOf(1000 + random.nextInt(3000)));
            ingreso.setNumeroCuenta("200" + random.nextInt(9999999));
            ingreso.setFechaActualizacion(java.time.LocalDate.now());
            ingreso.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(200)));
            ingreso.setVersion(1L);
            ingresos.add(ingreso);
        }
        return ingresos;
    }

    private List<EgresosInterno> mockEgresosInternos(String cedula, String nombre) {
        Random random = new Random();
        List<EgresosInterno> egresos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            EgresosInterno egreso = new EgresosInterno();
            egreso.setCedulaCliente(cedula);
            egreso.setNombres(nombre);
            egreso.setInstitucionBancaria("BANCO BANQUITO");
            egreso.setProducto(ProductoInternoEnum.PRESTAMO);
            egreso.setSaldoPendiente(BigDecimal.valueOf(1000 + random.nextInt(4000)));
            egreso.setMesesPendientes(random.nextInt(36) + 1);
            egreso.setCuotaPago(BigDecimal.valueOf(50 + random.nextInt(450)));
            egreso.setMora(MoraEnum.NO);
            egreso.setMoraUltimosTresMeses(MoraTresMesesEnum.NO);
            egreso.setFechaActualizacion(java.time.LocalDate.now());
            egreso.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(100)));
            egreso.setVersion(1L);
            egresos.add(egreso);
        }
        return egresos;
    }

    private List<EgresosExterno> mockEgresosExternos(String cedula, String nombre) {
        Random random = new Random();
        List<EgresosExterno> egresos = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            EgresosExterno egreso = new EgresosExterno();
            egreso.setCedulaCliente(cedula);
            egreso.setNombres(nombre);
            egreso.setInstitucionBancaria("BANCO PICHINCHA");
            egreso.setProducto(ProductoExternoEnum.PRESTAMO);
            egreso.setSaldoPendiente(BigDecimal.valueOf(2000 + random.nextInt(6000)));
            egreso.setMesesPendientes(random.nextInt(48) + 1);
            egreso.setCuotaPago(BigDecimal.valueOf(100 + random.nextInt(600)));
            egreso.setMora(MoraEnum.NO);
            egreso.setMoraUltimosTresMeses(MoraTresMesesEnum.NO);
            egreso.setFechaActualizacion(java.time.LocalDate.now());
            egreso.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(200)));
            egreso.setVersion(1L);
            egresos.add(egreso);
        }
        return egresos;
    }
}
