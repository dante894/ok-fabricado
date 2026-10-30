package com.egg.autenticidad.entidades.enumeraciones;

/**
 * Los cuatro posibles resultados al escanear un QR.
 * Cada uno trae su emoji, titulo, clase de color y mensaje: asi las plantillas
 * HTML no necesitan ningun if.
 */
public enum Resultado {

    AUTENTICO("👌", "Auténtico", "ok",
            "Producto original, vendido en tienda oficial. Esta verificación queda registrada y no se repetirá."),
    YA_VERIFICADO("⚠️", "Ya fue verificado", "warn",
            "Este código ya se verificó antes. Si no fuiste tú, el QR pudo haber sido copiado: avisa a la marca."),
    NO_VENDIDO("❌", "No figura como vendido", "bad",
            "Este producto no está registrado como vendido en una tienda oficial. Si lo compraste, contacta a la marca."),
    INEXISTENTE("❌", "No auténtico", "bad",
            "Este código no existe en nuestros registros. El producto podría ser falsificado.");

    private final String emoji;
    private final String titulo;
    private final String claseCss;
    private final String mensaje;

    Resultado(String emoji, String titulo, String claseCss, String mensaje) {
        this.emoji = emoji;
        this.titulo = titulo;
        this.claseCss = claseCss;
        this.mensaje = mensaje;
    }

    public String getEmoji() { return emoji; }
    public String getTitulo() { return titulo; }
    public String getClaseCss() { return claseCss; }
    public String getMensaje() { return mensaje; }
}
