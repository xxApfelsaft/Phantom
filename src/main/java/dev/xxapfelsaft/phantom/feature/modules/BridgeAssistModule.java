package dev.xxapfelsaft.phantom.feature.modules;

import com.dwarslooper.cactus.client.event.EventHandler;
import com.dwarslooper.cactus.client.event.impl.ClientTickEvent;
import com.dwarslooper.cactus.client.feature.module.Module;
import com.dwarslooper.cactus.client.util.game.ChatUtils;
import dev.xxapfelsaft.phantom.PhantomAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class BridgeAssistModule extends Module {

    private boolean isForcingSneak = false;

    public BridgeAssistModule() {
        super("bridge_assist", PhantomAddon.CATEGORY, new Options());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ChatUtils.infoPrefix("BridgeAssist", "§cWarning: Using this module on online servers (like Hypixel) may result in a ban! It is also currently experimental and buggy.");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (isForcingSneak) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.options != null) {
                mc.options.keyShift.setDown(false);
            }
            isForcingSneak = false;
        }
    }

    @EventHandler
    public void onTick(ClientTickEvent event) {
        if (!active()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Check if the player is likely bridging (looking down and holding a block)
        boolean isBridging = mc.player.getXRot() > 50.0f &&
            (mc.player.getMainHandItem().getItem() instanceof net.minecraft.world.item.BlockItem ||
             mc.player.getOffhandItem().getItem() instanceof net.minecraft.world.item.BlockItem);

        // Assist if on the ground, or if they are jump-bridging
        if (mc.player.onGround() || isBridging) {
            BlockPos posBelow = mc.player.blockPosition().below();
            boolean overAir = mc.level.getBlockState(posBelow).isAir();

            if (overAir) {
                // If over air and not already sneaking, force sneak
                if (!mc.options.keyShift.isDown()) {
                    mc.options.keyShift.setDown(true);
                    isForcingSneak = true;
                }
            } else {
                // If over solid block and we were the ones who forced the sneak, release it
                if (isForcingSneak) {
                    mc.options.keyShift.setDown(false);
                    isForcingSneak = false;
                }
            }
        } else {
            // Release sneak if we are jumping or falling normally
            if (isForcingSneak) {
                mc.options.keyShift.setDown(false);
                isForcingSneak = false;
            }
        }
    }
}
