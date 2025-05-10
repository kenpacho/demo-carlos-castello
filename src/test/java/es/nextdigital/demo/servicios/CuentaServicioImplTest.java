package es.nextdigital.demo.servicios;

import es.nextdigital.demo.exceptions.NotFoundException;
import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Movimiento;
import es.nextdigital.demo.model.TipoMovimiento;
import es.nextdigital.demo.repositorio.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CuentaServicioImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private CuentaServicioImpl cuentaServicio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarMovimientos_deberiaRetornarLista_siCuentaExiste() {
        // Arrange
        Integer cuentaId = 1;
        Movimiento mov1 = new Movimiento();
        Movimiento mov2 = new Movimiento();

        Cuenta cuenta = new Cuenta();
        cuenta.setMovimientos(new ArrayList<>(Arrays.asList(mov1, mov2)));

        when(cuentaRepository.findById(cuentaId)).thenReturn(Optional.of(cuenta));

        // Act
        List<Movimiento> movimientos = cuentaServicio.listarMovimientos(cuentaId);

        // Assert
        assertNotNull(movimientos);
        assertEquals(2, movimientos.size());
    }

    @Test
    void listarMovimientos_deberiaLanzarExcepcion_siCuentaNoExiste() {
        // Arrange
        Integer cuentaId = 999;

        when(cuentaRepository.findById(cuentaId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> cuentaServicio.listarMovimientos(cuentaId)
        );

        assertEquals("La cuenta buscada no existe", ex.getMessage());
    }

    @Test
    void sacarDinero_deberiaActualizarSaldoYGuardarMovimiento() {
        // Arrange
        float saldoInicial = 1000f;
        float cantidad = 200f;

        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(saldoInicial);
        cuenta.setMovimientos(new ArrayList<>());

        // Act
        boolean resultado = cuentaServicio.sacarDinero(cuenta, cantidad);

        // Assert
        assertTrue(resultado);
        assertEquals(saldoInicial - cantidad, cuenta.getSaldo());
        assertEquals(1, cuenta.getMovimientos().size());

        Movimiento movimiento = cuenta.getMovimientos().get(0);
        assertEquals(cantidad, movimiento.getImporte());
        assertEquals(TipoMovimiento.RETIRADA, movimiento.getTipoMovimiento());
        assertNotNull(movimiento.getFechaMovimiento());

        verify(cuentaRepository).save(cuenta);
    }
}