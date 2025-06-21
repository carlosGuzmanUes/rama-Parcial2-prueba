package com.example.rama.service;
import com.example.rama.model.ClassActivities;
import com.example.rama.repository.ClassActivitiesRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClassActivitiesService {

    private final ClassActivitiesRepository repository;

    public ClassActivitiesService(ClassActivitiesRepository repository) {
        this.repository = repository;
    }

    public List<ClassActivities> findAll() {
        return repository.findAll();
    }

    // ✅ CORRECCIÓN: Cambiar void a ClassActivities para retornar la entidad guardada
    public ClassActivities save(ClassActivities activity) {
        return repository.save(activity);
    }

    public void delete(ClassActivities activity) {
        repository.delete(activity);
    }

    // ✅ MÉTODOS ADICIONALES ÚTILES
    public Optional<ClassActivities> findById(Long id) {
        return repository.findById(id);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    // Métodos de búsqueda (si tienes el repositorio extendido)
    public List<ClassActivities> findByMateria(String materia) {
        return repository.findByMateriaOrderByDateDesc(materia);
    }

    public List<ClassActivities> findByGrupo(String grupo) {
        return repository.findByGrupoOrderByDateDesc(grupo);
    }

    public List<ClassActivities> findByMateriaAndGrupo(String materia, String grupo) {
        return repository.findByMateriaAndGrupoOrderByDateDesc(materia, grupo);
    }

    public List<ClassActivities> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateBetweenOrderByDateDesc(startDate, endDate);
    }

    public List<ClassActivities> findByTipoActividad(ClassActivities.TipoActividad tipo) {
        return repository.findByTipoActividadOrderByDateDesc(tipo);
    }

    public List<ClassActivities> findWithFilters(String materia, String grupo, 
                                                ClassActivities.TipoActividad tipoActividad,
                                                LocalDate startDate, LocalDate endDate, 
                                                String docente) {
        return repository.findActivitiesWithFilters(materia, grupo, tipoActividad, 
                                                   startDate, endDate, docente);
    }

    // Métodos para obtener opciones de filtros
    public List<String> getDistinctMaterias() {
        return repository.findDistinctMaterias();
    }

    public List<String> getDistinctGrupos() {
        return repository.findDistinctGrupos();
    }

    public List<String> getDistinctDocentes() {
        return repository.findDistinctDocentes();
    }
}
