package net.infoclient.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.infoclient.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class InfoHud {

    private static int   trackedFps  = 0;
    private static int   frameCount  = 0;
    private static long  lastFpsTime = System.currentTimeMillis();

    public static void render(MatrixStack matrices, float tickDelta) {
        if (!ModConfig.showHud) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (client.currentScreen != null) return;
        if (client.options.debugEnabled) return;

        frameCount++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime >= 1000) {
            trackedFps  = frameCount;
            frameCount  = 0;
            lastFpsTime = now;
        }

        renderInfoPanel(client, matrices);
        renderHeldItemInfo(client, matrices);
    }

    private static void renderInfoPanel(MinecraftClient client, MatrixStack matrices) {
        PlayerEntity player = client.player;
        BlockPos pos = player.getBlockPos();
        List<String> lines = new ArrayList<>();

        String fc = trackedFps >= 60 ? "§a" : trackedFps >= 30 ? "§e" : "§c";
        lines.add("§f§lInfoClient  §r" + fc + trackedFps + " §7FPS");
        lines.add("");
        lines.add(String.format("§7XYZ  §b%.1f §7/ §b%.1f §7/ §b%.1f",
                player.getX(), player.getY(), player.getZ()));

        Direction dir = player.getHorizontalFacing();
        lines.add("§7Dir  §d" + getFacing(player.getYaw())
                + " §8(" + dir.getName().toUpperCase() + ")");

        try {
            String biome = client.world.getBiome(pos).getKey()
                    .map(k -> capitalize(k.getValue().getPath().replace('_', ' ')))
                    .orElse("Desconocido");
            lines.add("§7Bioma  §2" + biome);
        } catch (Exception e) {
            lines.add("§7Bioma  §8N/A");
        }

        int bl = client.world.getLightLevel(LightType.BLOCK, pos);
        int sl = client.world.getLightLevel(LightType.SKY,   pos);
        String lc = bl >= 8 ? "§a" : bl >= 4 ? "§e" : "§c";
        lines.add("§7Luz  " + lc + bl + " §8bl  §3" + sl + " §8sky");

        long t = client.world.getTimeOfDay() % 24000;
        int hours = (int)((t / 1000 + 6) % 24);
        int mins  = (int)((t % 1000) * 60 / 1000);
        int h12   = hours % 12 == 0 ? 12 : hours % 12;
        lines.add("§7Hora  §3" + String.format("%02d:%02d %s", h12, mins, hours < 12 ? "AM" : "PM"));

        String dim = client.world.getRegistryKey().getValue().getPath();
        lines.add("§7Dim  §6" + capitalize(dim.replace('_', ' ')));

        double speed = Math.sqrt(
                player.getVelocity().x * player.getVelocity().x +
                player.getVelocity().z * player.getVelocity().z);
        lines.add(String.format("§7Vel  §f%.2f §7bl/t", speed));

        int LINE_H = 11, PAD = 4;
        DrawableHelper.fill(matrices, 1, 1, 155, lines.size() * LINE_H + PAD * 2 + 1, 0x99000000);
        for (int i = 0; i < lines.size(); i++)
            client.textRenderer.drawWithShadow(matrices, lines.get(i), PAD, PAD + i * LINE_H, 0xFFFFFF);
    }

    private static void renderHeldItemInfo(MinecraftClient client, MatrixStack matrices) {
        var held = client.player.getMainHandStack();
        if (held.isEmpty()) return;
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();

        String name = held.hasCustomName()
                ? "§o§f" + held.getName().getString()
                : "§f"   + held.getName().getString();
        String dur = "";
        if (held.isDamageable()) {
            int cur = held.getMaxDamage() - held.getDamage();
            int max = held.getMaxDamage();
            float r = (float) cur / max;
            dur = "  " + (r > 0.5f ? "§a" : r > 0.25f ? "§e" : "§c") + cur + "§8/" + max;
        }
        String stack = held.getCount() > 1 ? "  §7x" + held.getCount() : "";
        String txt = name + dur + stack;
        int tw = client.textRenderer.getWidth(txt);
        int x = (sw - tw) / 2, y = sh - 59;
        DrawableHelper.fill(matrices, x - 3, y - 2, x + tw + 3, y + 10, 0x88000000);
        client.textRenderer.drawWithShadow(matrices, txt, x, y, 0xFFFFFF);
    }

    private static String getFacing(float yaw) {
        float n = ((yaw % 360) + 360) % 360;
        if (n < 22.5f || n >= 337.5f) return "Sur";
        if (n < 67.5f)  return "Sur-Oeste";
        if (n < 112.5f) return "Oeste";
        if (n < 157.5f) return "Nor-Oeste";
        if (n < 202.5f) return "Norte";
        if (n < 247.5f) return "Nor-Este";
        if (n < 292.5f) return "Este";
        return "Sur-Este";
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        for (String w : s.split(" "))
            if (!w.isEmpty())
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.length() > 1 ? w.substring(1) : "").append(' ');
        return sb.toString().trim();
    }
}
