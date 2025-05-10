package es.nextdigital.demo.servicios;

import es.nextdigital.demo.common.ConstantData;
import es.nextdigital.demo.exceptions.ForbiddenOperationException;
import es.nextdigital.demo.exceptions.NotFoundException;
import es.nextdigital.demo.model.Banco;
import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Tarjeta;
import es.nextdigital.demo.model.TipoTarjeta;
import es.nextdigital.demo.repositorio.TarjetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TarjetaServicioImpl implements TarjetaServicio {

    private final TarjetaRepository tarjetaRepository;
    private final CuentaServicio cuentaServicio;
    private final BancoServicio bancoServicio;

    @Override
    public float sacarDinero(String numeroTarjeta, float cantidad, String bancoCajero) {
        final Optional<Tarjeta> tarjetaOptional = tarjetaRepository.findById(numeroTarjeta);

        if (tarjetaOptional.isPresent()) {
            final Tarjeta tarjeta = tarjetaOptional.get();
            final Cuenta cuenta = tarjeta.getCuenta();

            boolean puedeSacar;
            if (TipoTarjeta.DEBITO == tarjeta.getTipoTarjeta()) {
                puedeSacar = cantidad <= cuenta.getSaldo() && cantidad <= tarjeta.getLimiteRetirada();
            } else {
                puedeSacar = cantidad <= tarjeta.getLimiteCredito() && cantidad <= tarjeta.getLimiteRetirada();
            }

            if (puedeSacar) {
                if (cuentaServicio.sacarDinero(cuenta, cantidad)) {
                    float comisiones = 0f;

                    if (!ConstantData.MI_BANCO.equals(bancoCajero)) {
                        final Banco banco = bancoServicio.obtenerBanco(bancoCajero);
                        comisiones = banco.getComisionRetirada();
                    }

                    return comisiones;
                } else {
                    throw new ForbiddenOperationException("Hubo algun error sacando dinero");
                }
            } else {
                throw new ForbiddenOperationException("No se puede sacar dinero");
            }
        } else {
            throw new NotFoundException("Tarjeta no encontrada");
        }
    }
}
