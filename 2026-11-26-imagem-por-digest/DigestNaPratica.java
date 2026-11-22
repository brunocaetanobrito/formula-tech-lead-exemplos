import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tag x digest em miniatura: um "registro" que guarda conteúdo pelo hash
 * e etiquetas (tags) que apontam para um hash.
 * Uso: java DigestNaPratica.java
 */
public class DigestNaPratica {

    /** digest = "sha256:" + SHA-256 dos bytes do conteúdo, em hexadecimal. */
    static String digest(String conteudo) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(conteudo.getBytes(StandardCharsets.UTF_8));
        return "sha256:" + HexFormat.of().formatHex(hash);
    }

    static final Map<String, String> blobs = new LinkedHashMap<>(); // digest -> conteúdo
    static final Map<String, String> tags = new LinkedHashMap<>();  // tag -> digest

    static String publicar(String tag, String conteudo) throws Exception {
        String d = digest(conteudo);
        blobs.put(d, conteudo);
        tags.put(tag, d); // a tag passa a apontar para o conteúdo novo
        return d;
    }

    static String puxar(String referencia) {
        String d = referencia.contains("@") ? referencia.substring(referencia.indexOf('@') + 1)
                                            : tags.get(referencia);
        return blobs.get(d);
    }

    static String curto(String d) { return d.substring(0, 19) + "..."; }

    public static void main(String[] args) throws Exception {
        // Terça: o publicador sobe a imagem e aponta a tag para ela
        String terca = publicar("tijolo/base-java:21", "{\"jre\":\"versão A\"}");
        System.out.println("terça   tijolo/base-java:21 -> " + curto(terca));

        // Quinta: sai uma correção e a MESMA tag passa a apontar para outro conteúdo
        String quinta = publicar("tijolo/base-java:21", "{\"jre\":\"versão B, com correção\"}");
        System.out.println("quinta  tijolo/base-java:21 -> " + curto(quinta));

        System.out.println();
        System.out.println("Por tag:    " + puxar("tijolo/base-java:21"));
        System.out.println("Por digest: " + puxar("tijolo/base-java:21@" + terca));

        // Mudou um caractere, o digest inteiro muda
        System.out.println();
        System.out.println("Mesmo conteúdo, mesmo digest? " + digest("abc").equals(digest("abc")));
        System.out.println("abc -> " + curto(digest("abc")));
        System.out.println("abd -> " + curto(digest("abd")));
    }
}
