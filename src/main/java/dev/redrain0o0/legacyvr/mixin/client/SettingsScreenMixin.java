package dev.redrain0o0.legacyvr.mixin.client;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import wily.legacy.client.screen.HelpAndOptionsScreen;
import wily.legacy.client.screen.RenderableVList;
import wily.legacy.client.screen.RenderableVListScreen;

import java.util.function.Consumer;

@Mixin(HelpAndOptionsScreen.class)
public abstract class HelpAndOptionsScreenMixin extends RenderableVListScreen {
    public HelpAndOptionsScreenMixin(Component component, Consumer<RenderableVList> vListBuild) {
        super(component, vListBuild);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lwily/legacy/client/screen/RenderableVList;addRenderable(Lnet/minecraft/client/gui/components/Renderable;)Lwily/legacy/client/screen/RenderableVList;", ordinal = ))
}
