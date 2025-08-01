package com.riesgocrediticio.buro.repository;

import com.riesgocrediticio.buro.model.IngresosInterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngresosInternoRepository extends JpaRepository<IngresosInterno, Long> {

    List<IngresosInterno> findAllByCedulaCliente(String cedulaCliente);

    Optional<IngresosInterno> findTopByCedulaClienteOrderByFechaRegistroDesc(String cedulaCliente);

    boolean existsByCedulaCliente(String cedulaCliente);
}
