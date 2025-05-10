package es.nextdigital.demo.servicios;

import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Movimiento;

import java.util.List;

public interface CuentaServicio {

    List<Movimiento> listarMovimientos(Integer cuentaId);

    boolean sacarDinero(Cuenta cuenta, float cantidad);
}
