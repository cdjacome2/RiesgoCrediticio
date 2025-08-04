package com.riesgocrediticio.buro.service;

import com.riesgocrediticio.buro.client.ClienteBuroClient;
import com.riesgocrediticio.buro.dto.ClienteDto;
import com.riesgocrediticio.buro.dto.response.ConsultaBuroCreditoResponse;
import com.riesgocrediticio.buro.enums.MoraEnum;
import com.riesgocrediticio.buro.enums.MoraTresMesesEnum;
import com.riesgocrediticio.buro.enums.ProductoInternoEnum;
import com.riesgocrediticio.buro.exception.ClienteNoEncontradoException;
import com.riesgocrediticio.buro.mapper.EgresosInternoMapper;
import com.riesgocrediticio.buro.mapper.IngresosInternoMapper;
import com.riesgocrediticio.buro.model.EgresosInterno;
import com.riesgocrediticio.buro.model.IngresosInterno;
import com.riesgocrediticio.buro.repository.EgresosInternoRepository;
import com.riesgocrediticio.buro.repository.IngresosInternoRepository;

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
    private final IngresosInternoMapper ingresosInternoMapper;
    private final EgresosInternoMapper egresosInternoMapper;

    public BuroCreditoService(
            ClienteBuroClient clienteBuroClient,
            IngresosInternoRepository ingresosInternoRepository,
            EgresosInternoRepository egresosInternoRepository,
            IngresosInternoMapper ingresosInternoMapper,
            EgresosInternoMapper egresosInternoMapper
    ) {
        this.clienteBuroClient = clienteBuroClient;
        this.ingresosInternoRepository = ingresosInternoRepository;
        this.egresosInternoRepository = egresosInternoRepository;
        this.ingresosInternoMapper = ingresosInternoMapper;
        this.egresosInternoMapper = egresosInternoMapper;
    }

    @Transactional(readOnly = true)
    public ConsultaBuroCreditoResponse consultarPorCedula(String cedula) {
        try {
            log.debug("Iniciando consulta de buró interno para cédula: {}", cedula);

            List<IngresosInterno> ingresosInternos = ingresosInternoRepository.findAllByCedulaCliente(cedula);
            List<EgresosInterno> egresosInternos = egresosInternoRepository.findAllByCedulaCliente(cedula);

            boolean hayDatos = !ingresosInternos.isEmpty() || !egresosInternos.isEmpty();

            if (!hayDatos) {
                log.warn("No se encontró información en el buró interno para cédula={}", cedula);
                throw new ClienteNoEncontradoException("El cliente no está registrado en el buró interno.");
            }

            String nombre = ingresosInternos.stream().findFirst().map(IngresosInterno::getNombres)
                    .orElse(egresosInternos.stream().findFirst().map(EgresosInterno::getNombres)
                    .orElse(null));

            log.info("Consulta exitosa de buró interno para cédula={}", cedula);

            return ConsultaBuroCreditoResponse.builder()
                    .nombreCliente(nombre)
                    .cedulaCliente(cedula)
                    .ingresosInternos(ingresosInternoMapper.toDtoList(ingresosInternos))
                    .egresosInternos(egresosInternoMapper.toDtoList(egresosInternos))
                    .ingresosExternos(Collections.emptyList())
                    .egresosExternos(Collections.emptyList())
                    .build();
        } catch (ClienteNoEncontradoException ex) {
            log.warn("Cliente no encontrado: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado al consultar buró interno para cédula={}: {}", cedula, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Transactional
    public int sincronizarClientesDesdeCore() {
        log.info("Iniciando sincronización masiva de clientes PERSONA desde el core...");
        int creados = 0;
        Random random = new Random();

        try {
            List<ClienteDto> personas = clienteBuroClient.listarPorTipoEntidad("PERSONA");

            for (ClienteDto cliente : personas) {
                String cedula = cliente.getNumeroIdentificacion();
                String nombre = cliente.getNombre();

                if (ingresosInternoRepository.findAllByCedulaCliente(cedula).isEmpty()) {
                    ingresosInternoRepository.saveAll(mockIngresosInternos(cedula, nombre, random));
                    creados++;
                }
                if (egresosInternoRepository.findAllByCedulaCliente(cedula).isEmpty()) {
                    egresosInternoRepository.saveAll(mockEgresosInternos(cedula, nombre, random));
                }
            }
            log.info("Sincronización terminada. Total creados: {}", creados);

        } catch (Exception ex) {
            log.error("Error durante la sincronización masiva del buró interno: {}", ex.getMessage(), ex);
            throw ex;
        }
        return creados;
    }

    private List<IngresosInterno> mockIngresosInternos(String cedula, String nombre, Random random) {
        List<IngresosInterno> ingresos = new ArrayList<>();
        IngresosInterno ingreso = new IngresosInterno();
        ingreso.setCedulaCliente(cedula);
        ingreso.setNombres(nombre);
        ingreso.setInstitucionBancaria("BANCO BANQUITO");
        ingreso.setProducto("CUENTA DE AHORRO");
        ingreso.setSaldoPromedioMes(BigDecimal.valueOf(800 + random.nextInt(2000)));
        ingreso.setNumeroCuenta("100" + random.nextInt(9999999));
        ingreso.setFechaActualizacion(java.time.LocalDate.now());
        ingreso.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(14)));
        ingreso.setVersion(1L);
        ingresos.add(ingreso);
        return ingresos;
    }

    private List<EgresosInterno> mockEgresosInternos(String cedula, String nombre, Random random) {
        List<EgresosInterno> egresos = new ArrayList<>();
        int tipoProducto = random.nextInt(3);

        // Tarjeta de crédito (máximo una)
        if (tipoProducto == 0 || tipoProducto == 2) {
            EgresosInterno tarjeta = new EgresosInterno();
            tarjeta.setCedulaCliente(cedula);
            tarjeta.setNombres(nombre);
            tarjeta.setInstitucionBancaria("BANCO BANQUITO");
            tarjeta.setProducto(ProductoInternoEnum.TARJETA_DE_CREDITO);
            tarjeta.setSaldoPendiente(BigDecimal.valueOf(300 + random.nextInt(3700)));
            int mesesTarjeta = random.nextBoolean() ? 0 : (1 + random.nextInt(36)); // 0 o mayor
            tarjeta.setMesesPendientes(mesesTarjeta);
            tarjeta.setCuotaPago(BigDecimal.valueOf(20 + random.nextInt(80)));
            tarjeta.setMora(mesesTarjeta == 0 ? MoraEnum.NO : MoraEnum.SI);
            tarjeta.setMoraUltimosTresMeses(random.nextBoolean() ? MoraTresMesesEnum.SI : MoraTresMesesEnum.NO);
            tarjeta.setFechaActualizacion(java.time.LocalDate.now());
            tarjeta.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(14)));
            tarjeta.setVersion(1L);
            egresos.add(tarjeta);
        }

        // Préstamo vehicular (máximo uno)
        if (tipoProducto == 1 || tipoProducto == 2) {
            EgresosInterno prestamo = new EgresosInterno();
            prestamo.setCedulaCliente(cedula);
            prestamo.setNombres(nombre);
            prestamo.setInstitucionBancaria("BANCO BANQUITO");
            prestamo.setProducto(ProductoInternoEnum.PRESTAMO);
            int mesesPrestamo = random.nextBoolean() ? 0 : (12 + random.nextInt(36));
            prestamo.setMesesPendientes(mesesPrestamo);
            prestamo.setSaldoPendiente(BigDecimal.valueOf(2000 + random.nextInt(6000)));
            prestamo.setCuotaPago(BigDecimal.valueOf(100 + random.nextInt(500)));
            prestamo.setMora(mesesPrestamo == 0 ? MoraEnum.NO : MoraEnum.SI);
            prestamo.setMoraUltimosTresMeses(random.nextBoolean() ? MoraTresMesesEnum.SI : MoraTresMesesEnum.NO);
            prestamo.setFechaActualizacion(java.time.LocalDate.now());
            prestamo.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(14)));
            prestamo.setVersion(1L);
            egresos.add(prestamo);
        }
        return egresos;
    }

    @Transactional(readOnly = true)
    public int contarPersonasEnCore() {
        try {
            List<ClienteDto> personas = clienteBuroClient.listarPorTipoEntidad("PERSONA");
            int total = personas.size();
            log.info("Total de clientes PERSONA en el core: {}", total);
            return total;
        } catch (Exception ex) {
            log.error("Error al contar personas en el core: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
