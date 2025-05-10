package es.nextdigital.demo.controlador;

import es.nextdigital.demo.servicios.TarjetaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carlosbank/api/tarjetas")
@RequiredArgsConstructor
public class TarjetaController {

    private final TarjetaServicio tarjetaServicio;

    @PostMapping("/{numeroTarjeta}/sacar")
    public float sacarDinero(
            @PathVariable String numeroTarjeta,
            @RequestParam int pin,
            @RequestParam float cantidad,
            @RequestParam String banco) {
        return this.tarjetaServicio.sacarDinero(numeroTarjeta, pin, cantidad, banco);
    }

    @PostMapping("/{numeroTarjeta}/ingresar")
    public void ingresarDinero(
            @PathVariable String numeroTarjeta,
            @RequestParam int pin,
            @RequestParam float cantidad,
            @RequestParam String banco) {
        this.tarjetaServicio.ingresarDinero(numeroTarjeta, pin, cantidad, banco);
    }

    @PostMapping("/{numeroTarjeta}/activar")
    public void activarTarjeta(
            @PathVariable String numeroTarjeta,
            @RequestParam int pin) {
        this.tarjetaServicio.activarTarjeta(numeroTarjeta, pin);
    }

    @PostMapping("/{numeroTarjeta}/cambiar-pin")
    public void cambiarPin(
            @PathVariable String numeroTarjeta,
            @RequestParam int pin,
            @RequestParam int nuevoPin) {
        this.tarjetaServicio.cambiarPin(numeroTarjeta, pin, nuevoPin);
    }

    @GetMapping("/{numeroTarjeta}/configuracion")
    public float consultarConfiguracion(
            @PathVariable String numeroTarjeta,
            @RequestParam int pin) {
        return this.tarjetaServicio.consultarConfiguracion(numeroTarjeta, pin);
    }

    @PostMapping("/{numeroTarjeta}/configuracion")
    public void modificarConfiguracion(
            @PathVariable String numeroTarjeta,
            @RequestParam int pin,
            @RequestParam float limiteRetirada) {
        this.tarjetaServicio.modificarConfiguracion(numeroTarjeta, pin, limiteRetirada);
    }
}
