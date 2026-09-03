Este plugin lo desarrollé para la prueba técnica de SavagePvP y básicamente sirve para guardar datos mediante clave y valor. La clave tiene sus reglas: solo permite letras, números y guion bajo, con un máximo de 16 caracteres. El valor, en cambio, puede ser cualquier texto que quieras.

El comando principal es `/prueba` y maneja tres funciones:

- `save`: sirve para guardar o actualizar un dato.
- `view`: es para consultar un dato en concreto.
- `list`: abre una interfaz con paginación para ver todos los registros almacenados de forma cómoda.

En la parte técnica, usé SQLite para guardar todo y me aseguré de que las consultas corran de forma asíncrona para no congelar el servidor ni provocar lag. Además, estructuré el proyecto separando la lógica en *repository*, *service*, comandos y la parte visual para que el código quede limpio y sea fácil de mantener a futuro.

Está hecho con Java 21 y Gradle 8.10. Para compilarlo solo hay que ejecutar `gradle clean build`, buscar el archivo `.jar` en la carpeta `build/libs` y moverlo a la carpeta `plugins` del servidor. También le agregué una validación previa a la clave para que, si no cumple con el formato o la longitud, muestre un mensaje de error claro avisando qué falló.