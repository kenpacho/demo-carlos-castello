package es.nextdigital.demo.servicios;

import es.nextdigital.demo.common.ConstantData;
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
import static org.mockito.Mockito.*;

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
        String numeroCuenta = "ES1233838388992228";
        Movimiento mov1 = new Movimiento();
        Movimiento mov2 = new Movimiento();

        Cuenta cuenta = new Cuenta();
        cuenta.setMovimientos(new ArrayList<>(Arrays.asList(mov1, mov2)));

        when(cuentaRepository.findById(numeroCuenta)).thenReturn(Optional.of(cuenta));

        // Act
        List<Movimiento> movimientos = cuentaServicio.listarMovimientos(numeroCuenta);

        // Assert
        assertNotNull(movimientos);
        assertEquals(2, movimientos.size());
    }

    @Test
    void listarMovimientos_deberiaLanzarExcepcion_siCuentaNoExiste() {
        // Arrange
        String numeroCuenta = "ES1233838388992228";

        when(cuentaRepository.findById(numeroCuenta)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> cuentaServicio.listarMovimientos(numeroCuenta)
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

    @Test
    void realizarTransferencia_deberiaTransferirDinero_siDestinoExiste() {
        String origen = "ES123";
        String destino = "ES999";
        float cantidad = 100f;

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setSaldo(200f);
        cuentaOrigen.setMovimientos(new ArrayList<>());

        Cuenta cuentaDestino = new Cuenta();

        when(cuentaRepository.findById(origen)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findById(destino)).thenReturn(Optional.of(cuentaDestino));

        cuentaServicio.realizarTransferencia(origen, destino, cantidad);

        assertEquals(100f, cuentaOrigen.getMovimientos().get(0).getImporte());
        assertEquals(TipoMovimiento.TRANSFERENCIA_SALIENTE, cuentaOrigen.getMovimientos().get(0).getTipoMovimiento());
        assertEquals(100f, cuentaOrigen.getSaldo());
        verify(cuentaRepository).save(cuentaOrigen);
    }

    @Test
    void realizarTransferencia_deberiaAplicarComision_siDestinoNoExistePeroIBANValido() {
        String origen = "ES123";
        String destino = "ES456"; // válido, pero no existe
        float cantidad = 100f;

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setSaldo(200f);
        cuentaOrigen.setMovimientos(new ArrayList<>());

        when(cuentaRepository.findById(origen)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findById(destino)).thenReturn(Optional.empty());

        cuentaServicio.realizarTransferencia(origen, destino, cantidad);

        float cantidadEsperada = cantidad * ConstantData.COMISION_TRANSFERENCIA;

        assertEquals(1, cuentaOrigen.getMovimientos().size());
        Movimiento mov = cuentaOrigen.getMovimientos().get(0);
        assertEquals(cantidadEsperada, mov.getImporte());
        assertEquals(TipoMovimiento.TRANSFERENCIA_SALIENTE, mov.getTipoMovimiento());
        assertEquals(200f - cantidadEsperada, cuentaOrigen.getSaldo(), 0.001);
        verify(cuentaRepository).save(cuentaOrigen);
    }

    @Test
    void realizarTransferencia_noDeberiaTransferir_siSaldoInsuficiente() {
        String origen = "ES123";
        String destino = "ES456";
        float cantidad = 100f;

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setSaldo(50f); // no suficiente
        cuentaOrigen.setMovimientos(new ArrayList<>());

        when(cuentaRepository.findById(origen)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findById(destino)).thenReturn(Optional.empty());

        cuentaServicio.realizarTransferencia(origen, destino, cantidad);

        assertTrue(cuentaOrigen.getMovimientos().isEmpty());
        assertEquals(50f, cuentaOrigen.getSaldo());
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void realizarTransferencia_deberiaLanzarExcepcion_siCuentaOrigenNoExiste() {
        when(cuentaRepository.findById("ORIGEN")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                cuentaServicio.realizarTransferencia("ORIGEN", "DESTINO", 100f)
        );
    }
}