package es.nextdigital.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;

@Data
@Entity
public class Movimiento {

    @Id
    private int movimientoId;
    private Date fechaMovimiento;
    private TipoMovimiento tipoMovimiento;
    private float importe;
}
