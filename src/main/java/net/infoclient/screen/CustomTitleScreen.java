package net.infoclient.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.infoclient.config.ModConfig;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class CustomTitleScreen extends Screen {

    // ── Particulas de fondo ───────────────────────────────────────────────────
    private static final int PARTICLE_COUNT = 60;
    private final float[] px  = new float[PARTICLE_COUNT];
    private final float[] py  = new float[PARTICLE_COUNT];
    private final float[] pvx = new float[PARTICLE_COUNT];
    private final float[] pvy = new float[PARTICLE_COUNT];
    private final float[] psize = new float[PARTICLE_COUNT];
    private final float[] palpha = new float[PARTICLE_COUNT];
    private final int[]   pcolor = new int[PARTICLE_COUNT];   // 0=cyan, 1=green, 2=purple

    private long tickCount = 0;
    private float logoAnim = 0f;   // 0->1 al entrar
    private float btnAnim  = 0f;

    // ── Splash texts ──────────────────────────────────────────────────────────
    private static final String[] SPLASHES = {
        "¡Sin hacks, puro estilo!",
        "FPS siempre en verde ;)",
        "Tecla P para la magia",
        "KeyStrokes activados",
        "1.18.2 forever",
        "AutoClicker legal*",
        "Coordenadas al instante",
        "Fabric es vida",
        "Hecho con café ☕",
        "InfoClient v1.0"
    };
    private final String splash;
    private float splashPulse = 0f;

    // ── Botones ───────────────────────────────────────────────────────────────
    private final List<StyledButton> styledButtons = new ArrayList<>();

    public CustomTitleScreen() {
        super(Text.literal("InfoClient"));
        Random rng = new Random();
        this.splash = SPLASHES[rng.nextInt(SPLASHES.length)];

        // Inicializar partículas
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            resetParticle(i, rng, true);
        }
    }

    private void resetParticle(int i, Random rng, boolean anyY) {
        px[i]    = rng.nextFloat() * 2000;          // X entre 0-2000 (se escala al width real)
        py[i]    = anyY ? rng.nextFloat() * 1000 : 1050f;
        float speed = 0.3f + rng.nextFloat() * 0.7f;
        float angle = rng.nextFloat() * (float)Math.PI * 2;
        pvx[i]   = (float)Math.cos(angle) * speed * 0.4f;
        pvy[i]   = -(0.4f + rng.nextFloat() * 0.6f) * speed;
        psize[i] = 1.5f + rng.nextFloat() * 2.5f;
        palpha[i]= 0.3f + rng.nextFloat() * 0.5f;
        pcolor[i]= rng.nextInt(3);
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        styledButtons.clear();

        int cx = this.width / 2;
        int by = this.height / 2 + 20;   // Y base de los botones
        int bw = 200;
        int bh = 24;
        int gap = 6;

        // Singleplayer
        styledButtons.add(new StyledButton(cx - bw/2, by,            bw, bh,
            "⚔  Un jugador", 0, () -> this.client.setScreen(new SelectWorldScreen(this))));
        // Multiplayer
        styledButtons.add(new StyledButton(cx - bw/2, by + bh+gap,   bw, bh,
            "🌐  Multi jugador", 1, () -> this.client.setScreen(new MultiplayerScreen(this))));
        // Opciones
        styledButtons.add(new StyledButton(cx - bw/2, by + (bh+gap)*2, bw, bh,
            "⚙  Opciones", 2, () -> this.client.setScreen(new OptionsScreen(this, this.client.options))));
        // Salir
        styledButtons.add(new StyledButton(cx - bw/2, by + (bh+gap)*3, bw, bh,
            "✖  Salir del juego", 3, () -> this.client.scheduleStop()));

        // Registrar como widgets de Minecraft (necesario para el click)
        for (StyledButton sb : styledButtons) {
            this.addDrawableChild(new ButtonWidget(sb.x, sb.y, sb.w, sb.h,
                Text.literal(""), btn -> sb.action.run()) {
                @Override public void renderButton(MatrixStack m, int mx, int my, float d) {}
            });
        }
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        tickCount++;
        logoAnim = Math.min(1f, logoAnim + 0.04f);
        btnAnim  = Math.min(1f, btnAnim  + 0.025f);
        splashPulse = (float)(Math.sin(tickCount * 0.08f) * 0.5f + 0.5f);

        Random rng = new Random();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // escalar coordenadas de 0-2000 → 0-width  y  0-1000 → 0-height
            float realX = px[i] / 2000f * this.width;
            float realY = py[i] / 1000f * this.height;
            px[i] += pvx[i] * (this.width  / 2000f) * 60f / 20f;
            py[i] += pvy[i] * (this.height / 1000f) * 60f / 20f;
            if (realX < -10 || realX > this.width + 10 || realY < -10) {
                resetParticle(i, rng, false);
            }
        }
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int w = this.width, h = this.height;
        int cx = w / 2;

        // 1. Fondo degradado vertical oscuro
        fillGradientV(matrices, 0, 0, w, h, 0xFF050510, 0xFF0D0D25);

        // 2. Partículas flotantes
        renderParticles(matrices);

        // 3. Línea central brillante (divisor izquierda/derecha)
        int lineX = cx;
        DrawableHelper.fill(matrices, lineX, 0, lineX + 1, h, 0x2200FFBB);

        // 4. Logo "InfoClient" animado
        renderLogo(matrices, cx, h);

        // 5. Splash text
        renderSplash(matrices, cx, h);

        // 6. Botones estilizados
        renderStyledButtons(matrices, mouseX, mouseY);

        // 7. Footer
        String footer = "§8InfoClient v1.0  |  Fabric 1.18.2  |  §7Presiona §bP §7para el menú en juego";
        int fw = this.textRenderer.getWidth(footer);
        this.textRenderer.drawWithShadow(matrices, footer, (w - fw) / 2f, h - 12f, 0xFFFFFF);

        // 8. Esquina: versión de MC
        this.textRenderer.drawWithShadow(matrices, "§8Minecraft 1.18.2", 4, h - 12, 0xFFFFFF);

        super.render(matrices, mouseX, mouseY, delta);
    }

    // ── Logo ─────────────────────────────────────────────────────────────────

    private void renderLogo(MatrixStack matrices, int cx, int h) {
        float ease = easeOutBack(logoAnim);
        int baseY  = (int)(h / 2 - 80 - (1f - ease) * 40);

        // Sombra del texto grande
        String title = "InfoClient";
        float scale  = 3.5f;
        int tw = (int)(this.textRenderer.getWidth(title) * scale);

        matrices.push();
        matrices.translate(cx - tw / 2f, baseY, 0);
        matrices.scale(scale, scale, 1f);

        // Sombra
        this.textRenderer.draw(matrices, "§0" + title, 2, 2, 0x44000000);
        // Texto principal en blanco
        this.textRenderer.draw(matrices, "§f" + title, 0, 0, 0xFFFFFF);
        matrices.pop();

        // Subtítulo con degradado simulado (verde → cyan)
        String sub = "Client Mod  ·  HUD  ·  KeyStrokes  ·  AutoClicker";
        int subW = this.textRenderer.getWidth(sub);
        this.textRenderer.drawWithShadow(matrices, "§b" + sub, cx - subW / 2f, baseY + 38, 0xFFFFFF);

        // Línea decorativa bajo el logo
        int lineY = baseY + 50;
        int lineLen = 160;
        // Línea principal cyan
        DrawableHelper.fill(matrices, cx - lineLen/2, lineY, cx + lineLen/2, lineY + 1, 0xFF00EEBB);
        // Brillo central
        DrawableHelper.fill(matrices, cx - 30, lineY - 1, cx + 30, lineY + 2, 0x6600FFCC);
    }

    // ── Splash ────────────────────────────────────────────────────────────────

    private void renderSplash(MatrixStack matrices, int cx, int h) {
        float pulse = 0.9f + splashPulse * 0.12f;
        int splashY = h / 2 - 20;
        String txt  = "✦ " + splash + " ✦";
        int tw = (int)(this.textRenderer.getWidth(txt) * pulse);

        matrices.push();
        matrices.translate(cx, splashY + 4, 0);
        matrices.scale(pulse, pulse, 1f);
        int sw = this.textRenderer.getWidth(txt);
        this.textRenderer.drawWithShadow(matrices, "§e" + txt, -sw / 2f, -4, 0xFFFF44);
        matrices.pop();
    }

    // ── Botones estilizados ───────────────────────────────────────────────────

    private void renderStyledButtons(MatrixStack matrices, int mouseX, int mouseY) {
        float ease = easeOutBack(btnAnim);

        for (int i = 0; i < styledButtons.size(); i++) {
            StyledButton sb = styledButtons.get(i);

            // Animación de entrada desde la derecha con delay por índice
            float localEase = MathHelper.clamp(
                easeOutBack(Math.max(0, btnAnim - i * 0.08f) / (1f - i * 0.05f)), 0f, 1f);
            int offsetX = (int)((1f - localEase) * 80);

            boolean hovered = mouseX >= sb.x && mouseX <= sb.x + sb.w
                           && mouseY >= sb.y && mouseY <= sb.y + sb.h;

            // Colores por botón
            int[] colors = {0xFF00DDAA, 0xFF0099FF, 0xFFAA44FF, 0xFFFF4444};
            int accent = colors[i];

            // Fondo botón
            int bgAlpha = hovered ? 0xDD : 0x99;
            int r = (accent >> 16 & 0xFF);
            int g = (accent >>  8 & 0xFF);
            int b = (accent       & 0xFF);
            int bg = (bgAlpha << 24) | ((r/6) << 16) | ((g/6) << 8) | (b/6);

            int rx = sb.x + offsetX;

            // Borde
            DrawableHelper.fill(matrices, rx-1,   sb.y-1, rx+sb.w+1, sb.y+sb.h+1, hovered ? accent | 0xFF000000 : (accent & 0x00FFFFFF) | 0x88000000);
            // Fondo
            DrawableHelper.fill(matrices, rx, sb.y, rx+sb.w, sb.y+sb.h, bg);
            // Línea izquierda de acento
            DrawableHelper.fill(matrices, rx, sb.y, rx+3, sb.y+sb.h, accent | 0xFF000000);

            // Brillo superior si hover
            if (hovered) {
                DrawableHelper.fill(matrices, rx, sb.y, rx+sb.w, sb.y+1, 0x66FFFFFF);
            }

            // Texto del botón
            int textColor = hovered ? 0xFFFFFF : 0xBBBBBB;
            String label = hovered ? "§f" + sb.label : "§7" + sb.label;
            int tw = this.textRenderer.getWidth(label);
            this.textRenderer.drawWithShadow(matrices, label,
                rx + (sb.w - tw) / 2f,
                sb.y + (sb.h - 8) / 2f,
                textColor);
        }
    }

    // ── Partículas ────────────────────────────────────────────────────────────

    private void renderParticles(MatrixStack matrices) {
        int[][] cols = {
            {0x00, 0xFF, 0xBB},   // cyan
            {0x00, 0xEE, 0x44},   // verde
            {0xAA, 0x44, 0xFF}    // morado
        };

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float rx = px[i] / 2000f * this.width;
            float ry = py[i] / 1000f * this.height;
            int   ci = pcolor[i];
            int   a  = (int)(palpha[i] * 255);
            int   color = (a << 24) | (cols[ci][0] << 16) | (cols[ci][1] << 8) | cols[ci][2];
            int   s  = (int)psize[i];
            DrawableHelper.fill(matrices, (int)rx, (int)ry, (int)rx+s, (int)ry+s, color);
        }
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private void fillGradientV(MatrixStack matrices, int x1, int y1, int x2, int y2,
                                int colorTop, int colorBot) {
        // Dividir en franjas para simular degradado
        int steps = 32;
        for (int i = 0; i < steps; i++) {
            float t0 = (float) i       / steps;
            float t1 = (float)(i + 1) / steps;
            int   c  = lerpColor(colorTop, colorBot, (t0 + t1) / 2f);
            int   sy = y1 + (int)(t0 * (y2 - y1));
            int   ey = y1 + (int)(t1 * (y2 - y1));
            DrawableHelper.fill(matrices, x1, sy, x2, ey, c);
        }
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a>>16&0xFF), ag = (a>>8&0xFF), ab = (a&0xFF), aa = (a>>24&0xFF);
        int br = (b>>16&0xFF), bg = (b>>8&0xFF), bb = (b&0xFF), ba = (b>>24&0xFF);
        int r = (int)(ar + (br-ar)*t);
        int g = (int)(ag + (bg-ag)*t);
        int bl2= (int)(ab + (bb-ab)*t);
        int al = (int)(aa + (ba-aa)*t);
        return (al<<24)|(r<<16)|(g<<8)|bl2;
    }

    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        t = MathHelper.clamp(t, 0f, 1f);
        return 1f + c3*(float)Math.pow(t-1,3) + c1*(float)Math.pow(t-1,2);
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    public boolean shouldPause() { return false; }

    // ── Record interno ────────────────────────────────────────────────────────

    private static class StyledButton {
        final int x, y, w, h;
        final String label;
        final int index;
        final Runnable action;

        StyledButton(int x, int y, int w, int h, String label, int index, Runnable action) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.label = label; this.index = index; this.action = action;
        }
    }
}
