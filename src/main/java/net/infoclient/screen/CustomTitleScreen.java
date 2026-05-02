package net.infoclient.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class CustomTitleScreen extends Screen {

    private static final int PARTICLE_COUNT = 60;
    private final float[] px = new float[PARTICLE_COUNT];
    private final float[] py = new float[PARTICLE_COUNT];
    private final float[] pvx = new float[PARTICLE_COUNT];
    private final float[] pvy = new float[PARTICLE_COUNT];
    private final float[] psize = new float[PARTICLE_COUNT];
    private final float[] palpha = new float[PARTICLE_COUNT];
    private final int[]   pcolor = new int[PARTICLE_COUNT];

    private long tickCount = 0;
    private float logoAnim = 0f;
    private float btnAnim  = 0f;

    private static final String[] SPLASHES = {
        "Sin hacks, puro estilo!",
        "FPS siempre en verde ;)",
        "Tecla P para la magia",
        "KeyStrokes activados",
        "1.18.2 forever",
        "Coordenadas al instante",
        "Fabric es vida",
        "InfoClient v1.0"
    };
    private final String splash;
    private float splashPulse = 0f;
    private final List<StyledButton> styledButtons = new ArrayList<>();

    public CustomTitleScreen() {
        super(new LiteralText("InfoClient"));
        Random rng = new Random();
        this.splash = SPLASHES[rng.nextInt(SPLASHES.length)];
        for (int i = 0; i < PARTICLE_COUNT; i++) resetParticle(i, rng, true);
    }

    private void resetParticle(int i, Random rng, boolean anyY) {
        px[i]    = rng.nextFloat() * 2000;
        py[i]    = anyY ? rng.nextFloat() * 1000 : 1050f;
        float speed = 0.3f + rng.nextFloat() * 0.7f;
        float angle = rng.nextFloat() * (float)Math.PI * 2;
        pvx[i]   = (float)Math.cos(angle) * speed * 0.4f;
        pvy[i]   = -(0.4f + rng.nextFloat() * 0.6f) * speed;
        psize[i] = 1.5f + rng.nextFloat() * 2.5f;
        palpha[i]= 0.3f + rng.nextFloat() * 0.5f;
        pcolor[i]= rng.nextInt(3);
    }

    @Override
    protected void init() {
        styledButtons.clear();
        int cx = this.width / 2;
        int by = this.height / 2 + 20;
        int bw = 200, bh = 24, gap = 6;

        styledButtons.add(new StyledButton(cx-bw/2, by,            bw, bh, "  Un jugador",     0, () -> client.setScreen(new SelectWorldScreen(this))));
        styledButtons.add(new StyledButton(cx-bw/2, by+bh+gap,     bw, bh, "  Multi jugador",   1, () -> client.setScreen(new MultiplayerScreen(this))));
        styledButtons.add(new StyledButton(cx-bw/2, by+(bh+gap)*2, bw, bh, "  Opciones",        2, () -> client.setScreen(new OptionsScreen(this, client.options))));
        styledButtons.add(new StyledButton(cx-bw/2, by+(bh+gap)*3, bw, bh, "  Salir del juego", 3, () -> client.scheduleStop()));

        for (StyledButton sb : styledButtons) {
            this.addDrawableChild(new ButtonWidget(sb.x, sb.y, sb.w, sb.h,
                new LiteralText(""), btn -> sb.action.run()) {
                @Override public void renderButton(MatrixStack m, int mx, int my, float d) {}
            });
        }
    }

    @Override
    public void tick() {
        tickCount++;
        logoAnim = Math.min(1f, logoAnim + 0.04f);
        btnAnim  = Math.min(1f, btnAnim  + 0.025f);
        splashPulse = (float)(Math.sin(tickCount * 0.08f) * 0.5f + 0.5f);

        Random rng = new Random();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float realX = px[i] / 2000f * this.width;
            float realY = py[i] / 1000f * this.height;
            px[i] += pvx[i] * (this.width  / 2000f) * 3f;
            py[i] += pvy[i] * (this.height / 1000f) * 3f;
            if (realX < -10 || realX > this.width + 10 || realY < -10)
                resetParticle(i, rng, false);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int w = this.width, h = this.height, cx = w / 2;

        fillGradientV(matrices, 0, 0, w, h, 0xFF050510, 0xFF0D0D25);
        renderParticles(matrices);
        renderLogo(matrices, cx, h);

        float pulse = 0.9f + splashPulse * 0.12f;
        String splashTxt = "* " + splash + " *";
        matrices.push();
        matrices.translate(cx, h/2 - 20 + 4, 0);
        matrices.scale(pulse, pulse, 1f);
        int sw2 = this.textRenderer.getWidth(splashTxt);
        this.textRenderer.drawWithShadow(matrices, "§e" + splashTxt, -sw2/2f, -4, 0xFFFF44);
        matrices.pop();

        renderStyledButtons(matrices, mouseX, mouseY);

        String footer = "§8InfoClient v1.0  |  Fabric 1.18.2  |  §7Presiona §bP §7en juego";
        int fw = this.textRenderer.getWidth(footer);
        this.textRenderer.drawWithShadow(matrices, footer, (w-fw)/2f, h-12f, 0xFFFFFF);
        this.textRenderer.drawWithShadow(matrices, "§8Minecraft 1.18.2", 4, h-12, 0xFFFFFF);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderLogo(MatrixStack matrices, int cx, int h) {
        float ease = easeOutBack(logoAnim);
        int baseY = (int)(h/2 - 80 - (1f-ease)*40);
        String title = "InfoClient";
        float scale = 3.5f;
        int tw = (int)(this.textRenderer.getWidth(title) * scale);

        matrices.push();
        matrices.translate(cx - tw/2f, baseY, 0);
        matrices.scale(scale, scale, 1f);
        this.textRenderer.draw(matrices, "§0"+title, 2, 2, 0x44000000);
        this.textRenderer.draw(matrices, "§f"+title, 0, 0, 0xFFFFFF);
        matrices.pop();

        String sub = "HUD  *  KeyStrokes  *  AutoClicker";
        int subW = this.textRenderer.getWidth(sub);
        this.textRenderer.drawWithShadow(matrices, "§b"+sub, cx-subW/2f, baseY+38, 0xFFFFFF);

        int lineY = baseY + 50;
        DrawableHelper.fill(matrices, cx-80, lineY, cx+80, lineY+1, 0xFF00EEBB);
        DrawableHelper.fill(matrices, cx-30, lineY-1, cx+30, lineY+2, 0x6600FFCC);
    }

    private void renderStyledButtons(MatrixStack matrices, int mouseX, int mouseY) {
        int[] colors = {0xFF00DDAA, 0xFF0099FF, 0xFFAA44FF, 0xFFFF4444};

        for (int i = 0; i < styledButtons.size(); i++) {
            StyledButton sb = styledButtons.get(i);
            float lp = Math.max(0, btnAnim - i*0.08f) / Math.max(0.001f, 1f-i*0.05f);
            float le = MathHelper.clamp(easeOutBack(lp), 0f, 1f);
            int offsetX = (int)((1f-le)*80);
            boolean hovered = mouseX>=sb.x && mouseX<=sb.x+sb.w && mouseY>=sb.y && mouseY<=sb.y+sb.h;

            int accent = colors[i];
            int r=(accent>>16&0xFF), g=(accent>>8&0xFF), b=(accent&0xFF);
            int bgAlpha = hovered ? 0xDD : 0x99;
            int bg = (bgAlpha<<24)|((r/6)<<16)|((g/6)<<8)|(b/6);
            int rx = sb.x + offsetX;

            DrawableHelper.fill(matrices, rx-1, sb.y-1, rx+sb.w+1, sb.y+sb.h+1,
                hovered ? accent|0xFF000000 : (accent&0x00FFFFFF)|0x88000000);
            DrawableHelper.fill(matrices, rx, sb.y, rx+sb.w, sb.y+sb.h, bg);
            DrawableHelper.fill(matrices, rx, sb.y, rx+3, sb.y+sb.h, accent|0xFF000000);
            if (hovered) DrawableHelper.fill(matrices, rx, sb.y, rx+sb.w, sb.y+1, 0x66FFFFFF);

            String label = hovered ? "§f"+sb.label : "§7"+sb.label;
            int lw = this.textRenderer.getWidth(label);
            this.textRenderer.drawWithShadow(matrices, label, rx+(sb.w-lw)/2f, sb.y+(sb.h-8)/2f, 0xFFFFFF);
        }
    }

    private void renderParticles(MatrixStack matrices) {
        int[][] cols = {{0x00,0xFF,0xBB},{0x00,0xEE,0x44},{0xAA,0x44,0xFF}};
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float rx = px[i]/2000f*this.width, ry = py[i]/1000f*this.height;
            int ci = pcolor[i], a = (int)(palpha[i]*255);
            int color = (a<<24)|(cols[ci][0]<<16)|(cols[ci][1]<<8)|cols[ci][2];
            int s = (int)psize[i];
            DrawableHelper.fill(matrices,(int)rx,(int)ry,(int)rx+s,(int)ry+s,color);
        }
    }

    private void fillGradientV(MatrixStack matrices, int x1, int y1, int x2, int y2, int cTop, int cBot) {
        int steps = 32;
        for (int i = 0; i < steps; i++) {
            float t0=(float)i/steps, t1=(float)(i+1)/steps;
            int c = lerpColor(cTop, cBot, (t0+t1)/2f);
            DrawableHelper.fill(matrices, x1, y1+(int)(t0*(y2-y1)), x2, y1+(int)(t1*(y2-y1)), c);
        }
    }

    private static int lerpColor(int a, int b, float t) {
        int ar=(a>>16&0xFF),ag=(a>>8&0xFF),ab=(a&0xFF),aa=(a>>24&0xFF);
        int br=(b>>16&0xFF),bg=(b>>8&0xFF),bb=(b&0xFF),ba=(b>>24&0xFF);
        return ((int)(aa+(ba-aa)*t)<<24)|((int)(ar+(br-ar)*t)<<16)
              |((int)(ag+(bg-ag)*t)<<8)|((int)(ab+(bb-ab)*t));
    }

    private static float easeOutBack(float t) {
        float c1=1.70158f, c3=c1+1f;
        t = MathHelper.clamp(t,0f,1f);
        return 1f+c3*(float)Math.pow(t-1,3)+c1*(float)Math.pow(t-1,2);
    }

    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public boolean shouldPause() { return false; }

    private static class StyledButton {
        final int x,y,w,h,index; final String label; final Runnable action;
        StyledButton(int x,int y,int w,int h,String label,int index,Runnable action){
            this.x=x;this.y=y;this.w=w;this.h=h;this.label=label;this.index=index;this.action=action;
        }
    }
}
