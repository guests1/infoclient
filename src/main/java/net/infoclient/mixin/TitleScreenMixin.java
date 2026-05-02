package net.infoclient.mixin;

import net.infoclient.screen.CustomTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class TitleScreenMixin {

    /**
     * Se inyecta cuando Minecraft va a mostrar el TitleScreen por primera vez
     * y lo reemplaza con nuestro CustomTitleScreen.
     */
    @Inject(
        method = "setScreen",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSetScreen(net.minecraft.client.gui.screen.Screen screen, CallbackInfo ci) {
        if (screen instanceof TitleScreen) {
            MinecraftClient client = (MinecraftClient)(Object)this;
            // Solo reemplazar si no estamos ya en nuestro menú
            if (!(client.currentScreen instanceof CustomTitleScreen)) {
                client.execute(() -> {
                    MinecraftClient.getInstance().setScreen(new CustomTitleScreen());
                });
                ci.cancel();
            }
        }
    }
}
