# MediTurno — Plataforma de Gestión de Turnos Médicos

API REST backend para la gestión de reservas médicas con roles diferenciados (paciente, médico, admin). Construida para demostrar arquitectura de producción con Spring Security, persistencia relacional y notificaciones por email.

## Stack

- **Lenguaje:** Java 17
- **Framework:** Spring Boot 3.x
- **Seguridad:** Spring Security + JWT (autenticación stateless con roles)
- **Base de datos:** PostgreSQL — migraciones versionadas con Flyway
- **ORM:** Spring Data JPA / Hibernate
- **Email:** JavaMailSender (notificaciones de confirmación y cancelación)
- **Documentación:** OpenAPI 3 / Swagger UI (`/swagger-ui.html`)
- **Tests:** JUnit 5 + Mockito — cobertura medida con JaCoCo (objetivo: 80%+)
- **Contenedores:** Docker + Docker Compose (app + PostgreSQL)
- **Build:** Maven

## Comandos

- `./mvnw spring-boot:run` — arranca el servidor en local (puerto 8080)
- `./mvnw test` — ejecuta todos los tests (deben pasar antes de cada commit)
- `./mvnw verify` — ejecuta tests + genera reporte JaCoCo en `target/site/jacoco/`
- `./mvnw clean package` — compila y empaqueta el JAR para producción
- `docker compose up --build` — levanta la app completa con base de datos

## Estructura del proyecto

```
src/
├── main/
│   ├── java/com/mediturno/
│   │   ├── config/         — Configuración de Spring Security, CORS, beans globales
│   │   ├── controller/     — Controladores REST (@RestController), sin lógica de negocio
│   │   ├── service/        — Lógica de negocio, transacciones, reglas del dominio
│   │   ├── repository/     — Interfaces JPA, queries personalizadas con @Query
│   │   ├── model/          — Entidades JPA y enums del dominio
│   │   ├── dto/            — Objetos de transferencia (request/response), nunca exponer entidades
│   │   ├── security/       — JwtFilter, UserDetailsService, generación y validación de tokens
│   │   ├── exception/      — Excepciones propias del dominio y GlobalExceptionHandler
│   │   └── notification/   — Servicio de email con JavaMailSender y templates
│   └── resources/
│       ├── db/migration/   — Scripts Flyway (V1__init.sql, V2__add_index.sql, etc.)
│       └── application.yml — Configuración por ambiente
└── test/
    ├── java/com/mediturno/
    │   ├── service/        — Tests unitarios de servicios con Mockito
    │   └── controller/     — Tests de integración con MockMvc
```

## Convenciones

- **Nombres:** camelCase para variables y métodos, PascalCase para clases, UPPER_SNAKE_CASE para constantes y enums.
- **DTOs obligatorios:** los controladores reciben y devuelven DTOs, nunca entidades JPA directamente.
- **Validación:** toda entrada del usuario se valida con Bean Validation (`@Valid`, `@NotBlank`, `@Email`, etc.) en el DTO antes de llegar al servicio.
- **Transacciones:** `@Transactional` solo en la capa de servicio, nunca en controladores ni repositorios.
- **Excepciones:** usar excepciones propias del dominio (`TurnoNotFoundException`, `HorarioNoDisponibleException`) capturadas por `GlobalExceptionHandler` con respuestas estandarizadas.
- **Migraciones Flyway:** nombradas como `V{número}__{descripción_en_snake_case}.sql`. Nunca modificar un script ya ejecutado, siempre crear uno nuevo.
- **Tests:** cada clase de servicio tiene su clase de test correspondiente (`AppointmentService` → `AppointmentServiceTest`). Los tests de integración van en `controller/`.
- **Commits:** mensajes en inglés, formato `feat:`, `fix:`, `test:`, `refactor:`, `docs:`.

## No hagas

- **No subir archivos `.env` ni `application-local.yml` al repositorio.** Las credenciales van en variables de entorno o en archivos ignorados por `.gitignore`.
- **No mezclar lógica de negocio en los controladores.** Los controladores solo reciben la request, llaman al servicio y devuelven la respuesta.
- **No crear endpoints sin documentación OpenAPI.** Cada endpoint necesita sus anotaciones `@Operation` y `@ApiResponse`.
- **No saltarse Flyway.** Ningún cambio de esquema se hace a mano en la base de datos. Todo va en un script de migración versionado.
- **No devolver stack traces al cliente.** El `GlobalExceptionHandler` captura todas las excepciones y devuelve un JSON estructurado con código de error y mensaje.
- **No confirmar un commit si los tests fallan.** Ejecutar `./mvnw test` antes de cada push.
- **No modificar la configuración de Spring Security sin entender el filter chain.** Si no estás seguro, preguntar antes de tocar `SecurityConfig`.

## Flujo de trabajo

- Antes de implementar una funcionalidad no trivial (nuevo módulo, cambio de seguridad, migración compleja), proponer el diseño y esperar aprobación.
- Una tarea a la vez. Al terminar, describir qué archivos se crearon o modificaron y por qué.
- Si la solución tiene más de un enfoque válido, presentar las opciones con sus trade-offs antes de implementar.
- Si no hay certeza al 80% sobre el comportamiento esperado, preguntar. No asumir ni inventar comportamiento del dominio médico.
- Cada nueva entidad o endpoint debe tener al menos un test antes de cerrar la tarea.

## Dominio del proyecto

**Roles:**

- `ADMIN` — gestiona médicos, especialidades y horarios disponibles
- `MEDICO` — consulta su agenda, puede marcar un turno como atendido o cancelado
- `PACIENTE` — busca disponibilidad, reserva turno, puede cancelar el suyo
  **Estados de un turno:** `PENDIENTE` → `CONFIRMADO` → `ATENDIDO` / `CANCELADO`

**Reglas de negocio clave:**

- Un médico no puede tener dos turnos solapados en el mismo horario
- Un paciente no puede reservar en el pasado
- La cancelación con menos de 2 horas de anticipación no está permitida para el paciente
- Toda reserva y cancelación dispara una notificación por email al paciente

## Documentación

- Swagger UI disponible en `http://localhost:8080/swagger-ui.html` con el servidor corriendo
- Colección Postman exportada en `/docs/MediTurno.postman_collection.json`
- Diagrama de arquitectura y modelo de datos en `/docs/architecture.md`
- Variables de entorno requeridas documentadas en `/docs/env.example`
