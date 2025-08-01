package com.riesgocrediticio.buro.repository;

import com.riesgocrediticio.buro.model.EgresosInterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EgresosInternoRepository extends JpaRepository<EgresosInterno, Long> {

    List<EgresosInterno> findAllByCedulaCliente(String cedulaCliente);

    Optional<EgresosInterno> findTopByCedulaClienteOrderByFechaRegistroDesc(String cedulaCliente);

    boolean existsByCedulaCliente(String cedulaCliente);
}
