package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.dto.DocenteDTO;
import com.universidad.sistema_academico.model.Docente;
import com.universidad.sistema_academico.repository.DocenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocenteService {

    private final DocenteRepository docenteRepository;

    public DocenteService(DocenteRepository docenteRepository) {
        this.docenteRepository = docenteRepository;
    }

    @Transactional(readOnly = true)
    public List<DocenteDTO> listarTodos() {
        return docenteRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocenteDTO buscarPorId(Long id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado con ID: " + id));
        return convertirADTO(docente);
    }

    public DocenteDTO guardar(DocenteDTO dto) {
        validarUnicidad(dto);
        Docente docente = convertirAEntidad(dto);
        return convertirADTO(docenteRepository.save(docente));
    }

    public void eliminar(Long id) {
        if (!docenteRepository.existsById(id)) {
            throw new RuntimeException("Docente no encontrado con ID: " + id);
        }
        docenteRepository.deleteById(id);
    }

    private void validarUnicidad(DocenteDTO dto) {
        docenteRepository.findByCodigoDocente(dto.getCodigoDocente()).ifPresent(d -> {
            if (dto.getIdDocente() == null || !d.getIdDocente().equals(dto.getIdDocente())) {
                throw new RuntimeException("Código de docente duplicado");
            }
        });

        docenteRepository.findByDni(dto.getDni()).ifPresent(d -> {
            if (dto.getIdDocente() == null || !d.getIdDocente().equals(dto.getIdDocente())) {
                throw new RuntimeException("DNI de docente duplicado");
            }
        });
    }

    private DocenteDTO convertirADTO(Docente d) {
        return new DocenteDTO(
                d.getIdDocente(),
                d.getCodigoDocente(),
                d.getDni(),
                d.getNombres(),
                d.getApellidoPaterno(),
                d.getApellidoMaterno(),
                d.getEspecialidad(),
                d.getEmail(),
                d.getCelular(),
                d.getEstado()
        );
    }

    private Docente convertirAEntidad(DocenteDTO dto) {
        Docente d = new Docente();
        d.setIdDocente(dto.getIdDocente());
        d.setCodigoDocente(dto.getCodigoDocente());
        d.setDni(dto.getDni());
        d.setNombres(dto.getNombres());
        d.setApellidoPaterno(dto.getApellidoPaterno());
        d.setApellidoMaterno(dto.getApellidoMaterno());
        d.setEspecialidad(dto.getEspecialidad());
        d.setEmail(dto.getEmail());
        d.setCelular(dto.getCelular());
        d.setEstado(dto.getEstado());
        return d;
    }
}
