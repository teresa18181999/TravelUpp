# Información del Proyecto - travelupp

## Datos del Proyecto

- **Nombre**: travelupp - Plataforma Web de Gestión de Viajes
- **Asignatura**: Programación y Lenguajes de Marcas
- **Evaluación**: Tercera Evaluación

## Diseño en Figma

El diseño visual completo del proyecto, incluyendo:

- Bocetación visual (wireframes de alta fidelidad)
- Diseño gráfico
- Guía de estilo
- Sistema de diseño

Se encuentra disponible en la siguiente URL de Figma:

**URL de Figma**: [Aquí deberás poner la URL de tu proyecto de Figma]

## Sistema de Diseño

### Paleta de Colores

- **Azul Primario**: `#92C0E8`
  - Uso: Botones principales, logo, enlaces
  - Variante hover: `#7eb3df`

- **Naranja Secundario**: `#EEB19A`
  - Uso: Iconos de viajes, badges, elementos secundarios
  - Variante hover: `#e5a589`

- **Fondos**:
  - Azul claro: `#E8F2FF`
  - Naranja claro: `#FFF9F7`
  - Blanco: `#FFFFFF`

- **Textos**:
  - Principal: `#1a1a1a`
  - Secundario: `#666666`
  - Gris claro: `#9ca3af`

- **Estados**:
  - Éxito: `#4ade80`
  - Error: `#ef4444`
  - Bordes: `#e0e0e0`

### Tipografía

- **Familia tipográfica**: System Fonts
  - `-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif`

- **Jerarquía**:
  - H1: `2rem` (32px) - Logo principal
  - H2: `1.75rem` (28px) - Títulos de sección
  - H2 secundario: `1.5rem` (24px) - Títulos de formularios
  - H3: `1.25rem` (20px) - Títulos de tarjetas
  - Texto base: `1rem` (16px)
  - Texto pequeño: `0.95rem` (15.2px)
  - Etiquetas: `0.9rem` (14.4px)
  - Badges: `0.75rem` (12px)

- **Line-height**: `1.6` para texto corrido

### Formularios y Controles

- **Inputs**:
  - Padding: `0.75rem 1rem`
  - Border: `1px solid #e0e0e0`
  - Border-radius: `1rem` (16px)
  - Focus: Border `#92C0E8` + box-shadow con opacidad 0.2

- **Botones**:
  - Primario: Fondo `#92C0E8`, texto blanco
  - Secundario: Fondo `#f3f4f6`, texto oscuro
  - Padding: `0.875rem 1.75rem`
  - Border-radius: `1rem`
  - Hover: Elevación con `translateY(-2px)` + sombra

- **Selects**:
  - Mismo estilo que inputs

### Iconografía

- **Biblioteca**: SVG inline (Lucide style)
- **Iconos principales**:
  - Avión: Logo y representación de viajes
  - Calendario: Fechas
  - Usuarios: Participantes
  - Lápiz: Editar
  - Papelera: Eliminar
  - Plus: Crear nuevo

### Estados Interactivos

- **Hover**:
  - Botones: Cambio de color + elevación
  - Tarjetas: Elevación `translateY(-4px)`
  - Enlaces: Subrayado

- **Focus**:
  - Border color primario
  - Box-shadow: `0 0 0 3px rgba(146, 192, 232, 0.2)`

- **Active**:
  - Formulario visible: Clase `.active`
  - Animación: `slideDown` con opacidad y transform

- **Error**:
  - Border rojo: `#ef4444`
  - Fondo de mensaje: `#fee2e2`
  - Texto rojo

### Componentes Visuales

#### Tarjeta de Viaje
- Fondo: Blanco
- Border-radius: `2rem` (32px)
- Padding: `1.5rem`
- Box-shadow: `0 4px 16px rgba(0, 0, 0, 0.1)`
- Hover: Elevación y sombra aumentada

#### Badges de Estado
- "En preparación": Azul con fondo `rgba(146, 192, 232, 0.15)`
- "Confirmado": Verde con fondo `rgba(74, 222, 128, 0.15)`
- "En curso": Naranja con fondo `rgba(238, 177, 154, 0.15)`
- "Completado": Gris con fondo `rgba(156, 163, 175, 0.15)`

#### Logo
- Icono: Círculo azul (`#92C0E8`) con avión blanco
- Tamaño: `3rem` (48px)
- Box-shadow: `0 4px 16px rgba(0, 0, 0, 0.1)`

### Diseño Responsive

#### Breakpoints
- **Mobile**: < 768px (48rem)
  - Grid de 1 columna
  - Formularios en columna única
  - Tamaños de fuente reducidos

- **Tablet**: 768px - 1024px (48rem - 64rem)
  - Grid de 2 columnas para viajes

- **Desktop**: > 1024px (64rem)
  - Grid de 3 columnas para viajes
  - Contenedor máximo: `75rem` (1200px)

#### Adaptaciones Mobile
- Logo: `1.75rem` (reducido desde 2rem)
- Padding de formulario: `1.5rem` (reducido desde 2rem)
- Acciones: Apiladas verticalmente
- Título de sección: `1.5rem` (reducido desde 1.75rem)

### Accesibilidad

- ✅ Etiquetas `<label>` asociadas a inputs
- ✅ Atributos `aria-label` en botones icónicos
- ✅ Contraste mínimo 4.5:1 para texto
- ✅ Estados de focus visibles
- ✅ Navegación con teclado
- ✅ Textos legibles (tamaño mínimo 14px)
- ✅ Mensajes de error descriptivos

## Wireframes

Se han diseñado wireframes de alta fidelidad para:

1. **Vista Desktop** (> 1024px)
   - Header con logo centrado
   - Botón "Crear Viaje" prominente
   - Grid de 3 columnas para tarjetas de viajes
   - Formulario modal

2. **Vista Tablet** (768px - 1024px)
   - Layout similar a desktop
   - Grid de 2 columnas

3. **Vista Mobile** (< 768px)
   - Layout vertical
   - Grid de 1 columna
   - Formulario a pantalla completa
   - Botones apilados

## Notas de Implementación

- ✅ **HTML**: Semántico con etiquetas apropiadas
- ✅ **CSS**: Flexbox y Grid, medidas relativas, sin px absolutos
- ✅ **JavaScript**: Fetch con promesas, manejo de errores con then/catch
- ✅ **Backend**: MVC con DAOs y Servlets aislados por operación
- ✅ **Base de datos**: MySQL con script de inicialización
- ✅ **Docker**: Orquestación con docker-compose
- ✅ **CRUD**: Completo (Create, Read, Update, Delete)
