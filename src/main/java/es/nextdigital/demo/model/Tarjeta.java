package es.nextdigital.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Tarjeta {

    @Id
    private String codigoTarjeta;
    private int mesCaducidad;
    private int anoCaducidad;
    private int cvv;
    private int pin;
    private boolean activada;
    private int limiteRetirada;
    private TipoTarjeta tipoTarjeta;
}
