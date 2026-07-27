package dev.xxapfelsaft.phantom.mixin;

import dev.xxapfelsaft.phantom.feature.modules.TransparentScoreboardModule;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Gui.class)
public class ObjectiveTrackerMixin {

    @ModifyArg(method = "displayScoreboardSidebar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V"), index = 4)
    private int modifyScoreboardBackgroundColor(int originalColor) {
        TransparentScoreboardModule module = TransparentScoreboardModule.getInstance();
        if (module != null && module.active()) {
            return 0x00000000; // Fully transparent
        }
        return originalColor;
    }
}
