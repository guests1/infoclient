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

    private static float wA=0, aA=0, sA=0, dA=0, lA=0, rA=0;
    private static final float SP   = 0.25f;
    private static final int   BOX  = 22;
    private static final int   GAP  = 3;
    private static final int   STEP = BOX + GAP;

    public static void render(MatrixStack matrices, float tickDelta) {
        if (!ModConfig.showKeyStrokes) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int ox = sw - STEP * 3 - 6;
        int oy = sh - BOX  * 3 - GAP * 2 - 54;

        boolean w = client.options.forwardKey.isPressed();
        boolean a = client.options.leftKey.isPressed();
        boolean s = client.options.backKey.isPressed();
        boolean d = client.options.rightKey.isPressed();
        boolean l = client.options.attackKey.isPressed();
        boolean r = client.options.useKey.isPressed();

        wA = lerp(wA, w?1:0, SP);  aA = lerp(aA, a?1:0, SP);
        sA = lerp(sA, s?1:0, SP);  dA = lerp(dA, d?1:0, SP);
        lA = lerp(lA, l?1:0, SP);  rA = lerp(rA, r?1:0, SP);

        DrawableHelper.fill(matrices, ox-4, oy-4, ox+STEP*3+4, oy+STEP*3-GAP+4, 0xAA0d0d1a);

        drawKey(matrices, client, ox+STEP,   oy,      "W", wA);
        drawKey(matrices, client, ox,         oy+STEP, "A", aA);
        drawKey(matrices, client, ox+STEP,   oy+STEP, "S", sA);
        drawKey(matrices, client, ox+STEP*2, oy+STEP, "D", dA);

        int my = oy + STEP * 2;
        int hw = STEP + BOX / 2;
        drawMouse(matrices, client, ox,    my, hw-GAP, "LMB", lA, l, false);
        drawMouse(matrices, client, ox+hw, my, hw-GAP, "RMB", rA, r, true);
    }

    private static void drawKey(MatrixStack matrices, MinecraftClient client,
                                 int x, int y, String label, float alpha) {
        int r  = (int)(0x10 + alpha * (0x00 - 0x10));
        int g  = (int)(0x10 + alpha * (0xE0 - 0x10));
        int b  = (int)(0x20 + alpha * (0x70 - 0x20));
        int bg = 0xCC000000 | (r << 16) | (g << 8) | b;
        int br = (int)(0x33 + alpha * 0xCC);
        int border = 0xFF000000 | (br << 8) | (br / 2);

        DrawableHelper.fill(matrices, x-1, y-1, x+BOX+1, y+BOX+1, border);
        DrawableHelper.fill(matrices, x,   y,   x+BOX,   y+BOX,   bg);

        int tw = client.textRenderer.getWidth(label);
        client.textRenderer.draw(matrices, label,
                x + (BOX - tw) / 2f, y + (BOX - 8) / 2f,
                alpha > 0.5f ? 0xFF111111 : 0xFFCCCCCC);
    }

    private static void drawMouse(MatrixStack matrices, MinecraftClient client,
                                   int x, int y, int width, String label,
                                   float alpha, boolean pressed, boolean right) {
        int r  = (int)(0x10 + alpha * (right ? 0xE0 : 0x00));
        int g  = (int)(0x10 + alpha * (right ? 0x50 : 0xE0));
        int b  = (int)(0x20 + alpha * (right ? 0x00 : 0x70));
        int bg = 0xCC000000 | (r << 16) | (g << 8) | b;
        int br = (int)(0x33 + alpha * 0xCC);
        int border = right
                ? 0xFF000000 | (br << 16) | (br / 4 << 8)
                : 0xFF000000 | (br << 8)  | (br / 2);

        DrawableHelper.fill(matrices, x-1, y-1, x+width+1, y+BOX+1, border);
        DrawableHelper.fill(matrices, x,   y,   x+width,   y+BOX,   bg);

        String display = (!right && ModConfig.autoClicker && pressed)
                ? AutoClicker.realCps + " CPS" : label;
        int tw = client.textRenderer.getWidth(display);
        client.textRenderer.draw(matrices, display,
                x + (width - tw) / 2f, y + (BOX - 8) / 2f,
                alpha > 0.5f ? 0xFF111111 : 0xFFAAAAAA);
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
}
