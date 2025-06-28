package app;

import app.controller.TarefaController;
import app.controller.UsuarioController;
import app.service.TarefaService;
import app.service.UsuarioService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson; 

import java.time.LocalDateTime;
import java.util.Map;

public class App {

    private static final UsuarioService usuarioService = new UsuarioService();
    private static final TarefaService tarefaService = new TarefaService();

    public static void resetState() {
        usuarioService.resetState();
        tarefaService.resetState();
    }

    public static Javalin createApp() {
        ObjectMapper customObjectMapper = new ObjectMapper();
        customObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(customObjectMapper, true)); 
            config.router.ignoreTrailingSlashes = true; 
            config.showJavalinBanner = false; 

            config.requestLogger.http((ctx, executionTimeMs) -> {
                System.out.println(">>> Requisição Recebida: " + ctx.method() + " " + ctx.path() + " | Tempo: " + executionTimeMs + "ms");
                if ((ctx.method().equals("POST") || ctx.method().equals("PUT")) && ctx.body().length() > 0) { 
                    System.out.println("    Corpo da Requisição: " + ctx.body());
                }
            });
        }); 

        app.after(ctx -> {
            System.out.println("<<< Resposta Enviada: " + ctx.method() + " " + ctx.path() + " | Status: " + ctx.status());
        });

        app.get("/hello", ctx -> ctx.result("Olá, Javalin!"));
        app.get("/status", ctx -> {
            ctx.json(Map.of("status", "ok", "timestamp", LocalDateTime.now().toString()));
        });
        app.post("/echo", ctx -> {
            ctx.json(ctx.bodyAsClass(Map.class)); 
        });
        app.get("/saudacao/{nome}", ctx -> {
            String nome = ctx.pathParam("nome");
            ctx.json(Map.of("mensagem", "Olá, " + nome + "!"));
        });

        new UsuarioController(usuarioService).registrarEndpoints(app);
        new TarefaController(tarefaService).registrarEndpoints(app);

        return app; 
    }

    public static void main(String[] args) {
        Javalin app = createApp(); 
        app.start(7000); 
        System.out.println("Servidor Javalin iniciado na porta 7000.");
        System.out.println("Acesse: http://localhost:7000/hello");
        // CORRIGIDO: O caractere de escape '\' foi escapado com '\\'
        System.out.println("Endpoints disponíveis: /hello, /status, /echo, /saudacao/{nome}, /usuarios (CRUD), /tarefas (CRUD)");
        System.out.println("Para testar o cliente Java, execute '.\\gradlew runClient' em um novo terminal."); 
    }
}