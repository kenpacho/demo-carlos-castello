package es.nextdigital.demo.servicios;

public interface TarjetaServicio {

    float sacarDinero(String numeroTarjeta, int pin, float cantidad, String bancoCajero);

    void ingresarDinero(String numeroTarjeta, int pin, float cantidad, String bancoCajero);

    void activarTarjeta(String numeroTarjeta, int pin);

    void cambiarPin(String numeroTarjeta, int pin, int nuevoPin);

    float consultarConfiguracion(String numeroTarjeta, int pin);

    void modificarConfiguracion(String numeroTarjeta, int pin, float limiteRetirada);
}
