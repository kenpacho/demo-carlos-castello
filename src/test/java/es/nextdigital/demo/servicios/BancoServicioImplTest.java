package es.nextdigital.demo.servicios;

import es.nextdigital.demo.exceptions.NotFoundException;
import es.nextdigital.demo.model.Banco;
import es.nextdigital.demo.repositorio.BancoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class BancoServicioImplTest {

    @Mock
    private BancoRepository bancoRepository;

    @InjectMocks
    private BancoServicioImpl bancoServicio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void obtenerBanco_deberiaDevolverBanco_siExiste() {
        // Arrange
        String nombreBanco = "Banco Madrid";
        Banco banco = new Banco(); // asegúrate de tener constructor o setters
        banco.setNombre(nombreBanco);

        when(bancoRepository.findBancosByNombre(nombreBanco))
                .thenReturn(Optional.of(banco));

        // Act
        Banco resultado = bancoServicio.obtenerBanco(nombreBanco);

        // Assert
        assertNotNull(resultado);
        assertEquals(nombreBanco, resultado.getNombre());
    }

    @Test
    void obtenerBanco_deberiaLanzarExcepcion_siNoExiste() {
        // Arrange
        String nombreBanco = "Banco Fantasma";

        when(bancoRepository.findBancosByNombre(nombreBanco))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException excepcion = assertThrows(
                NotFoundException.class,
                () -> bancoServicio.obtenerBanco(nombreBanco)
        );

        assertEquals("El banco no existe", excepcion.getMessage());
    }
}