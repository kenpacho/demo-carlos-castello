package es.nextdigital.demo.servicios;

import es.nextdigital.demo.common.ConstantData;
import es.nextdigital.demo.exceptions.NotFoundException;
import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Movimiento;
import es.nextdigital.demo.model.TipoMovimiento;
import es.nextdigital.demo.repositorio.CuentaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CuentaServicioImpl implements CuentaServicio {

    private final CuentaRepository cuentaRepository;

    @Override
    public List<Movimiento> listarMovimientos(final String numeroCuenta) {
        final Optional<Cuenta> cuenta = cuentaRepository.findById(numeroCuenta);

        if (cuenta.isPresent()) {
            return cuenta.get().getMovimientos();
        } else {
            throw new NotFoundException("La cuenta buscada no existe");
        }
    }

    @Transactional
    @Override
    public boolean sacarDinero(final Cuenta cuenta, final float cantidad) {
        cuenta.setSaldo(cuenta.getSaldo() - cantidad);

        final Movimiento movimiento = new Movimiento();
        movimiento.setFechaMovimiento(new Date());
        movimiento.setTipoMovimiento(TipoMovimiento.RETIRADA);
        movimiento.setImporte(cantidad);
        cuenta.getMovimientos().add(movimiento);

        this.cuentaRepository.save(cuenta);
        return true;
    }

    @Override
    public void ingresarDinero(final Cuenta cuenta, final float cantidad) {
        cuenta.setSaldo(cuenta.getSaldo() + cantidad);

        final Movimiento movimiento = new Movimiento();
        movimiento.setFechaMovimiento(new Date());
        movimiento.setTipoMovimiento(TipoMovimiento.INGRESO);
        movimiento.setImporte(cantidad);
        cuenta.getMovimientos().add(movimiento);

        this.cuentaRepository.save(cuenta);
    }

    @Transactional
    @Override
    public void realizarTransferencia(final String numeroCuentaOrigen, String ibanDestino, final float cantidad) {
        final Optional<Cuenta> cuentaOrigenOptional = cuentaRepository.findById(numeroCuentaOrigen);
        if (!cuentaOrigenOptional.isPresent()) {
            throw new NotFoundException("La cuenta origen no existe");
        }

        final Cuenta cuentaOrigen = cuentaOrigenOptional.get();
        final Optional<Cuenta> cuentaDestinoOptional = cuentaRepository.findById(ibanDestino);
        float comisionFactor = 1f; // No tener comision es multiplicar por factor 1
        if (!cuentaDestinoOptional.isPresent()) {
            if (esIBANValido(ibanDestino)) {
                // Asumo que la comision de transferencia es la misma para cualquier banco. Si no, deberia consultar
                // con un servicio externo a que banco pertenece el IBAN.
                comisionFactor = ConstantData.COMISION_TRANSFERENCIA;
            }
        }

        final float cantidadTransferencia = cantidad * comisionFactor;
        if (cuentaOrigen.getSaldo() >= cantidadTransferencia) {
            cuentaOrigen.setSaldo(cuentaOrigen.getSaldo() - cantidadTransferencia);

            final Movimiento movimiento = new Movimiento();
            movimiento.setFechaMovimiento(new Date());
            movimiento.setTipoMovimiento(TipoMovimiento.TRANSFERENCIA_SALIENTE);
            movimiento.setImporte(cantidadTransferencia);
            cuentaOrigen.getMovimientos().add(movimiento);

            this.cuentaRepository.save(cuentaOrigen);
        }
    }

    private boolean esIBANValido(final String iban) {
        // Como es para una prueba vamos a considerar como no validos cualquier iban que no sea de españa
        return iban.startsWith("ES");
    }
}
