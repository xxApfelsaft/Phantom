package dev.xxapfelsaft.phantom.mixin;

import dev.xxapfelsaft.phantom.feature.modules.CustomNameTagModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarNameTagMixin {

    @Inject(at = @At("TAIL"), method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
    private void afterSubmitNameTag(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        // Get player from entity ID
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        
        net.minecraft.world.entity.Entity entity = mc.level.getEntity(state.id);
        if (!(entity instanceof net.minecraft.world.entity.player.Player player)) return;
        
        String uuid = player.getUUID().toString();
        String text = null;
        
        synchronized (dev.xxapfelsaft.phantom.util.NametagSync.globalTags) {
            text = dev.xxapfelsaft.phantom.util.NametagSync.globalTags.get(uuid);
        }
        
        if (text == null || text.isEmpty()) return;

        Vec3 attachment = state.nameTagAttachment;
        double yShift = CustomNameTagModule.yOffset;

        // Automatically shift up if there is a below-name scoreboard objective (like money, health)
        if (state.scoreText != null) {
            yShift += 0.3;
        }

        collector.submitNameTag(
            poseStack,
            new Vec3(attachment.x, attachment.y + yShift, attachment.z),
            0,
            dev.xxapfelsaft.phantom.util.TextUtil.parse(text),
            !state.isDiscrete,
            state.lightCoords,
            state.distanceToCameraSq,
            camera
        );
    }
}
