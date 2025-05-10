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
import org.mockito.Spy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TarjetaServicioImplTest {

    private static final String NUMERO_TARJETA = "123456";
    private static final int PIN = 1234;
    private static final String PIN_ENCRIPTADO = TarjetaServicioImplTest.encriptarPin(PIN) ;
    private static final float CANTIDAD = 100f;

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private CuentaServicio cuentaServicio;

    @Mock
    private BancoServicio bancoServicio;

    @InjectMocks
    @Spy
    private TarjetaServicioImpl tarjetaServicio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sacarDinero_deberiaRetornar0_siBancoPropioYDebitoOK() {
        final Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(200f);

        final Tarjeta tarjeta = tarjeta(true, TipoTarjeta.DEBITO, 150f, 0f, cuenta);

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));
        when(cuentaServicio.sacarDinero(cuenta, CANTIDAD)).thenReturn(true);

        final float resultado = tarjetaServicio.sacarDinero(NUMERO_TARJETA, PIN, CANTIDAD, ConstantData.MI_BANCO);

        assertEquals(0f, resultado);
    }

    @Test
    void sacarDinero_deberiaAplicarComision_siBancoAjenoYCreditoOK() {
        final Cuenta cuenta = new Cuenta();
        final Tarjeta tarjeta = tarjeta(true, TipoTarjeta.CREDITO, 200f, 1000f, cuenta);

        Banco banco = new Banco();
        banco.setComisionRetirada(3.5f);

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));
        when(cuentaServicio.sacarDinero(cuenta, CANTIDAD)).thenReturn(true);
        when(bancoServicio.obtenerBanco("OtroBanco")).thenReturn(banco);

        float resultado = tarjetaServicio.sacarDinero(NUMERO_TARJETA, PIN, CANTIDAD, "OtroBanco");

        assertEquals(3.5f, resultado);
    }

    @Test
    void sacarDinero_deberiaLanzarExcepcion_siTarjetaNoActivada() {
        Tarjeta tarjeta = tarjeta(false, TipoTarjeta.DEBITO, 100f, 0f, new Cuenta());

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));

        assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.sacarDinero(NUMERO_TARJETA, PIN, CANTIDAD, ConstantData.MI_BANCO)
        );
    }

    @Test
    void sacarDinero_deberiaLanzarExcepcion_siTarjetaNoExiste() {
        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                tarjetaServicio.sacarDinero(NUMERO_TARJETA, PIN, CANTIDAD, ConstantData.MI_BANCO)
        );
    }

    @Test
    void sacarDinero_deberiaLanzarExcepcion_siCuentaServicioFalla() {
        final Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(200f);
        final Tarjeta tarjeta = tarjeta(true, TipoTarjeta.DEBITO, 200f, 0f, cuenta);

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));
        when(cuentaServicio.sacarDinero(cuenta, CANTIDAD)).thenReturn(false);

        assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.sacarDinero(NUMERO_TARJETA, PIN, CANTIDAD, ConstantData.MI_BANCO)
        );
    }

    @Test
    void sacarDinero_deberiaLanzarExcepcion_siCantidadSuperaLimites() {
        final Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(100f);
        final Tarjeta tarjeta = tarjeta(true, TipoTarjeta.DEBITO, 80f, 0f, cuenta);

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));

        assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.sacarDinero(NUMERO_TARJETA, PIN, CANTIDAD, ConstantData.MI_BANCO)
        );
    }

    @Test
    void ingresarDinero_deberiaIngresar_siBancoPropioYActivada() {
        final Cuenta cuenta = new Cuenta();
        final Tarjeta tarjeta = tarjeta(true, TipoTarjeta.DEBITO, 100f, 0f, cuenta);

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));

        tarjetaServicio.ingresarDinero(NUMERO_TARJETA, PIN, CANTIDAD, ConstantData.MI_BANCO);

        verify(cuentaServicio).ingresarDinero(cuenta, CANTIDAD);
    }

    @Test
    void ingresarDinero_deberiaLanzarExcepcion_siBancoAjeno() {
        final Tarjeta tarjeta = tarjeta(true, TipoTarjeta.DEBITO, 100f, 0f, new Cuenta());

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));

        assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.ingresarDinero(NUMERO_TARJETA, PIN, CANTIDAD, "OtroBanco")
        );
    }

    @Test
    void ingresarDinero_deberiaLanzarExcepcion_siTarjetaNoActivada() {
        final Tarjeta tarjeta = tarjeta(false, TipoTarjeta.DEBITO, 100f, 0f, new Cuenta());

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));

        assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.ingresarDinero(NUMERO_TARJETA, PIN, CANTIDAD, ConstantData.MI_BANCO)
        );
    }

    @Test
    void activarTarjeta_deberiaActivarla_siNoEstabaActivada() {
        final Tarjeta tarjeta = new Tarjeta();
        tarjeta.setActivada(false);

        when(tarjetaRepository.findById(NUMERO_TARJETA)).thenReturn(Optional.of(tarjeta));

        tarjetaServicio.activarTarjeta(NUMERO_TARJETA, PIN);

        assertTrue(tarjeta.isActivada());
        assertEquals(PIN_ENCRIPTADO, tarjeta.getPinEncriptado());
        verify(tarjetaRepository).save(tarjeta);
    }

    @Test
    void activarTarjeta_deberiaLanzarExcepcion_siYaEstabaActiva() {
        final Tarjeta tarjeta = new Tarjeta();
        tarjeta.setActivada(true);

        when(tarjetaRepository.findById(NUMERO_TARJETA)).thenReturn(Optional.of(tarjeta));

        assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.activarTarjeta(NUMERO_TARJETA, PIN)
        );
    }

    @Test
    void cambiarPin_deberiaActualizarPin_siTodoEsCorrecto() {
        String nuevoPinHash = encriptarPin(5678);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setActivada(true);
        tarjeta.setPinEncriptado(PIN_ENCRIPTADO);

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO))
                .thenReturn(Optional.of(tarjeta));

        tarjeta.setPinEncriptado(PIN_ENCRIPTADO);
        tarjetaServicio.cambiarPin(NUMERO_TARJETA, PIN, 5678);

        // Assert
        assertEquals(nuevoPinHash, tarjeta.getPinEncriptado());
        verify(tarjetaRepository).save(tarjeta);
    }

    @Test
    void cambiarPin_deberiaLanzarExcepcion_siNuevoPinEsIgual() {

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setActivada(true);
        tarjeta.setPinEncriptado(PIN_ENCRIPTADO);

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO))
                .thenReturn(Optional.of(tarjeta));

        ForbiddenOperationException ex = assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.cambiarPin(NUMERO_TARJETA, PIN, PIN)
        );
        assertEquals("No se puede cambiar al mismo pin", ex.getMessage());
    }

    @Test
    void cambiarPin_deberiaLanzarExcepcion_siTarjetaNoActivada() {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setActivada(false);
        tarjeta.setPinEncriptado(PIN_ENCRIPTADO);

        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO))
                .thenReturn(Optional.of(tarjeta));

        ForbiddenOperationException ex = assertThrows(ForbiddenOperationException.class, () ->
                tarjetaServicio.cambiarPin(NUMERO_TARJETA, PIN, 5678)
        );
        assertEquals("La tarjeta no está activada", ex.getMessage());
    }

    @Test
    void cambiarPin_deberiaLanzarExcepcion_siTarjetaNoExiste() {
        when(tarjetaRepository.findByNumeroAndPinEncriptado(NUMERO_TARJETA, PIN_ENCRIPTADO))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                tarjetaServicio.cambiarPin(NUMERO_TARJETA, PIN, 5678)
        );
        assertEquals("Tarjeta no encontrada", ex.getMessage());
    }



    private Tarjeta tarjeta(final boolean activada, final TipoTarjeta tipo, final float limiteRetirada, final float limiteCredito, final Cuenta cuenta) {
        final Tarjeta t = new Tarjeta();
        t.setActivada(activada);
        t.setTipoTarjeta(tipo);
        t.setLimiteRetirada(limiteRetirada);
        t.setLimiteCredito(limiteCredito);
        t.setCuenta(cuenta);
        return t;
    }

    private static String encriptarPin(final int pin){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Integer.toString(pin).getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash SHA-256", e);
        }
    }
}