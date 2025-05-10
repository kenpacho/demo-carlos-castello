package es.nextdigital.demo.repositorio;

import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {
}
