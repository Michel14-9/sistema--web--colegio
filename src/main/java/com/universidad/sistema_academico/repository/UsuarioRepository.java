package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar por username (para login)
    Optional<Usuario> findByUsername(String username);

    // Buscar por email
    Optional<Usuario> findByEmail(String email);

    // Buscar por rol
    List<Usuario> findByRol(String rol);

    // Buscar usuarios activos por rol
    List<Usuario> findByRolAndActivoTrue(String rol);

    // Verificar si existe username
    boolean existsByUsername(String username);

    // Verificar si existe email
    boolean existsByEmail(String email);

    // Buscar por documento
    Optional<Usuario> findByDocumento(String documento);

    // Búsqueda personalizada con JPQL
    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND u.rol = :rol")
    List<Usuario> findActiveUsersByRole(@Param("rol") String rol);
}