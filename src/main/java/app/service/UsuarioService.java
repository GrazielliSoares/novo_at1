package app.service;

import app.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UsuarioService {
    private static final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();

    // Adicionar novo usuário (C - Create)
    public Usuario adicionarUsuario(Usuario novoUsuario) {
        if (novoUsuario.getEmail() == null || novoUsuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (usuarios.containsKey(novoUsuario.getEmail())) {
            return null; // Usuário já existe, retorna null para indicar falha
        }
        usuarios.put(novoUsuario.getEmail(), novoUsuario);
        return novoUsuario;
    }

    // Buscar usuário por email (R - Read)
    public Usuario buscarUsuarioPorEmail(String email) {
        return usuarios.get(email);
    }

    // Listar todos os usuários (R - Read)
    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios.values());
    }

    // Atualizar usuário (U - Update)
    public Usuario atualizarUsuario(String email, Usuario usuarioAtualizado) {
        // Validação: email não pode ser nulo ou vazio
        if (usuarioAtualizado.getEmail() == null || usuarioAtualizado.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        // Validação: verifica se o email no path corresponde ao email no corpo
        if (!email.equals(usuarioAtualizado.getEmail())) {
             // Caso os emails sejam diferentes, verifica se o novo email já existe para outro usuário.
            if (usuarios.containsKey(usuarioAtualizado.getEmail()) && !usuarios.get(usuarioAtualizado.getEmail()).getEmail().equals(email)) {
                throw new IllegalArgumentException("Novo email já está em uso por outro usuário.");
            }
             // Remove o usuário com o email antigo e adiciona com o novo email
            usuarios.remove(email);
            usuarios.put(usuarioAtualizado.getEmail(), usuarioAtualizado);
            return usuarioAtualizado;
        }

        // Se o email não mudou, apenas atualiza o objeto existente
        if (usuarios.containsKey(email)) {
            usuarios.put(email, usuarioAtualizado); // Sobrescreve o usuário existente
            return usuarioAtualizado;
        }
        return null; // Usuário não encontrado para atualização
    }

    // Deletar usuário (D - Delete) 
    public boolean deletarUsuario(String email) {
        // Retorna true se o usuário foi removido, false caso contrário (não encontrado)
        return usuarios.remove(email) != null;
    }

    // Resetar estado (para testes)
    public void resetState() {
        usuarios.clear();
    }
}