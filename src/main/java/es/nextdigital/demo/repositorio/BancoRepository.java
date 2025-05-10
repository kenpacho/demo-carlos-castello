package es.nextdigital.demo.repositorio;

import es.nextdigital.demo.model.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BancoRepository extends JpaRepository<Banco, Integer> {

    Optional<Banco> findBancosByNombre(String nombre);
}
