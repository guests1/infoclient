package net.infoclient.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.infoclient.config.ModConfig;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SettingsScreen extends Screen {

    private final Screen parent;

    // Dimensiones del panel central
    private static final int PANEL_W = 300;
    private static final int PANEL_H = 290;

    // Elementos dinámicos que actualizan su texto
    private ButtonWidget hudBtn;
    private ButtonWidget keystrokesBtn;
    private ButtonWidget autoClickBtn;
    private ButtonWidget cpsLabel;   // label no-clickable que muestra el CPS actual
    private ButtonWidget cpsMinusBtn;
    private ButtonWidget cpsPlusBtn;

    public SettingsScreen(Screen parent) {
        super(Text.literal("InfoClient"));
        this.parent = parent;
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        int panelX = (this.width  - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;

        int col    = panelX + PANEL_W / 2;   // centro horizontal del panel
        int bw     = 220;                      // ancho de botón normal
        int bh     = 22;                       // alto de botón
        int startY = panelY + 60;
        int gap    = 30;

        // ── Toggle HUD ──────────────────────────────────────────────────────
        hudBtn = new ButtonWidget(
                col - bw / 2, startY, bw, bh,
                buildToggleText("§b⬡  HUD Principal", ModConfig.showHud),
                btn -> {
                    ModConfig.showHud = !ModConfig.showHud;
                    btn.setMessage(buildToggleText("§b⬡  HUD Principal", ModConfig.showHud));
                }
        );
        this.addDrawableChild(hudBtn);

        // ── Toggle KeyStrokes ────────────────────────────────────────────────
        keystrokesBtn = new ButtonWidget(
                col - bw / 2, startY + gap, bw, bh,
                buildToggleText("§d⌨  KeyStrokes WASD", ModConfig.showKeyStrokes),
                btn -> {
                    ModConfig.showKeyStrokes = !ModConfig.showKeyStrokes;
                    btn.setMessage(buildToggleText("§d⌨  KeyStrokes WASD", ModConfig.showKeyStrokes));
                }
        );
        this.addDrawableChild(keystrokesBtn);

        // ── Toggle AutoClicker ───────────────────────────────────────────────
        autoClickBtn = new ButtonWidget(
                col - bw / 2, startY + gap * 2, bw, bh,
                buildToggleText("§e⚡  AutoClicker", ModConfig.autoClicker),
                btn -> {
                    ModConfig.autoClicker = !ModConfig.autoClicker;
                    btn.setMessage(buildToggleText("§e⚡  AutoClicker", ModConfig.autoClicker));
                }
        );
        this.addDrawableChild(autoClickBtn);

        // ── CPS control ──────────────────────────────────────────────────────
        int cpsY    = startY + gap * 3;
        int minusW  = 32;
        int plusW   = 32;
        int labelW  = bw - minusW - plusW - 4;

        cpsMinusBtn = new ButtonWidget(
                col - bw / 2, cpsY, minusW, bh,
                Text.literal("§c ─"),
                btn -> {
                    ModConfig.autoClickerCps = Math.max(1, ModConfig.autoClickerCps - 1);
                    refreshCpsLabel();
                }
        );
        this.addDrawableChild(cpsMinusBtn);

        cpsLabel = new ButtonWidget(
                col - bw / 2 + minusW + 2, cpsY, labelW, bh,
                buildCpsText(),
                btn -> {}   // no hace nada al hacer clic
        );
        cpsLabel.active = false;   // no se puede clickear, es solo display
        this.addDrawableChild(cpsLabel);

        cpsPlusBtn = new ButtonWidget(
                col - bw / 2 + minusW + 2 + labelW + 2, cpsY, plusW, bh,
                Text.literal("§a ＋"),
                btn -> {
                    ModConfig.autoClickerCps = Math.min(20, ModConfig.autoClickerCps + 1);
                    refreshCpsLabel();
                }
        );
        this.addDrawableChild(cpsPlusBtn);

        // ── Botón cerrar ─────────────────────────────────────────────────────
        this.addDrawableChild(new ButtonWidget(
                col - 60, panelY + PANEL_H - 36, 120, 22,
                Text.literal("§7✖  Cerrar"),
                btn -> this.close()
        ));
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Fondo oscuro estándar de Minecraft
        this.renderBackground(matrices);

        int panelX = (this.width  - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;

        // ── Sombra del panel ────────────────────────────────────────────────
        DrawableHelper.fill(matrices,
                panelX + 4, panelY + 4,
                panelX + PANEL_W + 4, panelY + PANEL_H + 4,
                0x66000000);

        // ── Panel principal ─────────────────────────────────────────────────
        DrawableHelper.fill(matrices,
                panelX, panelY,
                panelX + PANEL_W, panelY + PANEL_H,
                0xEE111122);

        // ── Borde izquierdo + derecho sutiles ───────────────────────────────
        DrawableHelper.fill(matrices, panelX,               panelY, panelX + 1,               panelY + PANEL_H, 0xFF33335A);
        DrawableHelper.fill(matrices, panelX + PANEL_W - 1, panelY, panelX + PANEL_W,          panelY + PANEL_H, 0xFF33335A);
        DrawableHelper.fill(matrices, panelX, panelY + PANEL_H - 1, panelX + PANEL_W, panelY + PANEL_H, 0xFF33335A);

        // ── Barra superior degradada (verde → cyan) ──────────────────────────
        int headerH = 40;
        // Simular degradado con varias franjas
        for (int i = 0; i < PANEL_W; i++) {
            float t   = (float) i / PANEL_W;
            int   r   = (int)(0x00 + t * (0x00 - 0x00));
            int   g   = (int)(0xCC + t * (0xAA - 0xCC));
            int   b   = (int)(0x88 + t * (0xFF - 0x88));
            int   col = 0xFF000000 | (r << 16) | (g << 8) | b;
            DrawableHelper.fill(matrices, panelX + i, panelY, panelX + i + 1, panelY + headerH, col);
        }

        // ── Línea divisoria bajo el header ──────────────────────────────────
        DrawableHelper.fill(matrices, panelX, panelY + headerH, panelX + PANEL_W, panelY + headerH + 1, 0xFF00EEBB);

        // ── Título ──────────────────────────────────────────────────────────
        drawCenteredText(matrices, this.textRenderer,
                "§0§l✦  InfoClient", this.width / 2, panelY + 10, 0xFFFFFF);
        drawCenteredText(matrices, this.textRenderer,
                "§0Configuración  |  §0§oPresa P para cerrar",
                this.width / 2, panelY + 24, 0xFFFFFF);

        // ── Sección: Features ────────────────────────────────────────────────
        drawCenteredText(matrices, this.textRenderer,
                "§7— Features —",
                this.width / 2, panelY + 48, 0xFFFFFF);

        // ── Sección: AutoClicker CPS ─────────────────────────────────────────
        int cpsLabelY = (this.height - PANEL_H) / 2 + 60 + 30 * 3 - 11;
        drawCenteredText(matrices, this.textRenderer,
                "§7Clicks Por Segundo",
                this.width / 2, cpsLabelY, 0xFFFFFF);

        // ── Versión ──────────────────────────────────────────────────────────
        this.textRenderer.drawWithShadow(matrices, "§8v1.0 · 1.18.2",
                panelX + 6, panelY + PANEL_H - 11, 0xFFFFFF);

        // ── Widgets ──────────────────────────────────────────────────────────
        super.render(matrices, mouseX, mouseY, delta);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void refreshCpsLabel() {
        cpsLabel.setMessage(buildCpsText());
    }

    private static Text buildToggleText(String name, boolean enabled) {
        String state = enabled ? "§a[ON]" : "§c[OFF]";
        return Text.literal(name + "  " + state);
    }

    private static Text buildCpsText() {
        return Text.literal("§f" + ModConfig.autoClickerCps + " §7CPS");
    }

    @Override
    public boolean shouldPause() {
        return false;   // el juego no se pausa al abrir el menú
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
