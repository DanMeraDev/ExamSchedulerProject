Exam Scheduler - Branch & Bound Optimizer 📅x
Este software automatiza la asignación de horarios de exámenes buscando minimizar los conflictos (solapamientos) entre cursos. Utiliza una implementación del algoritmo Branch & Bound para encontrar una solución óptima o cercana a la óptima dentro de las restricciones dadas.

📝 Descripción
La aplicación modela los conflictos entre exámenes mediante un grafo, donde cada nodo es un curso y las aristas representan estudiantes compartidos. El objetivo es asignar cada examen a una franja horaria de manera que se minimice la interferencia, utilizando la librería JGraphT para el manejo de estructuras de grafos.

🛠️ Requisitos del Sistema
Java Development Kit (JDK): Versión 17 o superior.

Nota: El proyecto utiliza records y otras características modernas de Java.

Apache Maven: Para la gestión de dependencias y construcción del proyecto.

📦 Librerías Externas
El proyecto depende de la siguiente librería principal:

JGraphT: (org.jgrapht:jgrapht-core:1.5.1) - Utilizada para la modelización y análisis del grafo de conflictos.

🚀 Instrucciones de Ejecución
Desde la Consola/Terminal
Descomprimir la carpeta del proyecto.

Navegar hasta la raíz del proyecto (donde reside el archivo pom.xml):

Bash
cd ruta/al/proyecto
Compilar e instalar las dependencias:

Bash
mvn clean install
Ejecutar la aplicación:

Bash
mvn exec:java -Dexec.mainClass="com.example.examscheduler.AplicacionPrincipal"
Desde un IDE (NetBeans, IntelliJ, Eclipse)
Importar/Abrir el proyecto como un "Proyecto Maven" (Maven Project).

Sincronizar: Esperar a que el IDE descargue automáticamente las dependencias desde el repositorio central de Maven.

Localizar la clase principal: AplicacionPrincipal.java dentro del paquete com.example.examscheduler.

Ejecutar: Clic derecho sobre el archivo y seleccionar Run File o Ejecutar.

💡 Notas Adicionales
Rendimiento: Debido a la naturaleza del algoritmo Branch & Bound (NP-Hard), el tiempo de ejecución puede variar desde unos segundos hasta un poco más, dependiendo de la densidad del grafo de conflictos y el número de franjas horarias.

Logs: Puedes monitorear el progreso de la optimización y las podas del algoritmo directamente en la consola de salida del IDE o terminal.
