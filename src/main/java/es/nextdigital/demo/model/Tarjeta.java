package es.nextdigital.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Tarjeta {

    @Id
    private String numeroTarjeta;
    private int mesCaducidad;
    private int anoCaducidad;
    private int cvv;
    private String pinEncriptado;
    private boolean activada;
    private float limiteRetirada;
    private float limiteCredito;
    private TipoTarjeta tipoTarjeta;

    @ManyToOne
    private Cuenta cuenta;
}
