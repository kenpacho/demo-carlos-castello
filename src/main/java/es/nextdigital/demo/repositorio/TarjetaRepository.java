package es.nextdigital.demo.repositorio;

import es.nextdigital.demo.model.Movimiento;
import es.nextdigital.demo.model.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarjetaRepository extends JpaRepository<Tarjeta, String> {
}
