import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Lista as linhas FROM de um Dockerfile que não estão presas por digest.
 * Uso: java ChecaFrom.java Dockerfile
 */
public class ChecaFrom {

    // FROM [--platform=...] imagem [AS nome]
    private static final Pattern FROM = Pattern.compile(
            "^\\s*FROM\\s+(?:--platform=\\S+\\s+)?(\\S+)(?:\\s+AS\\s+(\\S+))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIGEST = Pattern.compile("@sha256:[a-f0-9]{64}$");

    public static void main(String[] args) throws Exception {
        var arquivo = Path.of(args.length > 0 ? args[0] : "Dockerfile");
        var linhas = Files.readAllLines(arquivo);
        var estagios = new java.util.HashSet<String>();
        int problemas = 0;

        for (int i = 0; i < linhas.size(); i++) {
            var m = FROM.matcher(linhas.get(i));
            if (!m.find()) continue;
            String imagem = m.group(1);
            boolean estagioAnterior = estagios.contains(imagem.toLowerCase());
            if (m.group(2) != null) estagios.add(m.group(2).toLowerCase());

            // FROM build (estágio anterior do multi-stage) e scratch não precisam de digest
            if (estagioAnterior || imagem.equalsIgnoreCase("scratch")) continue;
            if (!DIGEST.matcher(imagem).find()) {
                System.out.println("linha " + (i + 1) + ": " + imagem + " sem digest");
                problemas++;
            }
        }
        System.out.println(problemas == 0 ? "Ok: " + arquivo : "Reprovado: " + arquivo);
        System.exit(problemas == 0 ? 0 : 1);
    }
}
