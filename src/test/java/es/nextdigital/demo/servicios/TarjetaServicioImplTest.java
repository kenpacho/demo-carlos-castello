package es.nextdigital.demo.servicios;

import es.nextdigital.demo.common.ConstantData;
import es.nextdigital.demo.exceptions.ForbiddenOperationException;
import es.nextdigital.demo.exceptions.NotFoundException;
import es.nextdigital.demo.model.Banco;
import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Tarjeta;
import es.nextdigital.demo.model.TipoTarjeta;
import es.nextdigital.demo.repositorio.TarjetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TarjetaServicioImplTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private CuentaServicio cuentaServicio;

    @Mock
    private BancoServicio bancoServicio;

    @InjectMocks
    private TarjetaServicioImpl tarjetaServicio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sacarDinero_deberiaRetornar0SiEsMismoBancoYRetiroExitoso() {
        // Arrange
        String numeroTarjeta = "123456";
        float cantidad = 100f;
        String bancoCajero = ConstantData.MI_BANCO;

        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(200f);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta(numeroTarjeta);
        tarjeta.setCuenta(cuenta);
        tarjeta.setTipoTarjeta(TipoTarjeta.DEBITO);
        tarjeta.setLimiteRetirada(150f);

        when(tarjetaRepository.findById(numeroTarjeta)).thenReturn(Optional.of(tarjeta));
        when(cuentaServicio.sacarDinero(cuenta, cantidad)).thenReturn(true);

        // Act
        float comision = tarjetaServicio.sacarDinero(numeroTarjeta, cantidad, bancoCajero);

        // Assert
        assertEquals(0f, comision);
        verify(cuentaServicio).sacarDinero(cuenta, cantidad);
        verify(bancoServicio, never()).obtenerBanco(any());
    }

    @Test
    void sacarDinero_deberiaAplicarComisionSiEsOtroBanco() {
        // Arrange
        String numeroTarjeta = "123456";
        float cantidad = 100f;
        String bancoCajero = "Banco Externo";

        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(300f);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta(numeroTarjeta);
        tarjeta.setCuenta(cuenta);
        tarjeta.setTipoTarjeta(TipoTarjeta.DEBITO);
        tarjeta.setLimiteRetirada(200f);

        Banco banco = new Banco();
        banco.setComisionRetirada(2.5f);

        when(tarjetaRepository.findById(numeroTarjeta)).thenReturn(Optional.of(tarjeta));
        when(cuentaServicio.sacarDinero(cuenta, cantidad)).thenReturn(true);
        when(bancoServicio.obtenerBanco(bancoCajero)).thenReturn(banco);

        // Act
        float comision = tarjetaServicio.sacarDinero(numeroTarjeta, cantidad, bancoCajero);

        // Assert
        assertEquals(2.5f, comision);
    }

    @Test
    void sacarDinero_deberiaLanzarExcepcion_siNoPuedeSacarPorSaldo() {
        // Arrange
        String numeroTarjeta = "123456";
        float cantidad = 500f;

        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(100f);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta(numeroTarjeta);
        tarjeta.setCuenta(cuenta);
        tarjeta.setTipoTarjeta(TipoTarjeta.DEBITO);
        tarjeta.setLimiteRetirada(600f);

        when(tarjetaRepository.findById(numeroTarjeta)).thenReturn(Optional.of(tarjeta));

        // Act & Assert
        assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.sacarDinero(numeroTarjeta, cantidad, "Banco X")
        );
    }

    @Test
    void sacarDinero_deberiaLanzarExcepcion_siNoPuedeSacarPorLimiteRetirada() {
        // Arrange
        String numeroTarjeta = "123456";
        float cantidad = 300f;

        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(1000f);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta(numeroTarjeta);
        tarjeta.setCuenta(cuenta);
        tarjeta.setTipoTarjeta(TipoTarjeta.CREDITO);
        tarjeta.setLimiteRetirada(200f); // menor que cantidad
        tarjeta.setLimiteCredito(1000f);

        when(tarjetaRepository.findById(numeroTarjeta)).thenReturn(Optional.of(tarjeta));

        // Act & Assert
        assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.sacarDinero(numeroTarjeta, cantidad, "Banco Y")
        );
    }

    @Test
    void sacarDinero_deberiaLanzarExcepcion_siTarjetaNoExiste() {
        // Arrange
        when(tarjetaRepository.findById("000000")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                tarjetaServicio.sacarDinero("000000", 100f, "Banco Z")
        );
    }

    @Test
    void sacarDinero_deberiaLanzarExcepcion_siSacarDineroFalla() {
        // Arrange
        String numeroTarjeta = "123456";
        float cantidad = 50f;

        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(100f);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setNumeroTarjeta(numeroTarjeta);
        tarjeta.setCuenta(cuenta);
        tarjeta.setTipoTarjeta(TipoTarjeta.DEBITO);
        tarjeta.setLimiteRetirada(100f);

        when(tarjetaRepository.findById(numeroTarjeta)).thenReturn(Optional.of(tarjeta));
        when(cuentaServicio.sacarDinero(cuenta, cantidad)).thenReturn(false);

        // Act & Assert
        assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.sacarDinero(numeroTarjeta, cantidad, ConstantData.MI_BANCO)
        );
    }
}