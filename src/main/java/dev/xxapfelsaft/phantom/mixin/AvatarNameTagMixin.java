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
        String text = CustomNameTagModule.customText;
        if (text.isEmpty()) return;
        if (state.nameTag == null) return;

        // AvatarRenderer is used for all players in 1.21.11, so we filter by name.
        // We compare the nametag text with the local player's nametag text,
        // stripping any server-side formatting (ChatComponent.getStyle() is ignored).
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Use the player list to get the local player's display name
        var connection = mc.getConnection();
        if (connection == null) return;

        var localInfo = connection.getPlayerInfo(mc.player.getUUID());
        if (localInfo == null) return;

        // Check if the nametag contains the local player's profile name
        // (handles prefixes/suffixes from LuckPerms etc.)
        String profileName = localInfo.getProfile().name();
        if (!state.nameTag.getString().contains(profileName)) return;

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
