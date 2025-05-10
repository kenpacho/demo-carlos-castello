package es.nextdigital.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Cliente {

    @Id
    private Integer clienteId;
    private String nombre;
    private List<Cuenta> cuentas;

}
