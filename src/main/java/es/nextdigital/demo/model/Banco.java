package es.nextdigital.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Banco {

    @Id
    private Integer bancoId;
    private String nombre;
    private float comisionRetirada;
    private float comisionTransferencia;
}
