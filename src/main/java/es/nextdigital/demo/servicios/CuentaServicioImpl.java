package es.nextdigital.demo.servicios;

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
    public List<Movimiento> listarMovimientos(final Integer cuentaId) {
        final Optional<Cuenta> cuenta = cuentaRepository.findById(cuentaId);

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
}
