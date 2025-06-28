package app.controller;

import app.model.Usuario;
import app.service.UsuarioService;
import io.javalin.Javalin;
import io.javalin.http.Context;

// Início da classe UsuarioController
public class UsuarioController { 

    private final UsuarioService usuarioService;

    // Início do construtor
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    } // Fim do construtor

    // Início do método registrarEndpoints
    public void registrarEndpoints(Javalin app) {
        app.post("/usuarios", this::criarUsuario); // C - Create
        app.get("/usuarios", this::listarUsuarios); // R - Read all
        app.get("/usuarios/{email}", this::buscarUsuarioPorEmail); // R - Read one
        app.put("/usuarios/{email}", this::atualizarUsuario); // U - Update
        app.delete("/usuarios/{email}", this::deletarUsuario); // D - Delete
    } // Fim do método registrarEndpoints

    // Início do método criarUsuario
    private void criarUsuario(Context ctx) {
        try {
            Usuario novoUsuario = ctx.bodyAsClass(Usuario.class);
            Usuario usuarioCriado = usuarioService.adicionarUsuario(novoUsuario);

            if (usuarioCriado != null) {
                ctx.status(201).json(usuarioCriado);
            } else {
                ctx.status(409).result("Usuário já existe");
            }
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao criar usuário: " + e.getMessage());
            ctx.status(400).result("JSON mal-formado ou formato de dados inválido.");
        }
    } // Fim do método criarUsuario

    // Início do método listarUsuarios
    private void listarUsuarios(Context ctx) {
        ctx.json(usuarioService.listarUsuarios());
    } // Fim do método listarUsuarios

    // Início do método buscarUsuarioPorEmail
    private void buscarUsuarioPorEmail(Context ctx) {
        String email = ctx.pathParam("email");
        Usuario usuario = usuarioService.buscarUsuarioPorEmail(email);

        if (usuario != null) {
            ctx.json(usuario);
        } else {
            ctx.status(404).result("Usuário não encontrado");
        }
    } // Fim do método buscarUsuarioPorEmail

    // Início do método atualizarUsuario
    private void atualizarUsuario(Context ctx) {
        String email = ctx.pathParam("email");
        try {
            Usuario usuarioAtualizado = ctx.bodyAsClass(Usuario.class);
            Usuario resultado = usuarioService.atualizarUsuario(email, usuarioAtualizado);

            if (resultado != null) {
                ctx.status(200).json(resultado); // 200 OK
            } else {
                ctx.status(404).result("Usuário não encontrado para atualização."); // 404 Not Found
            }
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
            ctx.status(400).result("JSON mal-formado ou formato de dados inválido.");
        }
    } // Fim do método atualizarUsuario

    // Início do método deletarUsuario
    private void deletarUsuario(Context ctx) {
        String email = ctx.pathParam("email");
        boolean deletado = usuarioService.deletarUsuario(email);

        if (deletado) {
            ctx.status(204); // 204 No Content
        } else {
            ctx.status(404).result("Usuário não encontrado para exclusão.");
        }
    } 

} 