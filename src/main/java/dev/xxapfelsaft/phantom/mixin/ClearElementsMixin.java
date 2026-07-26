package dev.xxapfelsaft.phantom.mixin;

import dev.xxapfelsaft.phantom.feature.modules.ClearElementsModule;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WaterFogEnvironment.class, LavaFogEnvironment.class, AtmosphericFogEnvironment.class})
public class ClearElementsMixin {

    @Inject(method = "setupFog", at = @At("TAIL"))
    private void onSetupFog(FogData data, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
        ClearElementsModule module = ClearElementsModule.getInstance();
        if (module == null || !module.active()) return;

        Object self = this;
        if (self instanceof AtmosphericFogEnvironment && module.noFog.get()) {
            data.renderDistanceStart = renderDistance * 10.0f;
            data.renderDistanceEnd = renderDistance * 11.0f;
            data.environmentalStart = renderDistance * 10.0f;
            data.environmentalEnd = renderDistance * 11.0f;
            data.skyEnd = renderDistance * 10.0f;
        } else if (self instanceof WaterFogEnvironment && module.clearWater.get()) {
            data.renderDistanceStart = 1000.0f;
            data.renderDistanceEnd = 1000.0f;
            data.environmentalStart = 1000.0f;
            data.environmentalEnd = 1000.0f;
        } else if (self instanceof LavaFogEnvironment && module.clearLava.get()) {
            data.renderDistanceStart = 1000.0f;
            data.renderDistanceEnd = 1000.0f;
            data.environmentalStart = 1000.0f;
            data.environmentalEnd = 1000.0f;
        }
    }
}
