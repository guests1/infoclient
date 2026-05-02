package net.infoclient.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.infoclient.config.ModConfig;
import net.infoclient.feature.AutoClicker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class KeyStrokesHud {

    // Animación suave al presionar (fade in/out)
    private static float wAlpha   = 0, aAlpha  = 0, sAlpha  = 0, dAlpha  = 0;
    private static float lmbAlpha = 0, rmbAlpha = 0;
    private static final float SPEED = 0.25f;

    private static final int BOX   = 22;  // tamaño del cuadro en px
    private static final int GAP   =  3;  // espacio entre cuadros
    private static final int STEP  = BOX + GAP;

    public static void render(MatrixStack matrices, float tickDelta) {
        if (!ModConfig.showKeyStrokes) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();

        // Posición: esquina inferior derecha, encima del hotbar
        int originX = sw  - STEP * 3 - 6;
        int originY = sh  - BOX  * 3 - GAP * 2 - 54;

        // Estado de las teclas
        boolean w   = client.options.forwardKey.isPressed();
        boolean a   = client.options.leftKey.isPressed();
        boolean s   = client.options.backKey.isPressed();
        boolean d   = client.options.rightKey.isPressed();
        boolean lmb = client.options.attackKey.isPressed();
        boolean rmb = client.options.useKey.isPressed();

        // Actualizar animaciones
        wAlpha   = lerp(wAlpha,   w   ? 1f : 0f, SPEED);
        aAlpha   = lerp(aAlpha,   a   ? 1f : 0f, SPEED);
        sAlpha   = lerp(sAlpha,   s   ? 1f : 0f, SPEED);
        dAlpha   = lerp(dAlpha,   d   ? 1f : 0f, SPEED);
        lmbAlpha = lerp(lmbAlpha, lmb ? 1f : 0f, SPEED);
        rmbAlpha = lerp(rmbAlpha, rmb ? 1f : 0f, SPEED);

        /*
         *   Layout:
         *       [ W ]
         *   [A] [S] [D]
         *   [LMB]  [RMB]
         */

        // Fondo del panel completo (3 columnas × 3 filas)
        int px = originX - 4;
        int py = originY - 4;
        int pw = STEP * 3 + 4;
        int ph = STEP * 3 - GAP + 4;
        DrawableHelper.fill(matrices, px, py, px + pw, py + ph, 0xAA0d0d1a);

        // Fila 1 — W centrado
        drawKey(matrices, client, originX + STEP,       originY,          "W",   wAlpha);

        // Fila 2 — A S D
        drawKey(matrices, client, originX,              originY + STEP,   "A",   aAlpha);
        drawKey(matrices, client, originX + STEP,       originY + STEP,   "S",   sAlpha);
        drawKey(matrices, client, originX + STEP * 2,   originY + STEP,   "D",   dAlpha);

        // Fila 3 — LMB RMB (doble ancho cada uno)
        int mouseY = originY + STEP * 2;
        int halfW  = STEP + BOX / 2;  // ancho de cada botón de ratón
        drawMouseBtn(matrices, client, originX,          mouseY, halfW - GAP, "LMB", lmbAlpha, lmb, false);
        drawMouseBtn(matrices, client, originX + halfW,  mouseY, halfW - GAP, "RMB", rmbAlpha, rmb, true);
    }

    // ── Dibuja una tecla de movimiento ───────────────────────────────────────

    private static void drawKey(MatrixStack matrices, MinecraftClient client,
                                 int x, int y, String label, float alpha) {
        // Color de fondo interpolado
        int r = (int)(0x10 + alpha * (0x00 - 0x10));
        int g = (int)(0x10 + alpha * (0xE0 - 0x10));
        int b = (int)(0x20 + alpha * (0x70 - 0x20));
        int bg = 0xCC000000 | (r << 16) | (g << 8) | b;

        // Borde (más brillante cuando se presiona)
        int borderBrightness = (int)(0x33 + alpha * (0xCC));
        int borderColor = 0xFF000000 | (0 << 16) | (borderBrightness << 8) | (borderBrightness / 2);

        // Borde exterior
        DrawableHelper.fill(matrices, x - 1, y - 1, x + BOX + 1, y + BOX + 1, borderColor);
        // Fondo
        DrawableHelper.fill(matrices, x, y, x + BOX, y + BOX, bg);

        // Texto centrado
        int tw = client.textRenderer.getWidth(label);
        int tx = x + (BOX - tw) / 2;
        int ty = y + (BOX - 8) / 2;
        int textColor = alpha > 0.5f ? 0xFF111111 : 0xFFCCCCCC;
        client.textRenderer.draw(matrices, label, tx, ty, textColor);
    }

    // ── Dibuja un botón de ratón ─────────────────────────────────────────────

    private static void drawMouseBtn(MatrixStack matrices, MinecraftClient client,
                                      int x, int y, int width, String label,
                                      float alpha, boolean pressed, boolean isRight) {
        int r  = (int)(0x10 + alpha * (isRight ? 0xE0 : 0x00));
        int g  = (int)(0x10 + alpha * (isRight ? 0x50 : 0xE0));
        int b  = (int)(0x20 + alpha * (isRight ? 0x00 : 0x70));
        int bg = 0xCC000000 | (r << 16) | (g << 8) | b;

        int br = (int)(0x33 + alpha * 0xCC);
        int borderColor = isRight
                ? 0xFF000000 | (br << 16) | (br / 4 << 8) | 0
                : 0xFF000000 | 0 | (br << 8) | (br / 2);

        DrawableHelper.fill(matrices, x - 1, y - 1, x + width + 1, y + BOX + 1, borderColor);
        DrawableHelper.fill(matrices, x,     y,     x + width,     y + BOX,     bg);

        // Etiqueta + CPS real si autoclicker activo y LMB
        String display = label;
        if (!isRight && ModConfig.autoClicker && pressed) {
            display = AutoClicker.realCps + " CPS";
        }

        int tw = client.textRenderer.getWidth(display);
        int tx = x + (width - tw) / 2;
        int ty = y + (BOX - 8) / 2;
        int textColor = alpha > 0.5f ? 0xFF111111 : 0xFFAAAAAA;
        client.textRenderer.draw(matrices, display, tx, ty, textColor);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
