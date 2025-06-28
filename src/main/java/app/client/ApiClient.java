package app.client; 

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.IOException; 

public class ApiClient {

    private static final String BASE_URL = "http://localhost:7000"; 

    public static void main(String[] args) {
        System.out.println("--- Iniciando testes do Cliente Java (ApiClient) ---");

        try {
            // 1. Criar um cliente Java que envie uma requisição POST para o endpoint de criação
            System.out.println("\n--- Teste POST /usuarios ---");
            String userJson = "{\"nome\": \"Maria\", \"email\": \"maria@email.com\", \"idade\": 25}";
            sendPostRequest("/usuarios", userJson);

            System.out.println("\n--- Teste POST /tarefas ---");
            String taskJson = "{\"titulo\": \"Comprar alimentos\", \"descricao\": \"Leite, pão, ovos\", \"concluida\": false}";
            sendPostRequest("/tarefas", taskJson);

            // Adicionando um segundo usuário e tarefa para listar depois
            System.out.println("\n--- Teste POST /usuarios (segundo usuário) ---");
            String userJson2 = "{\"nome\": \"Grazielli\", \"email\": \"grazi@email.com\", \"idade\": 23}";
            sendPostRequest("/usuarios", userJson2);

            System.out.println("\n--- Teste POST /tarefas (segunda tarefa) ---");
            String taskJson2 = "{\"titulo\": \"Enviar email\", \"descricao\": \"Confirmar reunião\", \"concluida\": true}";
            sendPostRequest("/tarefas", taskJson2);


            // 2. Criar um cliente Java que realize uma requisição GET para o endpoint de listagem e imprima os dados
            System.out.println("\n--- Teste GET /usuarios (Listar Todos) ---");
            sendGetRequest("/usuarios");

            System.out.println("\n--- Teste GET /tarefas (Listar Todas) ---");
            sendGetRequest("/tarefas");

            // 3. Criar um cliente Java que envie uma requisição GET com path param, buscando um item pelo identificador
            System.out.println("\n--- Teste GET /usuarios/{email} (Buscar por Email) ---");
            sendGetRequest("/usuarios/maria.cliente@example.com"); // Email do usuário criado

            System.out.println("\n--- Teste GET /tarefas/{id} (Buscar por ID) ---");
            // Nota: O ID da tarefa será o próximo disponível, ou seja, 1 para a primeira tarefa criada
            // e 2 para a segunda. Em um sistema real, você pegaria o ID da resposta do POST.
            sendGetRequest("/tarefas/1"); // Supondo que a primeira tarefa criada tenha ID 1

            System.out.println("\n--- Teste GET /tarefas/{id} (Buscar por ID inexistente) ---");
            sendGetRequest("/tarefas/999"); // ID que provavelmente não existe

            // 4. Criar um cliente Java que envie uma requisição GET para o endpoint /status
            System.out.println("\n--- Teste GET /status ---");
            sendGetRequest("/status");

        } catch (Exception e) { // O 'Exception' genérico pega o IOException também
            System.err.println("Ocorreu um erro durante a execução do cliente: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n--- Testes do Cliente Java Concluídos ---");
    }

    /**
     * Envia uma requisição GET para a API e imprime a resposta.
     * @param endpoint O caminho do endpoint (ex: "/usuarios", "/tarefas/1")
     * @throws IOException Se ocorrer um erro de I/O
     */
    private static void sendGetRequest(String endpoint) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET"); // Define o método HTTP como GET
        connection.setRequestProperty("Accept", "application/json"); // Aceita JSON como resposta

        int responseCode = connection.getResponseCode(); // Obtém o código de resposta HTTP
        System.out.println("  Requisição GET: " + endpoint + " | Código de Resposta: " + responseCode);

        // Lê a resposta da API
        BufferedReader in;
        if (responseCode >= 200 && responseCode < 300) { // Se a resposta for de sucesso (2xx)
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        } else { // Se a resposta for de erro
            in = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
        }

        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();

        System.out.println("  Corpo da Resposta: " + content.toString());
    }

    /**
     * Envia uma requisição POST para a API com um corpo JSON.
     * @param endpoint O caminho do endpoint (ex: "/usuarios")
     * @param jsonInputString O corpo JSON a ser enviado
     * @throws IOException Se ocorrer um erro de I/O
     */
    private static void sendPostRequest(String endpoint, String jsonInputString) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST"); // Define o método HTTP como POST
        connection.setRequestProperty("Content-Type", "application/json; utf-8"); // Tipo de conteúdo que está sendo enviado
        connection.setRequestProperty("Accept", "application/json"); // Tipo de conteúdo que aceita como resposta
        connection.setDoOutput(true); // Habilita a escrita no corpo da requisição

        // Escreve o corpo JSON na requisição
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode(); // Obtém o código de resposta HTTP
        System.out.println("  Requisição POST: " + endpoint + " | Código de Resposta: " + responseCode);

        // Lê a resposta da API
        BufferedReader in;
        if (responseCode >= 200 && responseCode < 300) {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        } else {
            in = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
        }

        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();

        System.out.println("  Corpo da Resposta: " + content.toString());
    }
}