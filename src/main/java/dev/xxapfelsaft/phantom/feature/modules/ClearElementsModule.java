package dev.xxapfelsaft.phantom.feature.modules;

import com.dwarslooper.cactus.client.feature.module.Module;
import com.dwarslooper.cactus.client.systems.config.settings.impl.BooleanSetting;
import com.dwarslooper.cactus.client.util.game.ChatUtils;
import dev.xxapfelsaft.phantom.PhantomAddon;

public class ClearElementsModule extends Module {

    private static ClearElementsModule instance;

    public final BooleanSetting clearWater = new BooleanSetting("clearWater", true);
    public final BooleanSetting clearLava = new BooleanSetting("clearLava", true);
    public final BooleanSetting noFog = new BooleanSetting("noFog", true);

    public ClearElementsModule() {
        super("clearElements", PhantomAddon.CATEGORY, new Options());
        mainGroup.add(clearWater);
        mainGroup.add(clearLava);
        mainGroup.add(noFog);
        instance = this;
    }

    public static ClearElementsModule getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ChatUtils.infoPrefix("ClearElements", "§cWarning: Using this module on strict online servers may result in a ban!");
    }
}
