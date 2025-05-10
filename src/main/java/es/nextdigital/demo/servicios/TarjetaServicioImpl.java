package es.nextdigital.demo.servicios;

import es.nextdigital.demo.common.ConstantData;
import es.nextdigital.demo.exceptions.ForbiddenOperationException;
import es.nextdigital.demo.exceptions.NotFoundException;
import es.nextdigital.demo.model.Banco;
import es.nextdigital.demo.model.Cuenta;
import es.nextdigital.demo.model.Tarjeta;
import es.nextdigital.demo.model.TipoTarjeta;
import es.nextdigital.demo.repositorio.TarjetaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TarjetaServicioImpl implements TarjetaServicio {

    private static final float MIN_LIMITE_RETIRADA = 500;
    private static final float MAX_LIMITE_RETIRADA = 6000;

    private final TarjetaRepository tarjetaRepository;
    private final CuentaServicio cuentaServicio;
    private final BancoServicio bancoServicio;

    @Override
    public float sacarDinero(final String numeroTarjeta, int pin, final float cantidad, final String bancoCajero) {
        final Optional<Tarjeta> tarjetaOptional = this.tarjetaRepository.findByNumeroAndPinEncriptado(numeroTarjeta, this.encriptarPin(pin));

        if (tarjetaOptional.isPresent()) {
            final Tarjeta tarjeta = tarjetaOptional.get();
            final Cuenta cuenta = tarjeta.getCuenta();
            if (!tarjeta.isActivada()) {
                throw new ForbiddenOperationException("La tarjeta no está activada");
            }

            boolean puedeSacar;
            if (TipoTarjeta.DEBITO == tarjeta.getTipoTarjeta()) {
                puedeSacar = cantidad <= cuenta.getSaldo() && cantidad <= tarjeta.getLimiteRetirada();
            } else {
                puedeSacar = cantidad <= tarjeta.getLimiteCredito() && cantidad <= tarjeta.getLimiteRetirada();
            }

            if (puedeSacar) {
                if (cuentaServicio.sacarDinero(cuenta, cantidad)) {
                    float comisiones = 0f;

                    if (!ConstantData.MI_BANCO.equals(bancoCajero)) {
                        final Banco banco = bancoServicio.obtenerBanco(bancoCajero);
                        comisiones = banco.getComisionRetirada();
                    }

                    return comisiones;
                } else {
                    throw new ForbiddenOperationException("Hubo algun error sacando dinero");
                }
            } else {
                throw new ForbiddenOperationException("No se puede sacar dinero");
            }
        } else {
            throw new NotFoundException("Tarjeta no encontrada");
        }
    }

    @Override
    public void ingresarDinero(final String numeroTarjeta, int pin, final float cantidad, final String bancoCajero) {
        final Optional<Tarjeta> tarjetaOptional = this.tarjetaRepository.findByNumeroAndPinEncriptado(numeroTarjeta, this.encriptarPin(pin));

        if (tarjetaOptional.isPresent()) {
            final Tarjeta tarjeta = tarjetaOptional.get();
            final Cuenta cuenta = tarjeta.getCuenta();
            if (!tarjeta.isActivada()) {
                throw new ForbiddenOperationException("La tarjeta no está activada");
            }

            boolean puedeIngresar = ConstantData.MI_BANCO.equals(bancoCajero);
            if (puedeIngresar) {
                cuentaServicio.ingresarDinero(cuenta, cantidad);
            } else {
                throw new ForbiddenOperationException("No se puede ingresar dinero desde un cajero de otro banco");
            }
        }
    }

    @Transactional
    @Override
    public void activarTarjeta(final String numeroTarjeta, final int pin) {
        final Optional<Tarjeta> tarjetaOptional = this.tarjetaRepository.findById(numeroTarjeta);
        if (tarjetaOptional.isPresent()) {
            final Tarjeta tarjeta = tarjetaOptional.get();
            if (tarjeta.isActivada()) {
                throw new ForbiddenOperationException("La tarjeta ya está activada");
            }

            tarjeta.setActivada(true);
            tarjeta.setPinEncriptado(this.encriptarPin(pin));

            tarjetaRepository.save(tarjeta);
        } else {
            throw new NotFoundException("Tarjeta no encontrada");
        }
    }

    @Transactional
    @Override
    public void cambiarPin(final String numeroTarjeta, final int pin, final int nuevoPin) {
        final Optional<Tarjeta> tarjetaOptional = this.tarjetaRepository.findByNumeroAndPinEncriptado(numeroTarjeta, this.encriptarPin(pin));
        if (tarjetaOptional.isPresent()) {
            final Tarjeta tarjeta = tarjetaOptional.get();
            if (!tarjeta.isActivada()) {
                throw new ForbiddenOperationException("La tarjeta no está activada");
            }

            final String nuevoPinEncriptado = this.encriptarPin(nuevoPin);
            if (tarjeta.getPinEncriptado().equals(nuevoPinEncriptado)) {
                throw new ForbiddenOperationException("No se puede cambiar al mismo pin");
            }

            tarjeta.setPinEncriptado(nuevoPinEncriptado);
            tarjetaRepository.save(tarjeta);
        } else {
            throw new NotFoundException("Tarjeta no encontrada");
        }
    }

    @Override
    public float consultarConfiguracion(String numeroTarjeta, int pin) {
        final Optional<Tarjeta> tarjetaOptional = this.tarjetaRepository.findByNumeroAndPinEncriptado(numeroTarjeta, this.encriptarPin(pin));
        if (tarjetaOptional.isPresent()) {
            final Tarjeta tarjeta = tarjetaOptional.get();
            if (!tarjeta.isActivada()) {
                throw new ForbiddenOperationException("La tarjeta no está activada");
            }

            return tarjeta.getLimiteRetirada();
        } else {
            throw new NotFoundException("Tarjeta no encontrada");
        }
    }

    @Override
    public void modificarConfiguracion(String numeroTarjeta, int pin, float limiteRetirada) {
        final Optional<Tarjeta> tarjetaOptional = this.tarjetaRepository.findByNumeroAndPinEncriptado(numeroTarjeta, this.encriptarPin(pin));
        if (tarjetaOptional.isPresent()) {
            final Tarjeta tarjeta = tarjetaOptional.get();
            if (!tarjeta.isActivada()) {
                throw new ForbiddenOperationException("La tarjeta no está activada");
            }

            if (limiteRetirada >= MIN_LIMITE_RETIRADA && limiteRetirada <= MAX_LIMITE_RETIRADA) {
                tarjeta.setLimiteRetirada(limiteRetirada);
                tarjetaRepository.save(tarjeta);
            } else {
                throw new ForbiddenOperationException("El nuevo limite de retirada esta fuera de los valores permitidos");
            }
        } else {
            throw new NotFoundException("Tarjeta no encontrada");
        }
    }

    private String encriptarPin(int pin){
       // Por ser un "examen" para encriptar simplemente hasheamos el pin. Obviamente no es la encriptacion ideal.
       // Habria que utilizar algun tipo de clave privada y algun algoritmo complejo de encriptacion.
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
