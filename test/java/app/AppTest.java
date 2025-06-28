package app;

import io.javalin.Javalin;
import io.javalin.testtools.HttpClient; 
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach; 
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.RequestBody;
import okhttp3.MediaType;

import java.io.IOException;

public class AppTest {

    private Javalin app; 
    private HttpClient client; 
    private final ObjectMapper objectMapper = new ObjectMapper(); 

    @BeforeEach
    void setup() {
        App.resetState(); 
        app = App.createApp(); 
        app.start(0); 
        client = new HttpClient(app); 
    }

    @AfterEach
    void teardown() {
        app.stop(); 
        try {
            Thread.sleep(50); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private RequestBody createJsonRequestBody(String json) {
        return RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
    }

    // --- Testes de Endpoints Básicos ---

    @Test
    public void testHelloWorld() throws IOException {
        Response response = client.get("/hello"); 
        assertEquals(200, response.code());
        assertEquals("Olá, Javalin!", response.body().string());
    }

    @Test
    public void testStatusEndpoint() throws IOException {
        Response response = client.get("/status");
        assertEquals(200, response.code());
        String responseBody = response.body().string();
        
        JsonNode actualJson = objectMapper.readTree(responseBody);
        assertTrue(actualJson.has("status"));
        assertTrue(actualJson.has("timestamp"));
        assertEquals("ok", actualJson.get("status").asText());
        assertNotNull(actualJson.get("timestamp").asText());
    }

    @Test
    public void testEchoEndpoint() throws IOException {
        String jsonInput = "{\"message\": \"olá\", \"value\": 123}";
        Response postResponse = client.post("/echo", createJsonRequestBody(jsonInput));

        assertEquals(200, postResponse.code());
        JsonNode actualJson = objectMapper.readTree(postResponse.body().string());
        assertEquals("olá", actualJson.get("message").asText());
        assertEquals(123, actualJson.get("value").asInt());
    }

    @Test
    public void testSaudacaoEndpoint() throws IOException {
        Response response = client.get("/saudacao/Mundo");
        assertEquals(200, response.code());
        assertEquals("{\"mensagem\":\"Olá, Mundo!\"}", response.body().string());
    }

    // --- Testes de Usuários (CRUD) ---

    @Test
    public void testCriarListarUsuario() throws IOException {
        String userJson = "{\"nome\": \"Teste\", \"email\": \"teste@example.com\", \"idade\": 30}";
        Response postResponse = client.post("/usuarios", createJsonRequestBody(userJson));
        assertEquals(201, postResponse.code()); 

        JsonNode actualJson = objectMapper.readTree(postResponse.body().string());
        assertEquals("Teste", actualJson.get("nome").asText());
        assertEquals("teste@example.com", actualJson.get("email").asText());
        assertEquals(30, actualJson.get("idade").asInt());

        Response getResponse = client.get("/usuarios");
        assertEquals(200, getResponse.code()); 
        JsonNode usersList = objectMapper.readTree(getResponse.body().string());
        assertTrue(usersList.isArray(), "A resposta de /usuarios deve ser um array JSON.");
        assertTrue(usersList.size() > 0, "A lista de usuários não deve estar vazia após a criação.");
        
        boolean found = false;
        for (JsonNode userNode : usersList) {
            if (userNode.has("email") && userNode.get("email").asText().equals("teste@example.com")) {
                found = true;
                assertEquals("Teste", userNode.get("nome").asText());
                assertEquals(30, userNode.get("idade").asInt());
                break;
            }
        }
        assertTrue(found, "Usuário criado não encontrado na lista de usuários.");
    }

    @Test
    public void testBuscarUsuarioPorEmail() throws IOException {
        String userJson = "{\"nome\": \"Busca\", \"email\": \"busca@example.com\", \"idade\": 25}";
        client.post("/usuarios", createJsonRequestBody(userJson));

        Response getResponse = client.get("/usuarios/busca@example.com");
        assertEquals(200, getResponse.code());
        
        JsonNode actualJson = objectMapper.readTree(getResponse.body().string());
        assertEquals("Busca", actualJson.get("nome").asText());
        assertEquals("busca@example.com", actualJson.get("email").asText());
        assertEquals(25, actualJson.get("idade").asInt());

        Response notFoundResponse = client.get("/usuarios/naoexiste@example.com");
        assertEquals(404, notFoundResponse.code()); 
        assertEquals("Usuário não encontrado", notFoundResponse.body().string());
    }

    @Test // Teste para Atualizar Usuário (Adicionado)
    public void testAtualizarUsuario() throws IOException {
        String originalEmail = "original@example.com";
        String userJsonOriginal = "{\"nome\": \"Original\", \"email\": \"" + originalEmail + "\", \"idade\": 20}";
        client.post("/usuarios", createJsonRequestBody(userJsonOriginal));

        String updatedUserJson = "{\"nome\": \"Atualizado\", \"email\": \"novo.email@example.com\", \"idade\": 21}";
        Response putResponse = client.put("/usuarios/" + originalEmail, createJsonRequestBody(updatedUserJson));
        assertEquals(200, putResponse.code());

        JsonNode actualJson = objectMapper.readTree(putResponse.body().string());
        assertEquals("Atualizado", actualJson.get("nome").asText());
        assertEquals("novo.email@example.com", actualJson.get("email").asText());
        assertEquals(21, actualJson.get("idade").asInt());

        // Verifica se o usuário original foi removido e o novo existe
        assertEquals(404, client.get("/usuarios/" + originalEmail).code());
        assertEquals(200, client.get("/usuarios/novo.email@example.com").code());
    }

    @Test // Teste para Atualizar Usuário Não Encontrado (Adicionado)
    public void testAtualizarUsuarioNaoEncontrado() throws IOException {
        String nonExistentEmail = "naoexiste@example.com";
        String updatedUserJson = "{\"nome\": \"Atualizado\", \"email\": \"naoexiste@example.com\", \"idade\": 99}";
        Response putResponse = client.put("/usuarios/" + nonExistentEmail, createJsonRequestBody(updatedUserJson));
        assertEquals(404, putResponse.code());
        assertEquals("Usuário não encontrado para atualização.", putResponse.body().string());
    }

    @Test // Teste para Deletar Usuário (Adicionado)
    public void testDeletarUsuario() throws IOException {
        String emailParaDeletar = "deletar@example.com";
        String userJson = "{\"nome\": \"Para Deletar\", \"email\": \"" + emailParaDeletar + "\", \"idade\": 10}";
        client.post("/usuarios", createJsonRequestBody(userJson)); // Cria o usuário

        Response deleteResponse = client.delete("/usuarios/" + emailParaDeletar);
        assertEquals(204, deleteResponse.code()); // 204 No Content para sucesso de DELETE

        // Tenta buscar o usuário deletado para confirmar que foi removido
        Response getResponse = client.get("/usuarios/" + emailParaDeletar);
        assertEquals(404, getResponse.code());
        assertEquals("Usuário não encontrado", getResponse.body().string());
    }

    @Test // Teste para Deletar Usuário Não Encontrado (Adicionado)
    public void testDeletarUsuarioNaoEncontrado() throws IOException {
        String nonExistentEmail = "naoexiste_deletar@example.com";
        Response deleteResponse = client.delete("/usuarios/" + nonExistentEmail);
        assertEquals(404, deleteResponse.code());
        assertEquals("Usuário não encontrado para exclusão.", deleteResponse.body().string());
    }

    // --- Testes de Tarefas (CRUD) ---

    @Test
    public void testCriarListarTarefa() throws IOException {
        String tarefaJson = "{\"titulo\": \"Comprar pão\", \"descricao\": \"Pão francês\", \"concluida\": false}";
        Response postResponse = client.post("/tarefas", createJsonRequestBody(tarefaJson));
        assertEquals(201, postResponse.code()); 
        
        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(postResponse.body().string());
        assertTrue(jsonNode.has("id"), "Resposta da tarefa criada deve conter um ID.");
        assertTrue(jsonNode.get("id").asInt() > 0, "ID da tarefa deve ser maior que 0.");
        assertEquals("Comprar pão", jsonNode.get("titulo").asText());
        assertEquals("Pão francês", jsonNode.get("descricao").asText());
        assertEquals(false, jsonNode.get("concluida").asBoolean());

        Response getResponse = client.get("/tarefas");
        assertEquals(200, getResponse.code()); 
        JsonNode tarefasList = objectMapper.readTree(getResponse.body().string());
        assertTrue(tarefasList.isArray(), "A resposta de /tarefas deve ser um array JSON.");
        assertTrue(tarefasList.size() > 0, "A lista de tarefas não deve estar vazia após a criação.");
        
        boolean found = false;
        for (JsonNode tarefaNode : tarefasList) {
            if (tarefaNode.has("titulo") && tarefaNode.get("titulo").asText().equals("Comprar pão")) {
                found = true;
                assertTrue(tarefaNode.has("id"));
                assertEquals("Pão francês", tarefaNode.get("descricao").asText());
                assertEquals(false, tarefaNode.get("concluida").asBoolean());
                break;
            }
        }
        assertTrue(found, "Tarefa criada não encontrada na lista de tarefas.");
    }

    @Test
    public void testBuscarTarefaPorId() throws IOException {
        String tarefaJson = "{\"titulo\": \"Estudar Java\", \"descricao\": \"Fazer exercícios\", \"concluida\": false}";
        Response postResponse = client.post("/tarefas", createJsonRequestBody(tarefaJson));
        assertEquals(201, postResponse.code());

        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(postResponse.body().string());
        int idTarefa = jsonNode.get("id").asInt();

        Response getResponse = client.get("/tarefas/" + idTarefa);
        assertEquals(200, getResponse.code());
        
        JsonNode actualResponseJson = objectMapper.readTree(getResponse.body().string());
        assertEquals("Estudar Java", actualResponseJson.get("titulo").asText());
        assertEquals("Fazer exercícios", actualResponseJson.get("descricao").asText());
        assertEquals(false, actualResponseJson.get("concluida").asBoolean());
        assertEquals(idTarefa, actualResponseJson.get("id").asInt());
        assertTrue(actualResponseJson.has("id"), "A tarefa retornada deve ter um ID.");
    }

    @Test // Teste para Atualizar Tarefa (Adicionado)
    public void testAtualizarTarefa() throws IOException {
        String tarefaJsonOriginal = "{\"titulo\": \"Tarefa Antiga\", \"descricao\": \"Descrição Antiga\", \"concluida\": false}";
        Response postResponse = client.post("/tarefas", createJsonRequestBody(tarefaJsonOriginal));
        assertEquals(201, postResponse.code());
        ObjectNode originalTask = (ObjectNode) objectMapper.readTree(postResponse.body().string());
        int taskId = originalTask.get("id").asInt();

        String updatedTaskJson = "{\"id\": " + taskId + ", \"titulo\": \"Tarefa Nova\", \"descricao\": \"Nova Descrição\", \"concluida\": true}";
        Response putResponse = client.put("/tarefas/" + taskId, createJsonRequestBody(updatedTaskJson));
        assertEquals(200, putResponse.code());

        JsonNode actualJson = objectMapper.readTree(putResponse.body().string());
        assertEquals("Tarefa Nova", actualJson.get("titulo").asText());
        assertEquals("Nova Descrição", actualJson.get("descricao").asText());
        assertEquals(true, actualJson.get("concluida").asBoolean());
        assertEquals(taskId, actualJson.get("id").asInt());

        Response getResponse = client.get("/tarefas/" + taskId);
        assertEquals(200, getResponse.code());
        assertEquals("Tarefa Nova", objectMapper.readTree(getResponse.body().string()).get("titulo").asText());
    }

    @Test // Teste para Atualizar Tarefa Não Encontrada (Adicionado)
    public void testAtualizarTarefaNaoEncontrada() throws IOException {
        String updatedTaskJson = "{\"titulo\": \"Tarefa Não Existente\", \"descricao\": \"Desc\", \"concluida\": false}";
        Response putResponse = client.put("/tarefas/999", createJsonRequestBody(updatedTaskJson));
        assertEquals(404, putResponse.code());
        assertEquals("Tarefa não encontrada para atualização.", putResponse.body().string());
    }

    @Test // Teste para Deletar Tarefa (Adicionado)
    public void testDeletarTarefa() throws IOException {
        String tarefaJson = "{\"titulo\": \"Tarefa para deletar\", \"descricao\": \"Desc\", \"concluida\": false}";
        Response postResponse = client.post("/tarefas", createJsonRequestBody(tarefaJson));
        assertEquals(201, postResponse.code());
        ObjectNode createdTask = (ObjectNode) objectMapper.readTree(postResponse.body().string());
        int taskId = createdTask.get("id").asInt();

        Response deleteResponse = client.delete("/tarefas/" + taskId);
        assertEquals(204, deleteResponse.code()); // 204 No Content

        Response getResponse = client.get("/tarefas/" + taskId);
        assertEquals(404, getResponse.code());
        assertEquals("Tarefa não encontrada", getResponse.body().string());
    }

    @Test // Teste para Deletar Tarefa Não Encontrada (Adicionado)
    public void testDeletarTarefaNaoEncontrada() throws IOException {
        Response deleteResponse = client.delete("/tarefas/999");
        assertEquals(404, deleteResponse.code());
        assertEquals("Tarefa não encontrada para exclusão.", deleteResponse.body().string());
    }

    // --- Testes de Validação ---

    @Test
    public void testCriarUsuarioComEmailInvalido() throws IOException {
        String userJsonNullEmail = "{\"nome\": \"Invalido\", \"email\": null, \"idade\": 20}";
        Response responseNull = client.post("/usuarios", createJsonRequestBody(userJsonNullEmail));
        assertEquals(400, responseNull.code());
        assertEquals("Email é obrigatório", responseNull.body().string());

        String userJsonEmptyEmail = "{\"nome\": \"Invalido\", \"email\": \"\", \"idade\": 20}";
        Response responseEmpty = client.post("/usuarios", createJsonRequestBody(userJsonEmptyEmail));
        assertEquals(400, responseEmpty.code());
        assertEquals("Email é obrigatório", responseEmpty.body().string());

        String userJsonBlankEmail = "{\"nome\": \"Invalido\", \"email\": \"   \", \"idade\": 20}";
        Response responseBlank = client.post("/usuarios", createJsonRequestBody(userJsonBlankEmail));
        assertEquals(400, responseBlank.code());
        assertEquals("Email é obrigatório", responseBlank.body().string());
    }

    @Test
    public void testCriarTarefaComTituloInvalido() throws IOException {
        String tarefaJsonNullTitulo = "{\"titulo\": null, \"descricao\": \"descr\", \"concluida\": false}";
        Response responseNull = client.post("/tarefas", createJsonRequestBody(tarefaJsonNullTitulo));
        assertEquals(400, responseNull.code());
        assertEquals("Título é obrigatório", responseNull.body().string());

        String tarefaJsonEmptyTitulo = "{\"titulo\": \"\", \"descricao\": \"descr\", \"concluida\": false}";
        Response responseEmpty = client.post("/tarefas", createJsonRequestBody(tarefaJsonEmptyTitulo));
        assertEquals(400, responseEmpty.code());
        assertEquals("Título é obrigatório", responseEmpty.body().string());

        String tarefaJsonBlankTitulo = "{\"titulo\": \"   \", \"descricao\": \"descr\", \"concluida\": false}";
        Response responseBlank = client.post("/tarefas", createJsonRequestBody(tarefaJsonBlankTitulo));
        assertEquals(400, responseBlank.code());
        assertEquals("Título é obrigatório", responseBlank.body().string());
    }

    @Test
    public void testCriarUsuarioDuplicado() throws IOException {
        String userJsonDuplicado = "{\"nome\": \"Duplicado\", \"email\": \"duplicado@example.com\", \"idade\": 40}";
        Response postResponse1 = client.post("/usuarios", createJsonRequestBody(userJsonDuplicado));
        assertEquals(201, postResponse1.code());

        Response postResponse2 = client.post("/usuarios", createJsonRequestBody(userJsonDuplicado));
        assertEquals(409, postResponse2.code()); // 409 Conflito (já existe)
        assertEquals("Usuário já existe", postResponse2.body().string());
    }

    @Test
    public void testJsonMalformed() throws IOException {
        String malformedJson = "{ \"nome\": \"Teste\", \"email\": \"teste@example.com\", \"idade\": 30, }"; 
        Response malformedResponse = client.post("/usuarios", createJsonRequestBody(malformedJson));

        assertEquals(400, malformedResponse.code());
        assertEquals("JSON mal-formado ou formato de dados inválido.", malformedResponse.body().string());
    }

    @Test
    public void testTarefaIdNaoNumerico() throws IOException {
        Response response400 = client.get("/tarefas/abc");
        assertEquals(400, response400.code());
        assertEquals("ID inválido", response400.body().string());
    }
}