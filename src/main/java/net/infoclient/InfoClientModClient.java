package net.infoclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.infoclient.config.ModConfig;
import net.infoclient.feature.AutoClicker;
import net.infoclient.hud.InfoHud;
import net.infoclient.hud.KeyStrokesHud;
import net.infoclient.screen.SettingsScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class InfoClientModClient implements ClientModInitializer {

    private static KeyBinding openMenuKey;
    private static KeyBinding toggleHudKey;

    @Override
    public void onInitializeClient() {

        // Tecla P -> menu de configuracion
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.infoclient.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.infoclient"
        ));

        // Tecla F4 -> toggle rapido del HUD
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.infoclient.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F4,
                "category.infoclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Abrir/cerrar menu con P
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new SettingsScreen(null));
                } else if (client.currentScreen instanceof SettingsScreen) {
                    client.setScreen(null);
                }
            }

            // Toggle HUD con F4
            while (toggleHudKey.wasPressed()) {
                ModConfig.showHud = !ModConfig.showHud;
            }

            // AutoClicker tick
            AutoClicker.onTick(client);
        });

        // Registrar HUD
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            InfoHud.render(matrices, tickDelta);
            KeyStrokesHud.render(matrices, tickDelta);
        });

        System.out.println("[InfoClient] Cargado! P: Menu | F4: Toggle HUD");
    }
}
