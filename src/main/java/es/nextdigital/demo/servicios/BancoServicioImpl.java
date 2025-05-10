package es.nextdigital.demo.servicios;

import es.nextdigital.demo.exceptions.NotFoundException;
import es.nextdigital.demo.model.Banco;
import es.nextdigital.demo.repositorio.BancoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BancoServicioImpl implements BancoServicio {

    private final BancoRepository bancoRepository;

    @Override
    public Banco obtenerBanco(String nombreBanco) {

        final Optional<Banco> bancoOptional = this.bancoRepository.findBancosByNombre(nombreBanco);
        if (bancoOptional.isPresent()) {
            return bancoOptional.get();
        } else {
            throw new NotFoundException("El banco no existe");
        }
    }
}
