package com.egg.autenticidad.servicios;

import com.egg.autenticidad.entidades.Producto;
import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.repositorios.ProductoRepositorio;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Genera hojas A4 de etiquetas listas para imprimir (3 columnas x 9 filas = 27 por hoja).
 *
 * Hay DOS tipos, porque cumplen funciones distintas:
 *  - QR:     lleva el QR de verificacion. Va OCULTO dentro del producto (bajo una etiqueta sellada),
 *            para que nadie pueda fotografiarlo antes de la compra.
 *  - CODIGO: lleva un codigo de barras con el codigo de fabrica. Va VISIBLE, para que la caja lo lea
 *            con un lector de codigo de barras (o lo escriba a mano).
 */
@Service
public class EtiquetaServicio {

    public enum Tipo { QR, CODIGO }

    private static final int MAX_ETIQUETAS = 500;

    // Las medidas de PDF se dan en "puntos" (1 punto = 1/72 de pulgada). Convertimos desde milimetros.
    private static final float MM = 72f / 25.4f;
    private static final float MARGEN = 10 * MM;
    private static final int COLUMNAS = 3;
    private static final int FILAS = 9;
    private static final float PAGINA_ANCHO = PDRectangle.A4.getWidth();
    private static final float PAGINA_ALTO = PDRectangle.A4.getHeight();
    private static final float ANCHO = (PAGINA_ANCHO - 2 * MARGEN) / COLUMNAS;
    private static final float ALTO = 30 * MM;

    private final ProductoRepositorio productoRepositorio;
    private final QrServicio qrServicio;

    public EtiquetaServicio(ProductoRepositorio productoRepositorio, QrServicio qrServicio) {
        this.productoRepositorio = productoRepositorio;
        this.qrServicio = qrServicio;
    }

    /** Fuentes estandar del PDF: no hace falta incluirlas dentro del archivo. */
    private record Fuentes(PDFont normal, PDFont negrita) { }

    @Transactional(readOnly = true)
    public byte[] generarPdf(Collection<Long> ids, Tipo tipo) throws MiExcepcion {
        if (ids == null || ids.isEmpty()) {
            throw new MiExcepcion("Selecciona al menos un producto para imprimir sus etiquetas");
        }
        // TreeSet: quita repetidos y deja las etiquetas ordenadas por id
        List<Long> unicos = new ArrayList<>(new TreeSet<>(ids));
        if (unicos.size() > MAX_ETIQUETAS) {
            throw new MiExcepcion("Puedes imprimir como máximo " + MAX_ETIQUETAS + " etiquetas a la vez");
        }
        List<Producto> productos = productoRepositorio.findAllById(unicos).stream()
                .filter(p -> p.getCodigoFabrica() != null)
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
        if (productos.isEmpty()) {
            throw new MiExcepcion("No se encontraron productos para imprimir");
        }

        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Fuentes fuentes = new Fuentes(
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD));

            int porPagina = COLUMNAS * FILAS;
            PDPageContentStream cs = null;
            try {
                for (int i = 0; i < productos.size(); i++) {
                    int pos = i % porPagina;
                    if (pos == 0) {
                        if (cs != null) {
                            cs.close();
                        }
                        PDPage pagina = new PDPage(PDRectangle.A4);
                        doc.addPage(pagina);
                        cs = new PDPageContentStream(doc, pagina);
                    }
                    float x = MARGEN + (pos % COLUMNAS) * ANCHO;
                    float yAbajo = PAGINA_ALTO - MARGEN - ((pos / COLUMNAS) + 1) * ALTO;

                    // Marco tenue: guia para recortar
                    cs.setStrokingColor(0.75f);
                    cs.setLineWidth(0.4f);
                    cs.addRect(x, yAbajo, ANCHO, ALTO);
                    cs.stroke();
                    cs.setNonStrokingColor(0f);

                    if (tipo == Tipo.QR) {
                        dibujarEtiquetaQr(doc, cs, fuentes, productos.get(i), x, yAbajo);
                    } else {
                        dibujarEtiquetaCodigo(doc, cs, fuentes, productos.get(i), x, yAbajo);
                    }
                }
            } finally {
                if (cs != null) {
                    cs.close(); // hay que cerrar el flujo ANTES de guardar el documento
                }
            }
            doc.save(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new MiExcepcion("No se pudo generar el PDF de etiquetas");
        }
    }

    // ---------- Etiqueta con QR (va oculta dentro del producto) ----------
    private void dibujarEtiquetaQr(PDDocument doc, PDPageContentStream cs, Fuentes f, Producto p,
                                   float x, float y) throws IOException, MiExcepcion {
        PDImageXObject qr = PDImageXObject.createFromByteArray(
                doc, qrServicio.generarPng(p.getToken()), "qr-" + p.getCodigoFabrica());
        float tam = 24 * MM;
        float qx = x + 3 * MM;
        float qy = y + (ALTO - tam) / 2;
        cs.drawImage(qr, qx, qy, tam, tam);

        float tx = qx + tam + 4 * MM;
        float maxAncho = x + ANCHO - tx - 2 * MM;
        escribir(cs, f.negrita(), 7f, "OK FABRICADO", tx, y + ALTO - 24, maxAncho);
        escribir(cs, f.negrita(), 9f, p.getCodigoFabrica(), tx, y + ALTO - 40, maxAncho);
        escribir(cs, f.normal(), 7f, nombreModelo(p), tx, y + ALTO - 52, maxAncho);
        escribir(cs, f.normal(), 6f, "Escanea para verificar", tx, y + 14, maxAncho);
    }

    // ---------- Etiqueta con codigo de barras (visible, para la caja) ----------
    private void dibujarEtiquetaCodigo(PDDocument doc, PDPageContentStream cs, Fuentes f, Producto p,
                                       float x, float y) throws IOException, MiExcepcion {
        float interior = ANCHO - 8 * MM;
        escribirCentrado(cs, f.normal(), 7f, nombreModelo(p), x, y + ALTO - 14, ANCHO, interior);

        PDImageXObject barras = PDImageXObject.createFromByteArray(
                doc, codigoDeBarras(p.getCodigoFabrica()), "barras-" + p.getCodigoFabrica());
        float bAncho = 52 * MM;
        float bAlto = 11 * MM;
        cs.drawImage(barras, x + (ANCHO - bAncho) / 2, y + 34, bAncho, bAlto);

        escribirCentrado(cs, f.negrita(), 9f, p.getCodigoFabrica(), x, y + 22, ANCHO, interior);
        escribirCentrado(cs, f.normal(), 6f, "OK FABRICADO", x, y + 9, ANCHO, interior);
    }

    private byte[] codigoDeBarras(String codigo) throws MiExcepcion {
        try {
            BitMatrix matriz = new Code128Writer().encode(codigo, BarcodeFormat.CODE_128, 600, 120);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new MiExcepcion("No se pudo generar el código de barras");
        }
    }

    // ---------- Utilidades de texto ----------
    private String nombreModelo(Producto p) {
        return p.getModelo() != null ? p.getModelo().getNombre() : "";
    }

    private void escribir(PDPageContentStream cs, PDFont fuente, float tamano, String texto,
                          float x, float y, float maxAncho) throws IOException {
        String ajustado = ajustar(fuente, tamano, texto, maxAncho);
        cs.beginText();
        cs.setFont(fuente, tamano);
        cs.newLineAtOffset(x, y);
        cs.showText(ajustado);
        cs.endText();
    }

    private void escribirCentrado(PDPageContentStream cs, PDFont fuente, float tamano, String texto,
                                  float xCelda, float y, float anchoCelda, float maxAncho) throws IOException {
        String ajustado = ajustar(fuente, tamano, texto, maxAncho);
        float ancho = fuente.getStringWidth(ajustado) / 1000f * tamano;
        cs.beginText();
        cs.setFont(fuente, tamano);
        cs.newLineAtOffset(xCelda + (anchoCelda - ancho) / 2, y);
        cs.showText(ajustado);
        cs.endText();
    }

    /**
     * Deja solo caracteres que la fuente del PDF sabe dibujar (un emoji haria fallar showText)
     * y recorta con ".." si el texto no cabe en el ancho disponible.
     */
    private String ajustar(PDFont fuente, float tamano, String texto, float maxAncho) throws IOException {
        StringBuilder limpio = new StringBuilder();
        for (char c : texto.toCharArray()) {
            try {
                fuente.encode(String.valueOf(c));
                limpio.append(c);
            } catch (IllegalArgumentException e) {
                limpio.append('?');
            }
        }
        String resultado = limpio.toString();
        if (anchoTexto(fuente, tamano, resultado) <= maxAncho) {
            return resultado;
        }
        // No cabe: quitamos letras del final hasta que quepa junto con ".."
        while (resultado.length() > 1 && anchoTexto(fuente, tamano, resultado + "..") > maxAncho) {
            resultado = resultado.substring(0, resultado.length() - 1);
        }
        return resultado + "..";
    }

    private float anchoTexto(PDFont fuente, float tamano, String texto) throws IOException {
        return fuente.getStringWidth(texto) / 1000f * tamano;
    }
}
