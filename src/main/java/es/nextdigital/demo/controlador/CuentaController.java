package es.nextdigital.demo.controlador;

import es.nextdigital.demo.model.Movimiento;
import es.nextdigital.demo.servicios.CuentaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carlosbank/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaServicio cuentaServicio;

    @GetMapping("/{numeroCuenta}/movimientos")
    public List<Movimiento> listarMovimientos(@PathVariable String numeroCuenta) {
        return this.cuentaServicio.listarMovimientos(numeroCuenta);
    }

    @PostMapping("/{numeroCuenta}/transferencias")
    public void realizarTransferencia(
            @PathVariable String numeroCuenta,
            @RequestParam String ibanDestino,
            @RequestParam float cantidad) {
        this.cuentaServicio.realizarTransferencia(numeroCuenta, ibanDestino, cantidad);
    }
}
