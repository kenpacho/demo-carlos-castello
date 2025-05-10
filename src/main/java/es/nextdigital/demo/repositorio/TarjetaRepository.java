package es.nextdigital.demo.repositorio;

import es.nextdigital.demo.model.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TarjetaRepository extends JpaRepository<Tarjeta, String> {

    Optional<Tarjeta> findByNumeroAndPinEncriptado(String numero, String pinEncriptado);
}
