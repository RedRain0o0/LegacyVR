package dev.redrain0o0.legacyvr.mixin.client;

import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wily.legacy.client.screen.RenderableVList;

@Mixin(value = TitleScreen.class, priority = 1500)
public abstract class TitleScreenMixin extends Screen {
    @Shadow private @Final RenderableVList renderableVList;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @TargetHandler(mixin = "wily.legacy.mixin.base.client.title.TitleScreenMixin", name = "rebuildMenuButtons")
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lwily/legacy/client/screen/RenderableVList;addRenderable(Lnet/minecraft/client/gui/components/Renderable;)Lwily/legacy/client/screen/RenderableVList;", ordinal = 5, shift = At.Shift.AFTER))
    private void legacyvr$addVrButton(CallbackInfo ci) {
        //renderableVList.addRenderable(Button.builder(Component.literal("vr"), b -> minecraft.setScreen(new HelpAndOptionsScreen(this))).build());
    }
}
