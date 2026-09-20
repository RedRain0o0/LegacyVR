package dev.redrain0o0.legacyvr.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wily.legacy.client.screen.RenderableVList;
import wily.legacy.client.screen.RenderableVListScreen;
import wily.legacy.client.screen.SettingsScreen;

import java.util.function.Consumer;

@Mixin(SettingsScreen.class)
public abstract class SettingsScreenMixin extends RenderableVListScreen {
    public SettingsScreenMixin(Component component, Consumer<RenderableVList> vListBuild) {
        super(component, vListBuild);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lwily/legacy/client/screen/RenderableVList;addRenderable(Lnet/minecraft/client/gui/components/Renderable;)Lwily/legacy/client/screen/RenderableVList;", ordinal = 1))
    private void legacyvr$init(Screen parent, CallbackInfo ci) {
        //renderableVList.addRenderable(openScreenButton(Component.translatable("legacy.menu.vr_options"), () -> ).build());
    }
}
