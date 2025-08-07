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
import java.math.RoundingMode;
//import java.time.LocalDate;
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
        
        // // Solo si hoy es domingo se ejecuta la sincronización
        // if (!esUltimoDiaDeLaSemana()) {
        //     log.info("Hoy no es fin de semana. No se realizará la carga de datos del buró interno.");
        //     return "Sincronización no realizada. Solo se realiza los fines de semana.";
        // }

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

                // *** SOLO CREA ingresos Y egresos SI EL CLIENTE ES NUEVO ***
                if (!existeIngreso && !existeEgreso) {
                    // Crear ingresos
                    List<IngresosInterno> ingresos = mockIngresosInternos(cedula, nombre, random);
                    ingresosInternoRepository.saveAll(ingresos);

                    // Sumar total de ingresos
                    BigDecimal totalIngresos = ingresos.stream()
                            .map(IngresosInterno::getSaldoPromedioMes)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Crear egresos (UNA SOLA VEZ)
                    List<EgresosInterno> egresos = mockEgresosInternos(cedula, nombre, random, totalIngresos);
                    egresosInternoRepository.saveAll(egresos);

                    creados++;
                } else {
                    yaExistentes++;
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
        ingreso.setSaldoPromedioMes(BigDecimal.valueOf(200 + random.nextInt(2800)));
        ingreso.setNumeroCuenta("100" + (random.nextInt(9000000) + 1000000));
        ingreso.setFechaActualizacion(java.time.LocalDate.now());
        ingreso.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(14)));
        ingreso.setVersion(1L);
        ingresos.add(ingreso);
        return ingresos;
    }

    private List<EgresosInterno> mockEgresosInternos(String cedula, String nombre, Random random, BigDecimal totalIngresos) {
        List<EgresosInterno> egresos = new ArrayList<>();
        BigDecimal sumaCuotas = BigDecimal.ZERO;

        // Tarjeta de crédito (máximo una)
        boolean tieneTarjeta = true; 
        if (tieneTarjeta) {
            EgresosInterno tarjeta = new EgresosInterno();
            tarjeta.setCedulaCliente(cedula);
            tarjeta.setNombres(nombre);
            tarjeta.setInstitucionBancaria("BANCO BANQUITO");
            tarjeta.setProducto(ProductoInternoEnum.TARJETA_DE_CREDITO);

            int mesesTarjeta = random.nextBoolean() ? 0 : (1 + random.nextInt(36));
            BigDecimal cuotaTarjeta = BigDecimal.ZERO;
            BigDecimal saldoTarjeta = BigDecimal.ZERO;

            if (mesesTarjeta > 0) {
                cuotaTarjeta = BigDecimal.valueOf(20 + random.nextInt(101)); // $20-$120
                saldoTarjeta = cuotaTarjeta.multiply(BigDecimal.valueOf(mesesTarjeta)).setScale(2, RoundingMode.HALF_UP);
            }

            // Validación para que no supere el ingreso
            if (sumaCuotas.add(cuotaTarjeta).compareTo(totalIngresos) <= 0) {
                tarjeta.setCuotaPago(cuotaTarjeta);
                sumaCuotas = sumaCuotas.add(cuotaTarjeta);
            } else {
                tarjeta.setCuotaPago(BigDecimal.ZERO);
                saldoTarjeta = BigDecimal.ZERO;
                mesesTarjeta = 0;
            }

            tarjeta.setSaldoPendiente(saldoTarjeta);
            tarjeta.setMesesPendientes(mesesTarjeta);
            tarjeta.setMora(mesesTarjeta == 0 ? MoraEnum.NO : MoraEnum.SI);
            tarjeta.setMoraUltimosTresMeses(random.nextBoolean() ? MoraTresMesesEnum.SI : MoraTresMesesEnum.NO);
            tarjeta.setFechaActualizacion(java.time.LocalDate.now());
            tarjeta.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(14)));
            tarjeta.setVersion(1L);
            egresos.add(tarjeta);
        }

        // Préstamo (máximo uno)
        boolean tienePrestamo = true; // O random.nextBoolean()
        if (tienePrestamo) {
            EgresosInterno prestamo = new EgresosInterno();
            prestamo.setCedulaCliente(cedula);
            prestamo.setNombres(nombre);
            prestamo.setInstitucionBancaria("BANCO BANQUITO");
            prestamo.setProducto(ProductoInternoEnum.PRESTAMO);

            int mesesPrestamo = random.nextBoolean() ? 0 : (12 + random.nextInt(36));
            BigDecimal cuotaPrestamo = BigDecimal.ZERO;
            BigDecimal saldoPrestamo = BigDecimal.ZERO;

            if (mesesPrestamo > 0) {
                cuotaPrestamo = BigDecimal.valueOf(100 + random.nextInt(401)); // $100-$500
                saldoPrestamo = cuotaPrestamo.multiply(BigDecimal.valueOf(mesesPrestamo)).setScale(2, RoundingMode.HALF_UP);
            }

            if (sumaCuotas.add(cuotaPrestamo).compareTo(totalIngresos) <= 0) {
                prestamo.setCuotaPago(cuotaPrestamo);
                sumaCuotas = sumaCuotas.add(cuotaPrestamo);
            } else {
                prestamo.setCuotaPago(BigDecimal.ZERO);
                saldoPrestamo = BigDecimal.ZERO;
                mesesPrestamo = 0;
            }

            prestamo.setSaldoPendiente(saldoPrestamo);
            prestamo.setMesesPendientes(mesesPrestamo);
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

    @Transactional(readOnly = true)
    public int contarClientesEnBuroInterno() {
        try {
            log.info("Contando clientes en el buro interno...");
            long totalClientes = ingresosInternoRepository.findAll()
                .stream()
                .map(i -> i.getCedulaCliente())
                .distinct()
                .count();
            log.info("Total de clientes en el buro interno: {}", totalClientes);
            return (int) totalClientes; // Convertir a entero, si es necesario.
        } catch (Exception ex) {
            log.error("Error al contar clientes en el buro interno: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    // METODOS PARA EL BURO EXTERNO
    @Transactional
    public String sincronizarClientesDesdeInternoAExterno() {
        log.info("Iniciando sincronización del buró interno al externo...");
        
        // // Solo si hoy es el último día del mes se ejecuta la sincronización
        // if (!esUltimoDiaDelMes()) {
        //     log.info("Hoy no es el último día del mes. No se realizará la carga de datos del buró externo.");
        //     return "Sincronización no realizada. Solo se realiza el último día del mes.";
        // }

        int creados = 0;

        List<IngresosInterno> todosIngresos = ingresosInternoRepository.findAll();
        List<EgresosInterno> todosEgresos = egresosInternoRepository.findAll();

        Set<String> cedulasSincronizadas = new HashSet<>();

        // Procesa todos los clientes internos
        for (IngresosInterno ingreso : todosIngresos) {
            String cedula = ingreso.getCedulaCliente();
            String nombre = ingreso.getNombres();

            boolean existeIngresoExterno = !ingresosExternoRepository.findAllByCedulaCliente(cedula).isEmpty();
            boolean existeEgresoExterno = !egresosExternoRepository.findAllByCedulaCliente(cedula).isEmpty();

            // Solo crea si no existe
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
            creados
        );
        log.info(mensaje);
        return mensaje;
    }

    @Transactional
    public int generarClientesExternosMock(int cantidad) {
        log.info("Generando {} clientes externos inventados...", cantidad);
        int creados = 0;
        Random random = new Random();

        // Arrays de nombres y apellidos realistas
        String[] nombres = {"Juan", "Elena", "Sofía", "Carlos", "María", "Sam", "José", "Ana", "Lucía", "Pedro", "Daniela", "Andrea", "David", "Cristina", "Mónica"};
        String[] apellidos = {"Ramírez", "Smith", "Ponce", "García", "Vera", "Torres", "Morales", "Mendoza", "Gómez", "López", "Martínez", "Díaz", "Castillo", "Jiménez", "Rojas"};

        // Cedulas ya existentes
        Set<String> cedulasOcupadas = new HashSet<>();
        cedulasOcupadas.addAll(ingresosInternoRepository.findAll().stream().map(IngresosInterno::getCedulaCliente).toList());
        cedulasOcupadas.addAll(ingresosExternoRepository.findAll().stream().map(IngresosExterno::getCedulaCliente).toList());

        String[] bancosFicticios = {
            "BANCO FICTICIO 1",
            "BANCO FICTICIO 2",
            "BANCO FICTICIO 3",
            "BANCO FICTICIO 4",
            "BANCO FICTICIO 5"
        };

        while (creados < cantidad) {
            String cedulaRandom = String.valueOf(1000000000L + Math.abs(random.nextLong() % 8999999999L)); // 10 dígitos
            if (cedulasOcupadas.contains(cedulaRandom)) continue;

            // Genera un nombre realista
            String nombre = nombres[random.nextInt(nombres.length)] + " " + apellidos[random.nextInt(apellidos.length)];

            // Cada cliente puede tener cuentas/egresos en 2 o 3 bancos ficticios diferentes
            List<String> bancosCliente = new ArrayList<>(Arrays.asList(bancosFicticios));
            Collections.shuffle(bancosCliente, random);
            int bancosMax = 2 + random.nextInt(2); // 2 o 3 bancos

            for (int b = 0; b < bancosMax; b++) {
                String banco = bancosCliente.get(b);

                // --- INGRESOS (solo una cuenta por banco) ---
                IngresosExterno ingreso = new IngresosExterno();
                ingreso.setCedulaCliente(cedulaRandom);
                ingreso.setNombres(nombre);
                ingreso.setInstitucionBancaria(banco);
                ingreso.setProducto("CUENTA DE AHORRO");
                ingreso.setSaldoPromedioMes(BigDecimal.valueOf(200 + random.nextInt(2800)));
                ingreso.setNumeroCuenta("200" + (random.nextInt(9000000) + 1000000));
                ingreso.setFechaActualizacion(java.time.LocalDate.now());
                ingreso.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(14)));
                ingreso.setVersion(1L);
                ingresosExternoRepository.save(ingreso);

                BigDecimal totalIngresos = ingreso.getSaldoPromedioMes();

                // --- EGRESOS ---
                List<EgresosExterno> egresos = new ArrayList<>();
                int tipoProducto = random.nextInt(3);
                BigDecimal sumaCuotas = BigDecimal.ZERO;

                // Tarjeta de crédito (solo una por banco)
                if (tipoProducto == 0 || tipoProducto == 2) {
                    EgresosExterno tarjeta = new EgresosExterno();
                    tarjeta.setCedulaCliente(cedulaRandom);
                    tarjeta.setNombres(nombre);
                    tarjeta.setInstitucionBancaria(banco);
                    tarjeta.setProducto(ProductoExternoEnum.TARJETA_DE_CREDITO);

                    BigDecimal saldoTarjeta = BigDecimal.valueOf(300 + random.nextInt(3700));
                    int mesesTarjeta = random.nextBoolean() ? 0 : (1 + random.nextInt(36));
                    tarjeta.setSaldoPendiente(mesesTarjeta > 0 ? saldoTarjeta : BigDecimal.ZERO);
                    tarjeta.setMesesPendientes(mesesTarjeta);

                    BigDecimal cuotaTarjeta = BigDecimal.ZERO;
                    if (mesesTarjeta > 0 && saldoTarjeta.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal cuotaMin = saldoTarjeta.multiply(new BigDecimal("0.03"));
                        BigDecimal cuotaMax = saldoTarjeta.multiply(new BigDecimal("0.10"));
                        cuotaTarjeta = cuotaMin.add(
                            new BigDecimal(Math.random()).multiply(cuotaMax.subtract(cuotaMin))
                        ).setScale(2, RoundingMode.HALF_UP);
                    }

                    if (sumaCuotas.add(cuotaTarjeta).compareTo(totalIngresos) <= 0) {
                        tarjeta.setCuotaPago(cuotaTarjeta);
                        sumaCuotas = sumaCuotas.add(cuotaTarjeta);
                    } else {
                        tarjeta.setCuotaPago(BigDecimal.ZERO);
                    }

                    tarjeta.setMora(mesesTarjeta == 0 ? MoraEnum.NO : MoraEnum.SI);
                    tarjeta.setMoraUltimosTresMeses(random.nextBoolean() ? MoraTresMesesEnum.SI : MoraTresMesesEnum.NO);
                    tarjeta.setFechaActualizacion(java.time.LocalDate.now());
                    tarjeta.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(14)));
                    tarjeta.setVersion(1L);
                    egresos.add(tarjeta);
                }

                // Préstamo (solo uno por banco)
                if (tipoProducto == 1 || tipoProducto == 2) {
                    EgresosExterno prestamo = new EgresosExterno();
                    prestamo.setCedulaCliente(cedulaRandom);
                    prestamo.setNombres(nombre);
                    prestamo.setInstitucionBancaria(banco);
                    prestamo.setProducto(ProductoExternoEnum.PRESTAMO);

                    int mesesPrestamo = random.nextBoolean() ? 0 : (12 + random.nextInt(36));
                    BigDecimal saldoPrestamo = mesesPrestamo > 0
                            ? BigDecimal.valueOf(2000 + random.nextInt(6000))
                            : BigDecimal.ZERO;

                    prestamo.setMesesPendientes(mesesPrestamo);
                    prestamo.setSaldoPendiente(saldoPrestamo);

                    BigDecimal cuotaPrestamo = BigDecimal.ZERO;
                    if (mesesPrestamo > 0 && saldoPrestamo.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal cuotaTeorica = saldoPrestamo.divide(BigDecimal.valueOf(mesesPrestamo), 2, RoundingMode.HALF_UP);
                        BigDecimal cuotaMin = cuotaTeorica.multiply(new BigDecimal("0.7"));
                        BigDecimal cuotaMax = cuotaTeorica;
                        cuotaPrestamo = cuotaMin.add(
                            new BigDecimal(Math.random()).multiply(cuotaMax.subtract(cuotaMin))
                        ).setScale(2, RoundingMode.HALF_UP);
                    }

                    if (sumaCuotas.add(cuotaPrestamo).compareTo(totalIngresos) <= 0) {
                        prestamo.setCuotaPago(cuotaPrestamo);
                        sumaCuotas = sumaCuotas.add(cuotaPrestamo);
                    } else {
                        prestamo.setCuotaPago(BigDecimal.ZERO);
                    }

                    prestamo.setMora(mesesPrestamo == 0 ? MoraEnum.NO : MoraEnum.SI);
                    prestamo.setMoraUltimosTresMeses(random.nextBoolean() ? MoraTresMesesEnum.SI : MoraTresMesesEnum.NO);
                    prestamo.setFechaActualizacion(java.time.LocalDate.now());
                    prestamo.setFechaRegistro(java.time.LocalDate.now().minusDays(random.nextInt(14)));
                    prestamo.setVersion(1L);
                    egresos.add(prestamo);
                }
                egresosExternoRepository.saveAll(egresos);
            }

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

        int mesesPendientes = egresos.stream()
                .map(e -> {
                    if (e instanceof EgresosInterno ei) return Optional.ofNullable(ei.getMesesPendientes()).orElse(0);
                    if (e instanceof EgresosExterno ee) return Optional.ofNullable(ee.getMesesPendientes()).orElse(0);
                    return 0;
                })
                .max(Integer::compareTo)
                .orElse(0);

        boolean tieneMora = egresos.stream().anyMatch(e ->
            (e instanceof EgresosInterno ei && ei.getMora() != null && ei.getMora().toString().equalsIgnoreCase("SI")) ||
            (e instanceof EgresosExterno ee && ee.getMora() != null && ee.getMora().toString().equalsIgnoreCase("SI"))
        );
        boolean moraUltimosTresMeses = egresos.stream().anyMatch(e ->
            (e instanceof EgresosInterno ei && ei.getMoraUltimosTresMeses() != null && ei.getMoraUltimosTresMeses().toString().equalsIgnoreCase("SI")) ||
            (e instanceof EgresosExterno ee && ee.getMoraUltimosTresMeses() != null && ee.getMoraUltimosTresMeses().toString().equalsIgnoreCase("SI"))
        );

        // ----- REGLA ESPECIAL para clientes sin deudas, ni cuotas, ni mora -----
        if (!tieneMora && saldoPendiente.compareTo(BigDecimal.ZERO) == 0 &&
            totalCuotas.compareTo(BigDecimal.ZERO) == 0 && mesesPendientes == 0) {

            if (totalIngresos.compareTo(new BigDecimal("2000")) > 0) return "A+";
            if (totalIngresos.compareTo(new BigDecimal("1000")) >= 0) return "A-";
            if (totalIngresos.compareTo(new BigDecimal("400")) >= 0) return "B";
            if (totalIngresos.compareTo(BigDecimal.ZERO) > 0) return "C";
            return "C-";
        }

        if (!tieneMora && saldoPendiente.compareTo(BigDecimal.ZERO) == 0) {
            if (totalIngresos.compareTo(new BigDecimal("2000")) > 0) return "A+";
            if (totalIngresos.compareTo(new BigDecimal("1000")) >= 0) return "A-";
            if (totalIngresos.compareTo(new BigDecimal("400")) >= 0) return "B";
            if (totalIngresos.compareTo(BigDecimal.ZERO) > 0) return "C";
            return "C-";
        }
        if (!tieneMora && saldoPendiente.compareTo(BigDecimal.ZERO) > 0 &&
                saldoPendiente.compareTo(totalIngresos.multiply(new BigDecimal("0.25"))) < 0)
            return "B+";
        if (!tieneMora && saldoPendiente.compareTo(totalIngresos.multiply(new BigDecimal("0.25"))) >= 0 &&
                saldoPendiente.compareTo(totalIngresos.multiply(new BigDecimal("0.5"))) < 0)
            return "B-";
        if (!tieneMora && saldoPendiente.compareTo(totalIngresos.multiply(new BigDecimal("0.5"))) >= 0 &&
                saldoPendiente.compareTo(totalIngresos) < 0)
            return "C+";
        if (!tieneMora && saldoPendiente.compareTo(totalIngresos) >= 0)
            return "C-";
        if (tieneMora && mesesPendientes > 0 && totalCuotas.compareTo(totalIngresos) <= 0)
            return "D+";
        if (tieneMora && mesesPendientes > 0 && totalCuotas.compareTo(totalIngresos) > 0)
            return "D-";
        if (moraUltimosTresMeses && saldoPendiente.compareTo(totalIngresos) > 0)
            return "E+";
        if (moraUltimosTresMeses && saldoPendiente.compareTo(totalIngresos.multiply(new BigDecimal("2"))) > 0 && mesesPendientes > 24)
            return "E-";

        return "C-";
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

    // // Lógica para verificar si es el último día de la semana (domingo)
    // private boolean esUltimoDiaDeLaSemana() {
    //     LocalDate today = LocalDate.now();
    //     //return today.getDayOfWeek().toString().equals("SUNDAY");
    //     return today.getDayOfWeek().toString().equals("WEDNESDAY");
    // }

    // // Lógica para verificar si hoy es el último día del mes
    // private boolean esUltimoDiaDelMes() {
    //     LocalDate today = LocalDate.now();
    //     //return today.getDayOfMonth() == today.lengthOfMonth();
    //     return today.getDayOfMonth() == 6;
    // }
}
