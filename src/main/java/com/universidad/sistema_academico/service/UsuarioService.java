package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.model.Usuario;
import com.universidad.sistema_academico.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setFechaRegistro(LocalDateTime.now());
            usuario.setActivo(true);
        }
        return usuarioRepository.save(usuario);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Optional<Usuario> findByDocumento(String documento) {
        return usuarioRepository.findByDocumento(documento);
    }
}