package es.nextdigital.demo.repositorio;

import es.nextdigital.demo.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepository extends JpaRepository<Cuenta, String> {
}
