package net.infoclient.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.infoclient.config.ModConfig;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SettingsScreen extends Screen {

    private final Screen parent;
    private static final int PANEL_W = 300, PANEL_H = 290;
    private ButtonWidget cpsLabel;

    public SettingsScreen(Screen parent) {
        super(new LiteralText("InfoClient"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelX = (this.width - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;
        int col = panelX + PANEL_W / 2;
        int bw = 220, bh = 22, startY = panelY + 60, gap = 30;

        this.addDrawableChild(new ButtonWidget(col-bw/2, startY, bw, bh,
            buildToggle("§b  HUD Principal", ModConfig.showHud), btn -> {
                ModConfig.showHud = !ModConfig.showHud;
                btn.setMessage(buildToggle("§b  HUD Principal", ModConfig.showHud));
            }));

        this.addDrawableChild(new ButtonWidget(col-bw/2, startY+gap, bw, bh,
            buildToggle("§d  KeyStrokes WASD", ModConfig.showKeyStrokes), btn -> {
                ModConfig.showKeyStrokes = !ModConfig.showKeyStrokes;
                btn.setMessage(buildToggle("§d  KeyStrokes WASD", ModConfig.showKeyStrokes));
            }));

        this.addDrawableChild(new ButtonWidget(col-bw/2, startY+gap*2, bw, bh,
            buildToggle("§e  AutoClicker", ModConfig.autoClicker), btn -> {
                ModConfig.autoClicker = !ModConfig.autoClicker;
                btn.setMessage(buildToggle("§e  AutoClicker", ModConfig.autoClicker));
            }));

        int cpsY = startY + gap * 3;
        int mw = 32, lw = bw - 68;

        this.addDrawableChild(new ButtonWidget(col-bw/2, cpsY, mw, bh,
            new LiteralText("§c -"), btn -> {
                ModConfig.autoClickerCps = Math.max(1, ModConfig.autoClickerCps - 1);
                cpsLabel.setMessage(buildCps());
            }));

        cpsLabel = new ButtonWidget(col-bw/2+mw+2, cpsY, lw, bh, buildCps(), btn -> {});
        cpsLabel.active = false;
        this.addDrawableChild(cpsLabel);

        this.addDrawableChild(new ButtonWidget(col-bw/2+mw+lw+4, cpsY, mw, bh,
            new LiteralText("§a +"), btn -> {
                ModConfig.autoClickerCps = Math.min(20, ModConfig.autoClickerCps + 1);
                cpsLabel.setMessage(buildCps());
            }));

        this.addDrawableChild(new ButtonWidget(col-60, panelY+PANEL_H-36, 120, 22,
            new LiteralText("§7  Cerrar"), btn -> this.close()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        int px = (this.width - PANEL_W) / 2, py = (this.height - PANEL_H) / 2;

        DrawableHelper.fill(matrices, px+4, py+4, px+PANEL_W+4, py+PANEL_H+4, 0x66000000);
        DrawableHelper.fill(matrices, px, py, px+PANEL_W, py+PANEL_H, 0xEE111122);
        DrawableHelper.fill(matrices, px, py, px+1, py+PANEL_H, 0xFF33335A);
        DrawableHelper.fill(matrices, px+PANEL_W-1, py, px+PANEL_W, py+PANEL_H, 0xFF33335A);
        DrawableHelper.fill(matrices, px, py+PANEL_H-1, px+PANEL_W, py+PANEL_H, 0xFF33335A);

        for (int i = 0; i < PANEL_W; i++) {
            float t = (float)i / PANEL_W;
            int g = (int)(0xCC + t*(0xAA-0xCC));
            int b = (int)(0x88 + t*(0xFF-0x88));
            DrawableHelper.fill(matrices, px+i, py, px+i+1, py+40, 0xFF000000|(g<<8)|b);
        }
        DrawableHelper.fill(matrices, px, py+40, px+PANEL_W, py+41, 0xFF00EEBB);

        drawCenteredText(matrices, this.textRenderer, "§0§l  InfoClient",
            this.width/2, py+10, 0xFFFFFF);
        drawCenteredText(matrices, this.textRenderer, "§0Configuracion  |  §0§oPresa P para cerrar",
            this.width/2, py+24, 0xFFFFFF);
        drawCenteredText(matrices, this.textRenderer, "§7— Features —",
            this.width/2, py+48, 0xFFFFFF);

        int cpsLabelY = py + 60 + 30*3 - 11;
        drawCenteredText(matrices, this.textRenderer, "§7Clicks Por Segundo",
            this.width/2, cpsLabelY, 0xFFFFFF);

        this.textRenderer.drawWithShadow(matrices, "§8v1.0 · 1.18.2", px+6, py+PANEL_H-11, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private static Text buildToggle(String name, boolean on) {
        return new LiteralText(name + "  " + (on ? "§a[ON]" : "§c[OFF]"));
    }

    private static Text buildCps() {
        return new LiteralText("§f" + ModConfig.autoClickerCps + " §7CPS");
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void close() { this.client.setScreen(this.parent); }
}
