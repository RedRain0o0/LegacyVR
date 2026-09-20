package dev.redrain0o0.legacyvr.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.VRArmRenderer;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import wily.legacy.skins.client.render.RenderStateSkinIdAccess;
import wily.legacy.skins.client.render.boxloader.AttachSlot;
import wily.legacy.skins.client.render.boxloader.BuiltBoxModel;
import wily.legacy.skins.pose.SkinPoseRegistry;
import wily.legacy.skins.skin.ClientSkinAssets;
import wily.legacy.skins.skin.ClientSkinCache;
import wily.legacy.skins.skin.SkinIdUtil;

import java.util.List;
import java.util.Map;

@Mixin(VRArmRenderer.class)
public abstract class VRArmRendererMixin extends AvatarRenderer<AbstractClientPlayer> {
    protected VRArmRendererMixin(EntityRendererProvider.Context context, boolean slimSteve) {
        super(context, slimSteve);;
    }

    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), cancellable = true, require = 0)
    private void renderHand(ControllerType side, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, Identifier identifier, ModelPart modelPart, boolean sleeve, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        String skinId = ClientSkinCache.get(mc.player.getUUID());
        boolean hasSkin = !SkinIdUtil.isBlankOrAutoSelect(skinId);

        AvatarRenderState state = createRenderState();
        state.swimAmount = mc.player.getSwimAmount(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        if (hasSkin && state instanceof RenderStateSkinIdAccess access) {
            access.consoleskins$setSkinId(skinId);
            access.consoleskins$setEntityUuid(mc.player.getUUID());
            access.consoleskins$setSkipCustomAnimation(true);
        }
        getModel().setupAnim(state);

        if (!hasSkin) return;

        if (SkinPoseRegistry.hasPose(SkinPoseRegistry.PoseTag.HIDE_HAND, skinId)) {
            ci.cancel();
            return;
        }

        ClientSkinAssets.ResolvedSkin resolved = ClientSkinAssets.resolveSkin(skinId);
        Identifier texture = resolved == null ? null : resolved.texture();
        if (texture == null) return;

        BuiltBoxModel built = resolved == null ? null : resolved.boxModel();
        if (built == null) return;

        EntityModel m = getModel();
        if (!(m instanceof PlayerModel pm)) return;

        AttachSlot slot = null;
        if (modelPart == pm.rightArm) slot = AttachSlot.RIGHT_ARM;
        else if (modelPart == pm.leftArm) slot = AttachSlot.LEFT_ARM;
        if (slot == null) return;

        if (!built.hides(slot)) return;

        var parts = built.get(slot);
        if (parts == null || parts.isEmpty()) return;

        Identifier boxTexture = resolved == null || resolved.boxTexture() == null ? texture : resolved.boxTexture();
        final Identifier texFinal = boxTexture;
        final var partsFinal = parts;
        final float partScale = built.partScale();
        final ModelPart modelPartSnapshot = snapshotPart(modelPart);
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                VRRenderTypes.entityTranslucentHand(texFinal),
                (pose, vc) -> {
                    PoseStack ps = new PoseStack();
                    ps.last().set(pose);
                    ps.pushPose();
                    modelPartSnapshot.translateAndRotate(ps);
                    if (partScale != 1.0F) ps.scale(partScale, partScale, partScale);
                    for (ModelPart p : partsFinal) p.render(ps, vc, packedLight, OverlayTexture.NO_OVERLAY);
                    ps.popPose();
                }
        );
        //collector.submitModelPart(rendererArm, poseStack, VRRenderTypes.entityTranslucentHand(identifier), combinedLight, OverlayTexture.NO_OVERLAY, (TextureAtlasSprite)null, ARGB.white(this.armAlpha), (ModelFeatureRenderer.CrumblingOverlay)null);

        ci.cancel();
    }

    @Unique
    private static ModelPart snapshotPart(ModelPart part) {
        ModelPart snapshot = new ModelPart(List.of(), Map.of());
        if (part == null) return snapshot;
        snapshot.visible = part.visible;
        snapshot.x = part.x;
        snapshot.y = part.y;
        snapshot.z = part.z;
        snapshot.xRot = part.xRot;
        snapshot.yRot = part.yRot;
        snapshot.zRot = part.zRot;
        snapshot.xScale = part.xScale;
        snapshot.yScale = part.yScale;
        snapshot.zScale = part.zScale;
        return snapshot;
    }
}
