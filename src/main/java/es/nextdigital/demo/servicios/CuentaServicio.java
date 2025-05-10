package es.nextdigital.demo.servicios;

import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Movimiento;

import java.util.List;

public interface CuentaServicio {

    List<Movimiento> listarMovimientos(String numeroCuenta);

    boolean sacarDinero(Cuenta cuenta, float cantidad);

    void ingresarDinero(Cuenta cuenta, float cantidad);

    void realizarTransferencia(String numeroCuentaOrigen, String ibanDestino, float cantidad);
}
