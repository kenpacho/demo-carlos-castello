package es.nextdigital.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Cuenta {

    @Id
    private String numeroCuenta;
    private List<Tarjeta> tarjetas;
    private List<Movimiento> movimientos;
    private float saldo;
}
