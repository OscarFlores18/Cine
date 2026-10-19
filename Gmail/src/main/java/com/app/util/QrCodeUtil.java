package com.app.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Genera el QR del ticket de forma 100% local (sin llamar a ningun servicio
 * externo de generacion de QR) y lo devuelve como PNG en base64, listo para
 * incrustar directo en el HTML del mail con un data URI
 * ("data:image/png;base64,...") — asi el QR se ve siempre, incluso si el
 * cliente de correo bloquea imagenes remotas.
 */
@Slf4j
public final class QrCodeUtil {

    private QrCodeUtil() {}

    public static String generarBase64(String contenido, int tamanioPx) {
        try {
            BitMatrix matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, tamanioPx, tamanioPx);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            ImageIO.write(MatrixToImageWriter.toBufferedImage(matriz), "PNG", salida);
            return Base64.getEncoder().encodeToString(salida.toByteArray());
        } catch (WriterException | java.io.IOException e) {
            // Si por algun motivo el QR no se puede generar, el mail se manda
            // igual sin el (no tiene sentido tirar abajo todo el envio por esto).
            log.warn("No se pudo generar el QR del ticket: {}", e.getMessage());
            return null;
        }
    }
}
