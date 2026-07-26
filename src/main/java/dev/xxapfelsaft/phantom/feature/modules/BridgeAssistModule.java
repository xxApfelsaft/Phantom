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

        boolean isBridging = mc.player.getXRot() > 50.0f &&
            (mc.player.getMainHandItem().getItem() instanceof net.minecraft.world.item.BlockItem ||
             mc.player.getOffhandItem().getItem() instanceof net.minecraft.world.item.BlockItem);

        if (mc.player.onGround() || isBridging) {
            BlockPos posBelow = mc.player.blockPosition().below();
            boolean overAir = mc.level.getBlockState(posBelow).isAir();

            if (overAir) {
                if (!mc.options.keyShift.isDown()) {
                    mc.options.keyShift.setDown(true);
                    isForcingSneak = true;
                }
            } else {
                if (isForcingSneak) {
                    mc.options.keyShift.setDown(false);
                    isForcingSneak = false;
                }
            }
        } else {
            if (isForcingSneak) {
                mc.options.keyShift.setDown(false);
                isForcingSneak = false;
            }
        }
    }
}
