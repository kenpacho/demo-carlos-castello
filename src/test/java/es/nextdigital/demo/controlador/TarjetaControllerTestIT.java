package es.nextdigital.demo.controlador;

import es.nextdigital.demo.common.ConstantData;
import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Tarjeta;
import es.nextdigital.demo.model.TipoTarjeta;
import es.nextdigital.demo.repositorio.CuentaRepository;
import es.nextdigital.demo.repositorio.TarjetaRepository;
import es.nextdigital.demo.servicios.BancoServicio;
import es.nextdigital.demo.servicios.CuentaServicio;
import es.nextdigital.demo.servicios.CuentaServicioImpl;
import es.nextdigital.demo.servicios.TarjetaServicioImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TarjetaController.class})
@Import({TarjetaServicioImpl.class, CuentaServicioImpl.class, GlobalExceptionHandler.class})
class TarjetaControllerTestIT {

    private static final String BASE_ENDPOINT = "/carlosbank/api/tarjetas";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TarjetaRepository tarjetaRepository;

    @MockBean
    private CuentaRepository cuentaRepository;

    @MockBean
    private CuentaServicio cuentaServicio;

    @MockBean
    private BancoServicio bancoServicio;

    private final String PIN_ENCRIPTADO = encriptarPin(1234);

    @Test
    void sacarDinero_OK() throws Exception {
        Tarjeta tarjeta = buildTarjeta(true, TipoTarjeta.DEBITO, 1000, 0, 500);
        when(tarjetaRepository.findByNumeroAndPinEncriptado("1111", PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));
        when(cuentaServicio.sacarDinero(any(), eq(500f))).thenReturn(true);

        mockMvc.perform(post(BASE_ENDPOINT + "/1111/sacar")
                        .param("pin", "1234")
                        .param("cantidad", "500")
                        .param("banco", ConstantData.MI_BANCO))
                .andExpect(status().isOk())
                .andExpect(content().string("0.0"));
    }

    @Test
    void ingresarDinero_OK() throws Exception {
        Tarjeta tarjeta = buildTarjeta(true, TipoTarjeta.DEBITO, 1000, 0, 500);
        when(tarjetaRepository.findByNumeroAndPinEncriptado("1111", PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));

        mockMvc.perform(post(BASE_ENDPOINT + "/1111/ingresar")
                        .param("pin", "1234")
                        .param("cantidad", "500")
                        .param("banco", ConstantData.MI_BANCO))
                .andExpect(status().isOk());

        verify(cuentaServicio).ingresarDinero(any(), eq(500f));
    }

    @Test
    void activarTarjeta_OK() throws Exception {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setActivada(false);
        when(tarjetaRepository.findById("1111")).thenReturn(Optional.of(tarjeta));

        mockMvc.perform(post(BASE_ENDPOINT + "/1111/activar")
                        .param("pin", "1234"))
                .andExpect(status().isOk());

        verify(tarjetaRepository).save(any());
    }

    @Test
    void cambiarPin_OK() throws Exception {
        Tarjeta tarjeta = buildTarjeta(true, TipoTarjeta.DEBITO, 1000, 0, 500);
        when(tarjetaRepository.findByNumeroAndPinEncriptado("1111", PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));

        mockMvc.perform(post(BASE_ENDPOINT + "/1111/cambiar-pin")
                        .param("pin", "1234")
                        .param("nuevoPin", "5678"))
                .andExpect(status().isOk());
    }

    @Test
    void consultarConfiguracion_OK() throws Exception {
        Tarjeta tarjeta = buildTarjeta(true, TipoTarjeta.DEBITO, 1000, 0, 500);
        when(tarjetaRepository.findByNumeroAndPinEncriptado("1111", PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));

        mockMvc.perform(get(BASE_ENDPOINT + "/1111/configuracion")
                        .param("pin", "1234"))
                .andExpect(status().isOk())
                .andExpect(content().string("500.0"));
    }

    @Test
    void modificarConfiguracion_OK() throws Exception {
        Tarjeta tarjeta = buildTarjeta(true, TipoTarjeta.DEBITO, 1000, 0, 500);
        when(tarjetaRepository.findByNumeroAndPinEncriptado("1111", PIN_ENCRIPTADO)).thenReturn(Optional.of(tarjeta));

        mockMvc.perform(post(BASE_ENDPOINT + "/1111/configuracion")
                        .param("pin", "1234")
                        .param("limiteRetirada", "600"))
                .andExpect(status().isOk());

        verify(tarjetaRepository).save(any());
    }

    private Tarjeta buildTarjeta(boolean activada, TipoTarjeta tipo, float saldo, float credito, float limite) {
        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(saldo);
        cuenta.setMovimientos(new ArrayList<>());

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setActivada(activada);
        tarjeta.setTipoTarjeta(tipo);
        tarjeta.setCuenta(cuenta);
        tarjeta.setLimiteCredito(credito);
        tarjeta.setLimiteRetirada(limite);
        tarjeta.setPinEncriptado(PIN_ENCRIPTADO);
        return tarjeta;
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