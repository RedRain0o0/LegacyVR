package dev.redrain0o0.legacyvr.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.VRArmRenderer;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import wily.legacy.skins.client.render.boxloader.AttachSlot;
import wily.legacy.skins.client.render.boxloader.BoxModelManager;
import wily.legacy.skins.client.render.boxloader.BuiltBoxModel;
import wily.legacy.skins.pose.SkinPoseRegistry;
import wily.legacy.skins.skin.ClientSkinAssets;
import wily.legacy.skins.skin.ClientSkinCache;
import wily.legacy.skins.skin.SkinFairness;
import wily.legacy.skins.skin.SkinIdUtil;

import java.util.List;

@Mixin(VRArmRenderer.class)
public abstract class VRArmRendererMixin extends AvatarRenderer<AbstractClientPlayer> {
    @Shadow public float armAlpha;

    protected VRArmRendererMixin(EntityRendererProvider.Context context, boolean slimSteve) {
        super(context, slimSteve);
    }

    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lorg/vivecraft/client_vr/render/rendertypes/VRRenderTypes;entityTranslucentHand(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"), cancellable = true)
    private void legacyvr$renderHand(ControllerType side, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, Identifier identifier, ModelPart arm, boolean sleeve, CallbackInfo ci) {
        ModelPart sleevePart = side == ControllerType.RIGHT ? getModel().rightSleeve : getModel().leftSleeve;
        arm.skipDraw = false;
        sleevePart.resetPose();
        sleevePart.skipDraw = false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        String skinId = SkinFairness.effectiveSkinId(mc, ClientSkinCache.get(mc.player.getUUID(), mc.player.getScoreboardName()));
        if (SkinIdUtil.isBlankOrAutoSelect(skinId)) return;

        ClientSkinAssets.ResolvedSkin resolved = ClientSkinAssets.resolveSkin(skinId, mc.player.getUUID());
        if (SkinPoseRegistry.hasPose(SkinPoseRegistry.PoseTag.HIDE_HAND, skinId)) {
            ci.cancel();
            return;
        }
        if (resolved == null || resolved.texture() == null) return;

        AttachSlot armSlot = side == ControllerType.RIGHT ? AttachSlot.RIGHT_ARM : AttachSlot.LEFT_ARM;
        AttachSlot sleeveSlot = side == ControllerType.RIGHT ? AttachSlot.RIGHT_SLEEVE : AttachSlot.LEFT_SLEEVE;
        BuiltBoxModel built = resolved.boxModel();
        if (resolved.modelId() != null) {
            var offsets = BoxModelManager.getOffsets(resolved.modelId());
            var scales = BoxModelManager.getScales(resolved.modelId());
            legacyvr$applyTransform(arm, offsets == null ? null : offsets.get(armSlot), scales == null ? null : scales.get(armSlot));
            legacyvr$applyTransform(sleevePart, offsets == null ? null : offsets.get(sleeveSlot), scales == null ? null : scales.get(sleeveSlot));
        }
        if (built != null) {
            arm.skipDraw = built.hides(armSlot);
            sleevePart.skipDraw = built.hides(sleeveSlot);
        }

        int color = ARGB.white(armAlpha);
        collector.submitModelPart(arm, poseStack, VRRenderTypes.entityTranslucentHand(resolved.texture()), packedLight, OverlayTexture.NO_OVERLAY, null, color, null);
        if (built != null) {
            Identifier boxTexture = resolved.boxTexture() == null ? resolved.texture() : resolved.boxTexture();
            RenderType renderType = VRRenderTypes.entityTranslucentHand(boxTexture);
            poseStack.pushPose();
            arm.translateAndRotate(poseStack);
            legacyvr$submitParts(built.get(armSlot), built.partScale(), poseStack, collector, renderType, packedLight, color);
            if (sleeve) {
                sleevePart.translateAndRotate(poseStack);
                legacyvr$submitParts(built.get(sleeveSlot), built.partScale(), poseStack, collector, renderType, packedLight, color);
            }
            poseStack.popPose();
        }
        ci.cancel();
    }

    @Unique
    private static void legacyvr$applyTransform(ModelPart part, float[] offset, float[] scale) {
        if (offset != null) {
            part.x += offset[0];
            part.y += offset[1];
            part.z += offset[2];
        }
        if (scale != null) {
            part.xScale = scale[0];
            part.yScale = scale[1];
            part.zScale = scale[2];
        }
    }

    @Unique
    private static void legacyvr$submitParts(List<ModelPart> parts, float partScale, PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, int packedLight, int color) {
        if (parts == null || parts.isEmpty()) return;
        poseStack.pushPose();
        if (partScale != 1.0F) poseStack.scale(partScale, partScale, partScale);
        for (ModelPart part : parts) {
            collector.submitModelPart(part, poseStack, renderType, packedLight, OverlayTexture.NO_OVERLAY, null, color, null);
        }
        poseStack.popPose();
    }
}
