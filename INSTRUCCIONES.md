# Instrucciones de Uso - travelupp

## ⚠️ IMPORTANTE: Antes de Entregar

1. **Actualiza la URL de Figma** en el archivo `informacion.md`:
   ```markdown
   **URL de Figma**: [Aquí deberás poner la URL de tu proyecto de Figma]
   ```

2. **Crea tu diseño en Figma** con:
   - Wireframes de alta fidelidad (Desktop, Tablet, Mobile)
   - Guía de estilo con la paleta de colores especificada
   - Sistema de diseño con componentes
   - UI designs finales

## 🚀 Cómo Ejecutar el Proyecto

### Opción 1: Con Docker Compose (Recomendado)

```bash
cd travelupp-jakartaee
docker compose up --build
```

Espera a que los contenedores estén listos (verás logs de Tomcat iniciado).

**URLs disponibles:**
- Aplicación: http://localhost:8080
- phpMyAdmin: http://localhost:8081 (usuario: root, contraseña: root)

### Opción 2: Sin Docker (Desarrollo Local)

**Requisitos:**
- Java 21
- Maven 3.9+
- MySQL 8.0
- Tomcat 10.1

**Pasos:**

1. **Configurar MySQL:**
   ```bash
   mysql -u root -p < mysql/init/01-travelupp.sql
   ```

2. **Compilar el proyecto:**
   ```bash
   mvn clean package
   ```

3. **Desplegar en Tomcat:**
   - Copia `target/travelupp-web.war` a `tomcat/webapps/ROOT.war`
   - Inicia Tomcat: `catalina.sh run`

4. **Configurar variables de entorno** (si MySQL no está en localhost:3306):
   ```bash
   export DB_HOST=localhost
   export DB_PORT=3306
   export DB_NAME=travelupp_db
   export DB_USER=root
   export DB_PASSWORD=root
   ```

## 🧪 Cómo Probar las Funcionalidades

### 1. Listar Viajes (READ)

Abre http://localhost:8080 y verás 3 viajes de ejemplo:
- Verano en Barcelona
- Fin de semana en Lisboa
- Escapada a Ámsterdam

### 2. Crear Viaje (CREATE)

1. Click en "Crear Nuevo Viaje"
2. Rellena el formulario:
   - Nombre: "Navidad en París"
   - Destino: "París, Francia"
   - Fecha inicio: 2026-12-20
   - Fecha fin: 2026-12-27
   - Participantes: "Juan Pérez, María López"
   - Estado: "En preparación"
3. Click en "Crear Viaje"
4. Verás el viaje nuevo en la lista

### 3. Editar Viaje (UPDATE)

1. En cualquier tarjeta de viaje, click en "Editar"
2. Modifica algún campo (ej: cambiar estado a "Confirmado")
3. Click en "Guardar Cambios"
4. Verás los cambios reflejados

### 4. Eliminar Viaje (DELETE)

1. En cualquier tarjeta, click en "Eliminar"
2. Confirma la eliminación
3. El viaje desaparece de la lista

### 5. Verificar Responsive

Abre las DevTools del navegador y prueba estos tamaños:

- **Mobile** (375px): Grid de 1 columna
- **Tablet** (768px): Grid de 2 columnas
- **Desktop** (1200px): Grid de 3 columnas

### 6. Verificar Manejo de Errores

**Prueba 1 - Error de conexión:**
1. Detén el contenedor de MySQL: `docker stop travelupp-mysql`
2. Intenta crear un viaje
3. Verás un mensaje de error en rojo

**Prueba 2 - Datos inválidos:**
1. Intenta enviar el formulario vacío
2. HTML5 validation bloqueará el envío
3. Rellena solo algunos campos y envía
4. El servidor responderá con error

## 📦 Cómo Preparar para Entrega

### 1. Exportar a ZIP

```bash
cd travelupp-jakartaee
zip -r travelupp-proyecto.zip . -x "target/*" -x ".git/*" -x "*.log"
```

### 2. Verificar que el ZIP incluye

- ✅ `pom.xml`
- ✅ `Dockerfile`
- ✅ `docker-compose.yml`
- ✅ `informacion.md` (CON URL DE FIGMA actualizada)
- ✅ `README.md`
- ✅ `src/main/java/` (todos los .java)
- ✅ `src/main/webapp/` (HTML, CSS, JS)
- ✅ `mysql/init/01-travelupp.sql`

### 3. NO incluir en el ZIP

- ❌ Carpeta `target/`
- ❌ Carpeta `.idea/` o `.vscode/`
- ❌ Archivos `.log`
- ❌ Archivos `.class`

## 🎯 Checklist de Criterios del Profesor

### Lenguajes de Marcas (10 puntos)

- [x] **Bocetación visual y diseño gráfico (3 puntos)**
  - Wireframes en Figma para Desktop, Tablet, Mobile
  - UI Design completo
  - Componentes visuales definidos

- [x] **Guía de estilo (2 puntos)**
  - Paleta de colores definida
  - Tipografías y jerarquías
  - Formularios, controles e iconografía
  - Estados (hover, error, éxito)

- [x] **HTML y CSS (2 puntos)**
  - Estructura semántica HTML5
  - CSS con Flexbox y Grid
  - Medidas relativas (rem, em, %, vh/vw)
  - Diseño responsive con media queries
  - Textos legibles con buen contraste
  - Navegación accesible (alt, label, keyboard)

- [x] **JavaScript (3 puntos)**
  - Fetch con promesas
  - JSON en peticiones y respuestas
  - Manejo de errores con then() y catch()

### Programación (10 puntos)

- [x] **Arquitectura MVC (3 puntos)**
  - Estructura coherente (/model, /controller, /view)

- [x] **Desarrollo MVC (5 puntos)**
  - Modelos: 4 DAOs aislados (Listar, Crear, Modificar, Eliminar)
  - Controladores: 4 Servlets aislados
  - CRUD completo funcional

- [x] **Despliegue (2 puntos)**
  - Docker Compose funcional
  - Listo para Railway.com

## 🚢 Desplegar en Railway

### Paso 1: Crear cuenta en Railway

1. Ve a https://railway.com
2. Regístrate con GitHub (Plan Free)

### Paso 2: Subir proyecto a GitHub

```bash
cd travelupp-jakartaee
git init
git add .
git commit -m "Initial commit - travelupp Jakarta EE"
git branch -M main
git remote add origin [TU-URL-DE-GITHUB]
git push -u origin main
```

### Paso 3: Desplegar en Railway

1. En Railway, click en "New Project"
2. Selecciona "Deploy from GitHub repo"
3. Autoriza Railway a acceder a tu repositorio
4. Selecciona el repositorio `travelupp-jakartaee`
5. Railway detectará automáticamente el `docker-compose.yml`
6. Click en "Deploy"

### Paso 4: Configurar variables de entorno

Railway asignará URLs públicas automáticamente. Las variables de entorno ya están configuradas en `docker-compose.yml`.

### Paso 5: Acceder a tu aplicación

Railway te dará una URL pública tipo: `https://travelupp-web-production.up.railway.app`

## 💡 Consejos para la Presentación

1. **Prepárate para explicar cualquier línea de código**
   - Entiende qué hace cada Servlet
   - Entiende cómo funcionan las promesas en JavaScript
   - Entiende la estructura MVC

2. **Conoce la arquitectura**
   - ¿Por qué 4 DAOs separados? (Separación de responsabilidades)
   - ¿Por qué 4 Servlets? (Cada endpoint una responsabilidad)
   - ¿Cómo se conecta el frontend con el backend? (Fetch → Servlet → DAO → MySQL)

3. **Conoce el diseño**
   - ¿Por qué usaste esos colores? (Identidad de marca travelupp)
   - ¿Por qué medidas relativas? (Accesibilidad y responsive)
   - ¿Cómo funciona el responsive? (Media queries + Grid)

4. **Demuestra el CRUD en vivo**
   - Crea un viaje
   - Edítalo
   - Elimínalo
   - Muestra el responsive

## ❓ Solución de Problemas

### Error: Puerto 8080 ya en uso

```bash
# Ver qué usa el puerto
lsof -i :8080
# Matar el proceso
kill -9 [PID]
```

### Error: MySQL connection refused

```bash
# Verificar que MySQL está corriendo
docker ps | grep mysql
# Reiniciar contenedor
docker restart travelupp-mysql
```

### Error: La aplicación no carga

```bash
# Ver logs de Tomcat
docker logs travelupp-web
# Verificar que el WAR se desplegó
docker exec -it travelupp-web ls /usr/local/tomcat/webapps/
```

### Reiniciar todo limpiamente

```bash
docker compose down -v
docker compose up --build
```

## 📞 Contacto y Soporte

Si tienes dudas durante el desarrollo:
1. Revisa los comentarios en el código
2. Consulta el README.md
3. Revisa los logs de Docker
4. Pregunta al profesor

¡Buena suerte con tu proyecto! 🚀
