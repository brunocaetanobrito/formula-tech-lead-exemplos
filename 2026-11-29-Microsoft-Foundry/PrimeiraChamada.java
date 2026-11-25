import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

/**
 * Primeira chamada a um modelo no Microsoft Foundry, sem SDK: só Java 21.
 * Antes de rodar:
 *   az login
 *   export AZURE_AI_AUTH_TOKEN=$(az account get-access-token --scope https://ai.azure.com/.default --query accessToken -o tsv)
 *   export FOUNDRY_PROJECT_ENDPOINT=https://<recurso>.services.ai.azure.com/api/projects/<projeto>
 * Uso: java PrimeiraChamada.java "sua pergunta"
 */
public class PrimeiraChamada {

    // Pega o primeiro "text" de um item output_text na resposta
    private static final Pattern TEXTO = Pattern.compile(
            "\"type\"\\s*:\\s*\"output_text\".*?\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"", Pattern.DOTALL);

    public static void main(String[] args) throws Exception {
        String endpoint = System.getenv("FOUNDRY_PROJECT_ENDPOINT");
        String token = System.getenv("AZURE_AI_AUTH_TOKEN");
        if (endpoint == null || token == null) {
            System.out.println("Defina FOUNDRY_PROJECT_ENDPOINT e AZURE_AI_AUTH_TOKEN (veja o comentário no topo).");
            System.exit(2);
        }
        String modelo = System.getenv().getOrDefault("FOUNDRY_MODEL", "gpt-5-mini");
        String pergunta = args.length > 0 ? args[0] : "Explique em uma frase o que é o Microsoft Foundry.";

        String corpo = """
                {"model": "%s", "input": "%s"}
                """.formatted(modelo, pergunta.replace("\\", "\\\\").replace("\"", "\\\""));

        HttpRequest pedido = HttpRequest.newBuilder(URI.create(endpoint + "/openai/v1/responses"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();

        HttpResponse<String> resposta = HttpClient.newHttpClient()
                .send(pedido, HttpResponse.BodyHandlers.ofString());

        if (resposta.statusCode() != 200) {
            System.out.println("Erro " + resposta.statusCode() + ": " + resposta.body());
            System.exit(1);
        }
        var m = TEXTO.matcher(resposta.body());
        System.out.println(m.find() ? desescapar(m.group(1)) : resposta.body());
    }

    // Desfaz os escapes mais comuns de string JSON (quebra de linha, aspas, barra e código unicode)
    static String desescapar(String s) {
        var out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 == s.length()) { out.append(c); continue; }
            char n = s.charAt(++i);
            switch (n) {
                case 'n' -> out.append('\n');
                case 't' -> out.append('\t');
                case 'u' -> { out.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16)); i += 4; }
                default -> out.append(n);
            }
        }
        return out.toString();
    }
}
