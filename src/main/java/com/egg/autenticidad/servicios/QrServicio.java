package com.egg.autenticidad.servicios;

import com.egg.autenticidad.excepciones.MiExcepcion;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Dibuja el QR. El QR contiene una URL: https://tu-dominio/v/TOKEN */
@Service
public class QrServicio {

    private final String baseUrl;

    // @Value lee una propiedad de application.properties (app.base-url)
    public QrServicio(@Value("${app.base-url}") String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    public String urlDe(String token) {
        return baseUrl + "/v/" + token;
    }

    public byte[] generarPng(String token) throws MiExcepcion {
        try {
            BitMatrix matriz = new QRCodeWriter().encode(
                    urlDe(token), BarcodeFormat.QR_CODE, 300, 300,
                    Map.of(EncodeHintType.MARGIN, 1));
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida);
            return salida.toByteArray();
        } catch (WriterException | IOException e) {
            throw new MiExcepcion("No se pudo generar el QR");
        }
    }
}
