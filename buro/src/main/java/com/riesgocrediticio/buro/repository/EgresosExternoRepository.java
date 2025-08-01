package com.riesgocrediticio.buro.repository;

import com.riesgocrediticio.buro.model.EgresosExterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EgresosExternoRepository extends JpaRepository<EgresosExterno, Long> {

    List<EgresosExterno> findAllByCedulaCliente(String cedulaCliente);

    Optional<EgresosExterno> findTopByCedulaClienteOrderByFechaRegistroDesc(String cedulaCliente);

    boolean existsByCedulaCliente(String cedulaCliente);
}
