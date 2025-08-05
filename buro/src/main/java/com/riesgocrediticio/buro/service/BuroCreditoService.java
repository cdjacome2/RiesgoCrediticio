package com.riesgocrediticio.buro.service;

import com.riesgocrediticio.buro.client.ClienteBuroClient;
import com.riesgocrediticio.buro.dto.ClienteDto;
import com.riesgocrediticio.buro.dto.response.ConsultaBuroCreditoResponse;
import com.riesgocrediticio.buro.enums.MoraEnum;
import com.riesgocrediticio.buro.enums.MoraTresMesesEnum;
import com.riesgocrediticio.buro.enums.ProductoExternoEnum;
import com.riesgocrediticio.buro.enums.ProductoInternoEnum;
import com.riesgocrediticio.buro.exception.ClienteNoEncontradoException;
import com.riesgocrediticio.buro.mapper.EgresosExternoMapper;
import com.riesgocrediticio.buro.mapper.EgresosInternoMapper;
import com.riesgocrediticio.buro.mapper.IngresosExternoMapper;
import com.riesgocrediticio.buro.mapper.IngresosInternoMapper;
import com.riesgocrediticio.buro.model.EgresosExterno;
import com.riesgocrediticio.buro.model.EgresosInterno;
import com.riesgocrediticio.buro.model.IngresosExterno;
import com.riesgocrediticio.buro.model.IngresosInterno;
import com.riesgocrediticio.buro.repository.EgresosExternoRepository;
import com.riesgocrediticio.buro.repository.EgresosInternoRepository;
import com.riesgocrediticio.buro.repository.IngresosExternoRepository;
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
    private final IngresosExternoRepository ingresosExternoRepository;
    private final EgresosExternoRepository egresosExternoRepository;
    private final IngresosExternoMapper ingresosExternoMapper;
    private final EgresosExternoMapper egresosExternoMapper;

    public BuroCreditoService(
            ClienteBuroClient clienteBuroClient,
            IngresosInternoRepository ingresosInternoRepository,
            EgresosInternoRepository egresosInternoRepository,
            IngresosInternoMapper ingresosInternoMapper,
            EgresosInternoMapper egresosInternoMapper,
            IngresosExternoRepository ingresosExternoRepository,
            EgresosExternoRepository egresosExternoRepository,
            IngresosExternoMapper ingresosExternoMapper,
            EgresosExternoMapper egresosExternoMapper
    ) {
        this.clienteBuroClient = clienteBuroClient;
        this.ingresosInternoRepository = ingresosInternoRepository;
        this.egresosInternoRepository = egresosInternoRepository;
        this.ingresosInternoMapper = ingresosInternoMapper;
        this.egresosInternoMapper = egresosInternoMapper;
        this.ingresosExternoRepository = ingresosExternoRepository;
        this.egresosExternoRepository = egresosExternoRepository;
        this.ingresosExternoMapper = ingresosExternoMapper;
        this.egresosExternoMapper = egresosExternoMapper;
    }

    @Transactional(readOnly = true)
    public ConsultaBuroCreditoResponse consultarPorCedula(String cedula) {
        try {
            log.debug("Iniciando consulta de buró para cédula: {}", cedula);

            // Buscar en buró interno
            List<IngresosInterno> ingresosInternos = ingresosInternoRepository.findAllByCedulaCliente(cedula);
            List<EgresosInterno> egresosInternos = egresosInternoRepository.findAllByCedulaCliente(cedula);

            // Buscar en buró externo SOLO BANCO BANQUITO
            String BANCO_BANQUITO = "BANCO BANQUITO";
            List<IngresosExterno> ingresosExternos = ingresosExternoRepository.findAllByCedulaCliente(cedula)
                .stream()
                .filter(ie -> BANCO_BANQUITO.equalsIgnoreCase(ie.getInstitucionBancaria()))
                .toList();

            List<EgresosExterno> egresosExternos = egresosExternoRepository.findAllByCedulaCliente(cedula)
                .stream()
                .filter(ee -> BANCO_BANQUITO.equalsIgnoreCase(ee.getInstitucionBancaria()))
                .toList();

            boolean hayInterno = !ingresosInternos.isEmpty() || !egresosInternos.isEmpty();
            boolean hayExternoBanquito = !ingresosExternos.isEmpty() || !egresosExternos.isEmpty();

            // Si no hay en ninguno, lanzar excepción
            if (!hayInterno && !hayExternoBanquito) {
                log.warn("No se encontró información en el buro interno ni externo (BANCO BANQUITO) para cedula={}", cedula);
                throw new ClienteNoEncontradoException("El cliente no está registrado en el buro interno ni externo.");
            }

            if (hayInterno) {
                String nombre = ingresosInternos.stream().findFirst().map(IngresosInterno::getNombres)
                    .orElse(egresosInternos.stream().findFirst().map(EgresosInterno::getNombres).orElse(null));

                String calificacionRiesgo = calcularCalificacionRiesgo(ingresosInternos, egresosInternos);
                BigDecimal capacidadPago = calcularCapacidadPago(ingresosInternos, egresosInternos);

                log.info("Consulta exitosa de buró interno para cédula={}", cedula);
                return ConsultaBuroCreditoResponse.builder()
                    .nombreCliente(nombre)
                    .cedulaCliente(cedula)
                    .ingresosInternos(ingresosInternoMapper.toDtoList(ingresosInternos))
                    .egresosInternos(egresosInternoMapper.toDtoList(egresosInternos))
                    .ingresosExternos(Collections.emptyList())
                    .egresosExternos(Collections.emptyList())
                    .calificacionRiesgo(calificacionRiesgo)
                    .capacidadPago(capacidadPago)
                    .build();
            }


            // Si no hay en el interno, pero sí en el externo de BANCO BANQUITO
            String nombre = ingresosExternos.stream().findFirst().map(IngresosExterno::getNombres)
                .orElse(egresosExternos.stream().findFirst().map(EgresosExterno::getNombres).orElse(null));
            
            String calificacionRiesgo = calcularCalificacionRiesgo(ingresosExternos, egresosExternos);
            BigDecimal capacidadPago = calcularCapacidadPago(ingresosExternos, egresosExternos);

            log.info("Consulta exitosa de buró externo (BANCO BANQUITO) para cédula={}", cedula);
            return ConsultaBuroCreditoResponse.builder()
                .nombreCliente(nombre)
                .cedulaCliente(cedula)
                .ingresosInternos(Collections.emptyList())
                .egresosInternos(Collections.emptyList())
                .ingresosExternos(ingresosExternoMapper.toDtoList(ingresosExternos))
                .egresosExternos(egresosExternoMapper.toDtoList(egresosExternos))
                .calificacionRiesgo(calificacionRiesgo)
                .capacidadPago(capacidadPago)
                .build();

        } catch (ClienteNoEncontradoException ex) {
            log.warn("Cliente no encontrado: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado al consultar buró para cédula={}: {}", cedula, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Transactional
    public String sincronizarClientesDesdeCore() {
        log.info("Iniciando sincronización masiva de clientes PERSONA desde el core...");
        int creados = 0;
        int yaExistentes = 0;
        Random random = new Random();

        try {
            List<ClienteDto> personas = clienteBuroClient.listarPorTipoEntidad("PERSONA");

            for (ClienteDto cliente : personas) {
                String cedula = cliente.getNumeroIdentificacion();
                String nombre = cliente.getNombre();

                boolean existeIngreso = !ingresosInternoRepository.findAllByCedulaCliente(cedula).isEmpty();
                boolean existeEgreso = !egresosInternoRepository.findAllByCedulaCliente(cedula).isEmpty();

                if (!existeIngreso) {
                    ingresosInternoRepository.saveAll(mockIngresosInternos(cedula, nombre, random));
                    creados++;
                } else {
                    yaExistentes++;
                }

                if (!existeEgreso) {
                    egresosInternoRepository.saveAll(mockEgresosInternos(cedula, nombre, random));
                }
            }
            String mensaje = String.format(
                "Sincronización completada. Se crearon %d clientes nuevos en el buró interno. %d clientes ya estaban registrados.",
                creados, yaExistentes
            );
            log.info(mensaje);
            return mensaje;

        } catch (Exception ex) {
            log.error("Error durante la sincronización masiva del buró interno: {}", ex.getMessage(), ex);
            throw ex;
        }
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

    // METODOS PARA EL BURO EXTERNO
    @Transactional
    public String sincronizarClientesDesdeInternoAExterno() {
        log.info("Iniciando sincronización del buró interno al externo...");
        int creados = 0;
        int actualizados = 0;

        List<IngresosInterno> todosIngresos = ingresosInternoRepository.findAll();
        List<EgresosInterno> todosEgresos = egresosInternoRepository.findAll();

        Set<String> cedulasSincronizadas = new HashSet<>();

        // Procesa todos los clientes internos
        for (IngresosInterno ingreso : todosIngresos) {
            String cedula = ingreso.getCedulaCliente();
            String nombre = ingreso.getNombres();

            boolean existeIngresoExterno = !ingresosExternoRepository.findAllByCedulaCliente(cedula).isEmpty();
            boolean existeEgresoExterno = !egresosExternoRepository.findAllByCedulaCliente(cedula).isEmpty();

            // Solo crea si no existe; podrías actualizar si quieres mantener la info fresca cada mes
            if (!existeIngresoExterno) {
                IngresosExterno ingresoExt = new IngresosExterno();
                ingresoExt.setCedulaCliente(cedula);
                ingresoExt.setNombres(nombre);
                ingresoExt.setInstitucionBancaria(ingreso.getInstitucionBancaria());
                ingresoExt.setProducto(ingreso.getProducto());
                ingresoExt.setSaldoPromedioMes(ingreso.getSaldoPromedioMes());
                ingresoExt.setNumeroCuenta(ingreso.getNumeroCuenta());
                ingresoExt.setFechaActualizacion(ingreso.getFechaActualizacion());
                ingresoExt.setFechaRegistro(ingreso.getFechaRegistro());
                ingresoExt.setVersion(1L);
                ingresosExternoRepository.save(ingresoExt);
                creados++;
            } else {
                actualizados++;
                // aquí podrías actualizar datos si lo deseas
            }
            cedulasSincronizadas.add(cedula);
        }

        for (EgresosInterno egreso : todosEgresos) {
        String cedula = egreso.getCedulaCliente();
        String nombre = egreso.getNombres();

        // Busca si ya existe exactamente este egreso externo para evitar duplicados exactos
        boolean existeEseEgreso = egresosExternoRepository
            .findAllByCedulaCliente(cedula)
            .stream()
            .anyMatch(e -> 
                Objects.equals(e.getProducto(), egreso.getProducto().name()) &&
                Objects.equals(e.getSaldoPendiente(), egreso.getSaldoPendiente()) &&
                Objects.equals(e.getMesesPendientes(), egreso.getMesesPendientes()) &&
                Objects.equals(e.getCuotaPago(), egreso.getCuotaPago())
            );

        if (!existeEseEgreso) {
            EgresosExterno egresoExt = new EgresosExterno();
            egresoExt.setCedulaCliente(cedula);
            egresoExt.setNombres(nombre);
            egresoExt.setInstitucionBancaria(egreso.getInstitucionBancaria());
            egresoExt.setProducto(
                ProductoExternoEnum.valueOf(egreso.getProducto().name())
            );
            egresoExt.setSaldoPendiente(egreso.getSaldoPendiente());
            egresoExt.setMesesPendientes(egreso.getMesesPendientes());
            egresoExt.setCuotaPago(egreso.getCuotaPago());
            egresoExt.setMora(egreso.getMora());
            egresoExt.setMoraUltimosTresMeses(egreso.getMoraUltimosTresMeses());
            egresoExt.setFechaActualizacion(egreso.getFechaActualizacion());
            egresoExt.setFechaRegistro(egreso.getFechaRegistro());
            egresoExt.setVersion(1L);
            egresosExternoRepository.save(egresoExt);
        }
        cedulasSincronizadas.add(cedula);
    }
        String mensaje = String.format(
            "Sincronización buró externo completada. Se crearon %d registros nuevos. %d ya existían y fueron ignorados.",
            creados, actualizados
        );
        log.info(mensaje);
        return mensaje;
    }

    @Transactional
    public int generarClientesExternosMock(int cantidad) {
        log.info("Generando {} clientes externos inventados...", cantidad);
        int creados = 0;
        Random random = new Random();

        // Cedulas ya existentes
        Set<String> cedulasOcupadas = new HashSet<>();
        cedulasOcupadas.addAll(ingresosInternoRepository.findAll().stream().map(IngresosInterno::getCedulaCliente).toList());
        cedulasOcupadas.addAll(ingresosExternoRepository.findAll().stream().map(IngresosExterno::getCedulaCliente).toList());

        while (creados < cantidad) {
            String cedulaRandom = String.valueOf(1000000000L + Math.abs(random.nextLong() % 8999999999L)); // 10 dígitos
            if (cedulasOcupadas.contains(cedulaRandom)) continue;

            // Nombre ficticio
            String nombre = "Cliente Externo " + (creados + 1);

            // Mock ingresos y egresos (puedes adaptar tu lógica de mock aquí)
            IngresosExterno ingreso = new IngresosExterno();
            ingreso.setCedulaCliente(cedulaRandom);
            ingreso.setNombres(nombre);
            ingreso.setInstitucionBancaria("BANCO FICTICIO " + ((creados % 5) + 1));
            ingreso.setProducto("CUENTA DE AHORRO");
            ingreso.setSaldoPromedioMes(BigDecimal.valueOf(800 + random.nextInt(2000)));
            ingreso.setNumeroCuenta("200" + random.nextInt(9999999));
            ingreso.setFechaActualizacion(java.time.LocalDate.now());
            ingreso.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(14)));
            ingreso.setVersion(1L);
            ingresosExternoRepository.save(ingreso);

            EgresosExterno egreso = new EgresosExterno();
            egreso.setCedulaCliente(cedulaRandom);
            egreso.setNombres(nombre);
            egreso.setInstitucionBancaria(ingreso.getInstitucionBancaria());
            egreso.setProducto(ProductoExternoEnum.PRESTAMO);
            int mesesPrestamo = random.nextBoolean() ? 0 : (12 + random.nextInt(36));
            egreso.setMesesPendientes(mesesPrestamo);
            egreso.setSaldoPendiente(BigDecimal.valueOf(2000 + random.nextInt(6000)));
            egreso.setCuotaPago(BigDecimal.valueOf(100 + random.nextInt(500)));
            egreso.setMora(mesesPrestamo == 0 ? MoraEnum.NO : MoraEnum.SI);
            egreso.setMoraUltimosTresMeses(random.nextBoolean() ? MoraTresMesesEnum.SI : MoraTresMesesEnum.NO);
            egreso.setFechaActualizacion(java.time.LocalDate.now());
            egreso.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(14)));
            egreso.setVersion(1L);
            egresosExternoRepository.save(egreso);

            cedulasOcupadas.add(cedulaRandom);
            creados++;
        }
        log.info("Clientes externos inventados creados: {}", creados);
        return creados;
    }

    private String calcularCalificacionRiesgo(
        List<? extends Object> ingresos,
        List<? extends Object> egresos) {

        BigDecimal totalIngresos = ingresos.stream()
                .map(i -> {
                    if (i instanceof IngresosInterno ii) return ii.getSaldoPromedioMes();
                    if (i instanceof IngresosExterno ie) return ie.getSaldoPromedioMes();
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoPendiente = egresos.stream()
                .map(e -> {
                    if (e instanceof EgresosInterno ei) return ei.getSaldoPendiente();
                    if (e instanceof EgresosExterno ee) return ee.getSaldoPendiente();
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCuotas = egresos.stream()
                .map(e -> {
                    if (e instanceof EgresosInterno ei) return ei.getCuotaPago();
                    if (e instanceof EgresosExterno ee) return ee.getCuotaPago();
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean tieneMora = egresos.stream().anyMatch(e ->
            (e instanceof EgresosInterno ei && ei.getMora() != null && ei.getMora().toString().equalsIgnoreCase("SI")) ||
            (e instanceof EgresosExterno ee && ee.getMora() != null && ee.getMora().toString().equalsIgnoreCase("SI"))
        );
        boolean moraUltimosTresMeses = egresos.stream().anyMatch(e ->
            (e instanceof EgresosInterno ei && ei.getMoraUltimosTresMeses() != null && ei.getMoraUltimosTresMeses().toString().equalsIgnoreCase("SI")) ||
            (e instanceof EgresosExterno ee && ee.getMoraUltimosTresMeses() != null && ee.getMoraUltimosTresMeses().toString().equalsIgnoreCase("SI"))
        );
        int mesesPendientes = egresos.stream()
                .map(e -> {
                    if (e instanceof EgresosInterno ei) return Optional.ofNullable(ei.getMesesPendientes()).orElse(0);
                    if (e instanceof EgresosExterno ee) return Optional.ofNullable(ee.getMesesPendientes()).orElse(0);
                    return 0;
                })
                .max(Integer::compareTo)
                .orElse(0);

        // --- Reglas de la tabla ---

        // A+
        if (!tieneMora && saldoPendiente.compareTo(BigDecimal.ZERO) == 0 && totalIngresos.compareTo(new BigDecimal("2000")) > 0)
            return "A+";
        // A-
        if (!tieneMora && saldoPendiente.compareTo(BigDecimal.ZERO) == 0 && totalIngresos.compareTo(new BigDecimal("1000")) >= 0 && totalIngresos.compareTo(new BigDecimal("2000")) <= 0)
            return "A-";
        // B+
        if (!tieneMora && saldoPendiente.compareTo(BigDecimal.ZERO) > 0 &&
                saldoPendiente.compareTo(totalIngresos.multiply(new BigDecimal("0.25"))) < 0)
            return "B+";
        // B-
        if (!tieneMora && saldoPendiente.compareTo(totalIngresos.multiply(new BigDecimal("0.25"))) >= 0 &&
                saldoPendiente.compareTo(totalIngresos.multiply(new BigDecimal("0.5"))) < 0)
            return "B-";
        // C+
        if (!tieneMora && saldoPendiente.compareTo(totalIngresos.multiply(new BigDecimal("0.5"))) >= 0 &&
                saldoPendiente.compareTo(totalIngresos) < 0)
            return "C+";
        // C-
        if (!tieneMora && saldoPendiente.compareTo(totalIngresos) >= 0)
            return "C-";
        // D+
        if (tieneMora && mesesPendientes > 0 && totalCuotas.compareTo(totalIngresos) <= 0)
            return "D+";
        // D-
        if (tieneMora && mesesPendientes > 0 && totalCuotas.compareTo(totalIngresos) > 0)
            return "D-";
        // E+
        if (moraUltimosTresMeses && saldoPendiente.compareTo(totalIngresos) > 0)
            return "E+";
        // E-
        if (moraUltimosTresMeses && saldoPendiente.compareTo(totalIngresos.multiply(new BigDecimal("2"))) > 0 && mesesPendientes > 24)
            return "E-";

        return "S/R"; // Sin Regla encontrada
    }

    private BigDecimal calcularCapacidadPago(
        List<? extends Object> ingresos,
        List<? extends Object> egresos) {

    BigDecimal totalIngresos = ingresos.stream()
            .map(i -> {
                if (i instanceof IngresosInterno ii) return ii.getSaldoPromedioMes();
                if (i instanceof IngresosExterno ie) return ie.getSaldoPromedioMes();
                return BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalCuotas = egresos.stream()
            .map(e -> {
                if (e instanceof EgresosInterno ei) return ei.getCuotaPago();
                if (e instanceof EgresosExterno ee) return ee.getCuotaPago();
                return BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal diferencia = totalIngresos.subtract(totalCuotas);
    if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
        diferencia = BigDecimal.ZERO;
    }

        return diferencia.multiply(new BigDecimal("0.3")).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

}
