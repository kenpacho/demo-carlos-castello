package es.nextdigital.demo.controlador;

import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Movimiento;
import es.nextdigital.demo.model.TipoMovimiento;
import es.nextdigital.demo.repositorio.CuentaRepository;
import es.nextdigital.demo.repositorio.TarjetaRepository;
import es.nextdigital.demo.servicios.BancoServicio;
import es.nextdigital.demo.servicios.CuentaServicioImpl;
import es.nextdigital.demo.servicios.TarjetaServicioImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CuentaController.class})
@Import({TarjetaServicioImpl.class, CuentaServicioImpl.class, GlobalExceptionHandler.class})
class CuentaControllerTestIT {

    private static final String BASE_ENDPOINT = "/carlosbank/api/cuentas";
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TarjetaRepository tarjetaRepository;

    @MockBean
    private CuentaRepository cuentaRepository;

    @MockBean
    private BancoServicio bancoServicio;
    
    @Test
    void listarMovimientos_OK() throws Exception {
        Movimiento movimiento = new Movimiento();
        movimiento.setImporte(100f);
        movimiento.setTipoMovimiento(TipoMovimiento.INGRESO);
        movimiento.setFechaMovimiento(new Date());

        Cuenta cuenta = new Cuenta();
        cuenta.setMovimientos(List.of(movimiento));

        when(cuentaRepository.findById("ES01")).thenReturn(Optional.of(cuenta));

        mockMvc.perform(get(BASE_ENDPOINT + "/ES01/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].importe").value(100f));
    }

    @Test
    void realizarTransferencia_OK() throws Exception {
        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(1000f);
        cuenta.setMovimientos(new ArrayList<>());

        when(cuentaRepository.findById("ES01")).thenReturn(Optional.of(cuenta));

        mockMvc.perform(post(BASE_ENDPOINT + "/ES01/transferencias")
                        .param("ibanDestino", "ES02")
                        .param("cantidad", "100"))
                .andExpect(status().isOk());

        verify(cuentaRepository).save(any());
    }
}