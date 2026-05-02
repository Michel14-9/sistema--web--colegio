package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.model.Actividad;
import com.universidad.sistema_academico.repository.ActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ActividadService {

    @Autowired
    private ActividadRepository actividadRepository;

    public void registrarActividad(String usuario, String accion, String entidad, String detalle) {
        Actividad actividad = new Actividad();
        actividad.setUsuario(usuario);
        actividad.setAccion(accion);
        actividad.setEntidad(entidad);
        actividad.setDetalle(detalle);
        actividad.setFecha(LocalDateTime.now());
        actividadRepository.save(actividad);
    }
}