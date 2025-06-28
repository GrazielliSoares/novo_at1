package app.controller;

import app.model.Tarefa;
import app.service.TarefaService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class TarefaController { // Início da classe

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    public void registrarEndpoints(Javalin app) {
        app.post("/tarefas", this::criarTarefa); // C - Create
        app.get("/tarefas", this::listarTarefas); // R - Read all
        app.get("/tarefas/{id}", this::buscarTarefaPorId); // R - Read one
        app.put("/tarefas/{id}", this::atualizarTarefa); // U - Update
        app.delete("/tarefas/{id}", this::deletarTarefa); // D - Delete
    }

    private void criarTarefa(Context ctx) {
        try {
            Tarefa novaTarefa = ctx.bodyAsClass(Tarefa.class);
            Tarefa tarefaCriada = tarefaService.adicionarTarefa(novaTarefa);
            ctx.status(201).json(tarefaCriada);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao criar tarefa: " + e.getMessage());
            ctx.status(400).result("JSON mal-formado ou formato de dados inválido.");
        }
    }

    private void listarTarefas(Context ctx) {
        ctx.json(tarefaService.listarTarefas());
    }

    private void buscarTarefaPorId(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Tarefa tarefa = tarefaService.buscarTarefaPorId(id);

            if (tarefa != null) {
                ctx.json(tarefa);
            } else {
                ctx.status(404).result("Tarefa não encontrada");
            }
        } catch (NumberFormatException e) {
            ctx.status(400).result("ID inválido");
        }
    }

    private void atualizarTarefa(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Tarefa tarefaAtualizada = ctx.bodyAsClass(Tarefa.class);

            Tarefa resultado = tarefaService.atualizarTarefa(id, tarefaAtualizada);

            if (resultado != null) {
                ctx.status(200).json(resultado); // 200 OK
            } else {
                ctx.status(404).result("Tarefa não encontrada para atualização."); // 404 Not Found
            }
        } catch (NumberFormatException e) {
            ctx.status(400).result("ID inválido");
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao atualizar tarefa: " + e.getMessage());
            ctx.status(400).result("JSON mal-formado ou formato de dados inválido.");
        }
    }

    private void deletarTarefa(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean deletado = tarefaService.deletarTarefa(id);

            if (deletado) {
                ctx.status(204); // 204 No Content
            } else {
                ctx.status(404).result("Tarefa não encontrada para exclusão.");
            }
        } catch (NumberFormatException e) {
            ctx.status(400).result("ID inválido");
        }
    }
} 