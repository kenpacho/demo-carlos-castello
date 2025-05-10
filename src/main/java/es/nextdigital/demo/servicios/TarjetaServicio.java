package es.nextdigital.demo.servicios;

public interface TarjetaServicio {

    float sacarDinero(String numeroTarjeta, float cantidad, String bancoCajero);

    void ingresarDinero(String numeroTarjeta, float cantidad, String bancoCajero);
}
