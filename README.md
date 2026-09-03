# SavagePvP - Prueba técnica

Plugin compatible con la API de Spigot 1.8.8 para almacenar y visualizar registros de tipo `key/value`.

## Características

- `/prueba save <key> <value>` crea o actualiza un registro.
- `/prueba view <key>` consulta un registro.
- `/prueba list` abre una GUI paginada con todos los registros.
- Keys validadas con las restricciones indicadas en la prueba: `[a-zA-Z0-9_]{1,16}`.
- Values sin restricciones de contenido.
- SQLite como almacenamiento persistente.
- Acceso a SQLite ejecutado en un executor dedicado, fuera del hilo principal.
- Los resultados asíncronos se vuelven a procesar en el hilo principal únicamente para interactuar con Bukkit y el jugador.
- La GUI mantiene un snapshot de los datos para que los clics no hagan consultas bloqueantes.

## Requisitos

- Java 21.
- Gradle 8.10 o superior.
- Un servidor compatible con Spigot API 1.8.8.

## Compilación

Desde la raíz del proyecto:

```bash
gradle clean build
```

El JAR se genera en `build/libs/SavagePvP-Prueba-1.0.0.jar`.

Copia ese JAR a la carpeta `plugins/` del servidor.

## Arquitectura

```text
PruebaCommand
      |
      v
KeyValueService
      |
      v
KeyValueRepository
      |
      v
SqliteKeyValueRepository
      |
      v
    SQLite
```

### Model
`KeyValueEntry` representa un registro inmutable de key/value.

### Validation
`KeyValidator` centraliza la regla de negocio de la key y evita duplicar validaciones entre comandos.

### Service
`KeyValueService` contiene las reglas de negocio y expone una API basada en `CompletableFuture`.

### Repository
`KeyValueRepository` abstrae la persistencia. La implementación SQLite utiliza un executor de un solo hilo para serializar las operaciones de base de datos y evitar accesos concurrentes a una misma conexión.

La apertura de la conexión y la creación de la tabla también se ejecutan de forma asíncrona.

### GUI
`EntryListGui` presenta hasta 45 registros por página. El snapshot obtenido de forma asíncrona se guarda por jugador mientras la GUI está abierta. Los botones permiten navegar entre páginas y un clic sobre un registro muestra su contenido en el chat.

## Permisos

El comando requiere `prueba.use`.

## Decisiones técnicas

Se eligió SQLite porque la prueba permite almacenamiento local y no requiere infraestructura externa. El driver SQLite es la única dependencia de runtime adicional utilizada. La separación Repository/Service/Command facilita cambiar el sistema de almacenamiento sin modificar la lógica de comandos.

Java 21 fue elegido porque la prueba permite versiones modernas de Java y el código no depende de APIs exclusivas de una versión concreta de Minecraft.
