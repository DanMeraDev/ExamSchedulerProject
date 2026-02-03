package com.example.examscheduler.modelo;

import java.util.List;

public record Estudiante(String id, List<Curso> cursos) {
}
