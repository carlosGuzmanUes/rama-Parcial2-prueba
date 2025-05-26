package com.example.rama.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rama.model.Materia;
import com.example.rama.service.MateriaService;

@RestController
@RequestMapping("/api/materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public List<Materia> listarMaterias() {
        return materiaService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public Optional<Materia> obtenerMateria(@PathVariable Long id) {
        return materiaService.obtenerPorId(id);
    }

    @PostMapping
    public Materia crear(@RequestBody Materia materia) {
        return materiaService.crear(materia);
    }

    @PutMapping("/{id}")
    public Materia actualizar(@PathVariable Long id, @RequestBody Materia materia) {
        return materiaService.actualizar(id, materia);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        materiaService.eliminar(id);
    }
}
