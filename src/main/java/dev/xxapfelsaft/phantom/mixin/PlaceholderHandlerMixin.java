package dev.xxapfelsaft.phantom.mixin;

import com.dwarslooper.cactus.client.systems.params.PlaceholderHandler;
import dev.xxapfelsaft.phantom.util.PhantomPlaceholders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlaceholderHandler.class, remap = false)
public abstract class PlaceholderHandlerMixin {

    @Shadow
    private void registerPlaceholder(String name, PlaceholderHandler.PlaceholderGetter getter, String fallback) {
    }

    @Inject(method = "register", at = @At("TAIL"))
    private void onRegister(CallbackInfo ci) {
        PhantomPlaceholders.registerAll(this::registerPlaceholder);
    }
}
