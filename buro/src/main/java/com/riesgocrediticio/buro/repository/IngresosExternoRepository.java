package com.riesgocrediticio.buro.repository;

import com.riesgocrediticio.buro.model.IngresosExterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngresosExternoRepository extends JpaRepository<IngresosExterno, Long> {

    List<IngresosExterno> findAllByCedulaCliente(String cedulaCliente);

    Optional<IngresosExterno> findTopByCedulaClienteOrderByFechaRegistroDesc(String cedulaCliente);

    boolean existsByCedulaCliente(String cedulaCliente);
}

