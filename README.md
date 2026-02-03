# 📅 Exam Scheduler – Branch & Bound

## 📌 Descripción
Software para la **asignación automática de horarios de exámenes**, cuyo objetivo es **minimizar conflictos entre cursos** mediante el uso del algoritmo **Branch & Bound**.  
El sistema modela los conflictos como un grafo y busca una solución óptima en función de las franjas horarias disponibles.

Este proyecto fue desarrollado como parte de la asignatura **Estructura de Datos y Algoritmos II**, aplicando conceptos de grafos, optimización y algoritmos exactos.

---

## ⚙️ Requisitos del Sistema

- **Java Development Kit (JDK):**  
  Versión **17 o superior** (requerido por el uso de `records`).
- **Apache Maven:**  
  Para la gestión de dependencias y construcción del proyecto.

---

## 📚 Librerías Externas

- **JGraphT**  
  `org.jgrapht:jgrapht-core:1.5.1`  
  Utilizada para la **modelización del grafo de conflictos** entre cursos.

---

## ▶️ Instrucciones de Ejecución

### 🔹 Consola / Terminal

1. Descomprimir la carpeta del proyecto.
2. Navegar hasta la raíz del proyecto (donde se encuentra el archivo `pom.xml`).
3. Compilar el proyecto ejecutando:
   ```bash
   mvn clean install
Ejecutar la aplicación:

mvn exec:java -Dexec.mainClass="com.example.examscheduler.AplicacionPrincipal"
🔹 IDE (NetBeans / IntelliJ IDEA / Eclipse)
Abrir el proyecto como Maven Project.

Esperar a que se descarguen automáticamente las dependencias (incluyendo JGraphT).

Ubicar la clase:

com.example.examscheduler.AplicacionPrincipal
Clic derecho sobre el archivo → Run File / Ejecutar.

📝 Notas Adicionales
El algoritmo puede tardar algunos segundos dependiendo del número de cursos y franjas horarias seleccionadas.

Los logs de optimización y el proceso de búsqueda se muestran en la salida estándar (consola) del IDE o terminal.


Esto es exactamente el **mismo contenido de un README.md**, solo que presentado como **texto plano**.  
Si lo deseas, puedo:
- Quitar emojis (más formal)
- Adaptarlo a una **rúbrica universitaria**
- O añadir sección de **algoritmo y complejidad**
