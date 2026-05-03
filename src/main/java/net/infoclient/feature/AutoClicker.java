package net.infoclient.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.infoclient.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@Environment(EnvType.CLIENT)
public class AutoClicker {

    private static long lastClickMs   = 0;
    private static int  clicksThisSec = 0;
    private static long secStart      = System.currentTimeMillis();
    public  static int  realCps       = 0;

    public static void onTick(MinecraftClient client) {
        if (!ModConfig.autoClicker) return;
        if (client.player == null || client.world == null) return;
        if (client.currentScreen != null) return;
        if (!client.options.attackKey.isPressed()) return;

        long now   = System.currentTimeMillis();
        long delay = 1000L / Math.max(1, Math.min(20, ModConfig.autoClickerCps));

        if (now - secStart >= 1000) {
            realCps       = clicksThisSec;
            clicksThisSec = 0;
            secStart      = now;
        }
        if (now - lastClickMs < delay) return;
        lastClickMs = now;

        HitResult target = client.crosshairTarget;
        if (target == null || target.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) target).getEntity();
        client.interactionManager.attackEntity(client.player, entity);
        client.player.swingHand(Hand.MAIN_HAND);
        clicksThisSec++;
    }
}
