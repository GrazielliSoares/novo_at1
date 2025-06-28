package app.service;

import app.model.Tarefa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TarefaService {
    private static final Map<Integer, Tarefa> tarefas = new ConcurrentHashMap<>();
    private static int proximoIdTarefa = 1;

    // Adicionar nova tarefa (C - Create)
    public Tarefa adicionarTarefa(Tarefa novaTarefa) {
        if (novaTarefa.getTitulo() == null || novaTarefa.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        
        if (novaTarefa.getId() == 0) { 
            novaTarefa.setId(proximoIdTarefa++);
        } else if (novaTarefa.getId() >= proximoIdTarefa) {
            proximoIdTarefa = novaTarefa.getId() + 1;
        }

        tarefas.put(novaTarefa.getId(), novaTarefa);
        return novaTarefa;
    }

    
    public Tarefa buscarTarefaPorId(int id) {
        return tarefas.get(id);
    }

    // Listar todas as tarefas
    public List<Tarefa> listarTarefas() {
        return new ArrayList<>(tarefas.values());
    }

    // Atualizar tarefa (U - Update) 
    public Tarefa atualizarTarefa(int id, Tarefa tarefaAtualizada) {
        // Validação: título não pode ser nulo ou vazio
        if (tarefaAtualizada.getTitulo() == null || tarefaAtualizada.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        // Garante que o ID da tarefa atualizada corresponda ao ID do path, ou sobrescreve se o ID era 0
        if (tarefaAtualizada.getId() == 0 || tarefaAtualizada.getId() != id) {
             tarefaAtualizada.setId(id); // Força o ID do corpo a ser o mesmo do path
        }

        if (tarefas.containsKey(id)) {
            tarefas.put(id, tarefaAtualizada); // Sobrescreve a tarefa existente
            return tarefaAtualizada;
        }
        return null; // Tarefa não encontrada para atualização
    }

    // Deletar tarefa (D - Delete)
    public boolean deletarTarefa(int id) {
        // Retorna true se a tarefa foi removida, false caso contrário (não encontrada)
        return tarefas.remove(id) != null;
    }

    // Resetar estado (para testes)
    public void resetState() {
        tarefas.clear();
        proximoIdTarefa = 1;
    }
}