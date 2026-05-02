# InfoClient — Mod Fabric 1.18.2

Mod de cliente con HUD, visualizador de teclas, autoclick y menu de ajustes.

## Teclas
| Tecla | Accion |
|-------|--------|
| **P**  | Abrir/cerrar menu de configuracion |
| **F4** | Mostrar/ocultar todo el HUD       |

## Caracteristicas
- **HUD** — FPS (color segun rendimiento), XYZ, direccion, bioma, luz, hora, dimension, velocidad
- **Item info** — nombre + durabilidad + cantidad del item en mano, centrado encima del hotbar
- **KeyStrokes** — display animado de W/A/S/D + LMB/RMB en esquina inferior derecha
- **AutoClicker** — click automatico solo mientras mantienes el boton de ataque (configurable 1-20 CPS)
- **Menu P** — activa/desactiva cada feature y ajusta el CPS

## Como compilar

### Requisitos
- Java 17+  →  https://adoptium.net
- Gradle wrapper incluido (descarga automaticamente Gradle 7.4.2)

### Pasos

```bash
# Linux / macOS
chmod +x gradlew
./gradlew build

# Windows
gradlew.bat build
```

El .jar queda en:  build/libs/infoclient-1.0.0.jar

Copialo a .minecraft/mods/ junto con:
- Fabric Loader >= 0.14.9
- Fabric API 0.58.5+1.18.2

> Nota: el AutoClicker puede estar prohibido en algunos servidores. Usalo solo donde este permitido.
