# travelupp - Plataforma Web de Gestión de Viajes

Aplicación web desarrollada con Jakarta EE siguiendo el patrón de diseño MVC, con peticiones asíncronas mediante promesas para la intercomunicación entre cliente y servidor, accediendo a una base de datos MySQL.

## Descripción

travelupp es una plataforma para gestionar viajes, permitiendo crear, visualizar, editar y eliminar información sobre destinos, fechas, participantes y estados de los viajes.

## Tecnologías

- **Backend**: Jakarta EE (Java 21)
  - Servlets para controladores
  - JDBC para conexión a MySQL
  - Gson para serialización JSON
  - Tomcat 10.1 como servidor de aplicaciones

- **Frontend**: HTML5, CSS3 y JavaScript vanilla
  - Diseño responsive con Flexbox y Grid
  - Medidas relativas (rem, em, %, vh/vw)
  - Media queries para adaptabilidad
  - Fetch API con promesas para peticiones asíncronas
  - Manejo de errores con then() y catch()

- **Base de datos**: MySQL 8.0

- **Containerización**: Docker y Docker Compose

## Arquitectura MVC

```
src/main/
├── java/com/travelupp/
│   ├── model/                    # Modelos
│   │   ├── ConexionBD.java       # Conexión a base de datos
│   │   ├── Viaje.java            # Entidad Viaje
│   │   ├── ListarViajesDAO.java  # DAO para listar (READ)
│   │   ├── CrearViajeDAO.java    # DAO para crear (CREATE)
│   │   ├── ModificarViajeDAO.java # DAO para modificar (UPDATE)
│   │   └── EliminarViajeDAO.java # DAO para eliminar (DELETE)
│   └── controller/               # Controladores
│       ├── ListarViajesServlet.java
│       ├── CrearViajeServlet.java
│       ├── ModificarViajeServlet.java
│       └── EliminarViajeServlet.java
└── webapp/                       # Vista
    ├── index.html
    ├── css/
    │   └── estilos.css
    ├── js/
    │   └── app.js
    └── WEB-INF/
        └── web.xml
```

## CRUD Completo

- **C (Create)**: `POST /api/crear-viaje` → `CrearViajeServlet` → `CrearViajeDAO`
- **R (Read)**: `GET /api/listar-viajes` → `ListarViajesServlet` → `ListarViajesDAO`
- **U (Update)**: `PUT /api/modificar-viaje` → `ModificarViajeServlet` → `ModificarViajeDAO`
- **D (Delete)**: `DELETE /api/eliminar-viaje` → `EliminarViajeServlet` → `EliminarViajeDAO`

## Instalación y Ejecución

### Requisitos previos

- Docker y Docker Compose instalados

### Puesta en marcha

```bash
cd travelupp-jakartaee
docker compose up --build
```

## URLs de Acceso

- **Aplicación web**: http://localhost:8080
- **phpMyAdmin**: http://localhost:8081
  - Usuario: `root`
  - Contraseña: `root`
- **MySQL** (desde el host): `localhost:3307`
  - Base de datos: `travelupp_db`
  - Usuario: `root`
  - Contraseña: `root`

## Características del Diseño

### Paleta de Colores

- **Azul primario**: #92C0E8 (botones principales, logo)
- **Naranja secundario**: #EEB19A (iconos de viajes, detalles)
- **Fondo claro**: #E8F2FF (azul claro)
- **Fondo cálido**: #FFF9F7 (naranja claro)

### Tipografía

- Familia: System fonts (-apple-system, BlinkMacSystemFont, Segoe UI, Roboto)
- Jerarquía: h1 (2rem), h2 (1.75rem/1.5rem), h3 (1.25rem)
- Texto base: 1rem con line-height 1.6

### Estados Interactivos

- **Hover**: Cambios de color, elevación (transform: translateY)
- **Focus**: Border color + box-shadow con el color primario
- **Active**: Clase especial para formularios

### Accesibilidad

- Etiquetas `label` en todos los campos de formulario
- Atributos `aria-label` en botones icónicos
- Contraste adecuado entre texto y fondo
- Navegación con teclado soportada

## Funcionamiento

1. El usuario accede a la página principal
2. Ve la lista de viajes existentes (petición GET asíncrona)
3. Puede crear un nuevo viaje mediante formulario (petición POST)
4. Puede editar un viaje existente (petición PUT)
5. Puede eliminar un viaje (petición DELETE)
6. Todas las operaciones se realizan mediante fetch() con promesas
7. Los errores se capturan en catch() y se muestran al usuario

## Formato de Datos JSON

### Petición para crear/modificar viaje

```json
{
  "nombre": "Verano en Barcelona",
  "destino": "Barcelona, España",
  "fechaInicio": "2026-07-15",
  "fechaFin": "2026-07-22",
  "participantes": "Ana García, Carlos López",
  "estado": "En preparación"
}
```

### Respuesta del servidor

```json
{
  "ok": true,
  "total": 3,
  "viajes": [
    {
      "id": 1,
      "nombre": "Verano en Barcelona",
      "destino": "Barcelona, España",
      "fechaInicio": "2026-07-15",
      "fechaFin": "2026-07-22",
      "participantes": "Ana García, Carlos López",
      "estado": "En preparación"
    }
  ]
}
```

## Despliegue en Railway

El proyecto está listo para desplegarse en Railway.com (Plan Free) mediante Docker Compose. Los contenedores orquestados son:

1. **MySQL**: Base de datos
2. **phpMyAdmin**: Gestor visual de BD
3. **Tomcat**: Servidor de aplicaciones con la webapp

## Autor

Proyecto desarrollado como examen de Programación y Lenguajes de Marcas - Tercera Evaluación
