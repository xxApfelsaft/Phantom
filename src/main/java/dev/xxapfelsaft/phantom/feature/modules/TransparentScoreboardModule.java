package dev.xxapfelsaft.phantom.feature.modules;

import com.dwarslooper.cactus.client.feature.module.Module;
import dev.xxapfelsaft.phantom.PhantomAddon;

public class TransparentScoreboardModule extends Module {
    
    private static TransparentScoreboardModule instance;
    
    public TransparentScoreboardModule() {
        super("transparent_scoreboard", PhantomAddon.CATEGORY, new Options());
        instance = this;
    }
    
    public static TransparentScoreboardModule getInstance() {
        return instance;
    }
}
