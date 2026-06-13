package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Docente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {

    Optional<Docente> findByCodigoDocente(String codigoDocente);

    Optional<Docente> findByDni(String dni);

    Optional<Docente> findByEmail(String email);

    boolean existsByCodigoDocente(String codigoDocente);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    List<Docente> findByEstado(String estado);

    // ========== PAGINACIÓN Y FILTROS ==========

    @Query("SELECT d FROM Docente d WHERE d.eliminado = false OR d.eliminado IS NULL")
    List<Docente> findAllActive();

    @Query("SELECT d FROM Docente d WHERE d.eliminado = false OR d.eliminado IS NULL")
    Page<Docente> findAllActivePaged(Pageable pageable);

    @Query(value = "SELECT d.* FROM academico.docente d " +
            "WHERE (:nombre IS NULL OR d.nombres ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
            "d.apellido_paterno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
            "d.apellido_materno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%')) AND " +
            "(:especialidad IS NULL OR d.especialidad = CAST(:especialidad AS VARCHAR)) AND " +
            "(:estado IS NULL OR d.estado = CAST(:estado AS VARCHAR)) AND " +
            "(d.eliminado = false OR d.eliminado IS NULL) " +
            "ORDER BY d.id_docente DESC",
            countQuery = "SELECT COUNT(*) FROM academico.docente d " +
                    "WHERE (:nombre IS NULL OR d.nombres ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
                    "d.apellido_paterno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
                    "d.apellido_materno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%')) AND " +
                    "(:especialidad IS NULL OR d.especialidad = CAST(:especialidad AS VARCHAR)) AND " +
                    "(:estado IS NULL OR d.estado = CAST(:estado AS VARCHAR)) AND " +
                    "(d.eliminado = false OR d.eliminado IS NULL)",
            nativeQuery = true)
    Page<Docente> findWithFilters(@Param("nombre") String nombre,
                                  @Param("especialidad") String especialidad,
                                  @Param("estado") String estado,
                                  Pageable pageable);
}