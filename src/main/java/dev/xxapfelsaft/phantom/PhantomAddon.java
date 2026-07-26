package dev.xxapfelsaft.phantom;

import dev.xxapfelsaft.phantom.feature.commands.PhantomCommand;
import dev.xxapfelsaft.phantom.feature.modules.HitEffectModule;
import dev.xxapfelsaft.phantom.feature.modules.HitSoundModule;
import dev.xxapfelsaft.phantom.feature.modules.CustomNameTagModule;
import dev.xxapfelsaft.phantom.feature.modules.ServerIPElement;
import com.dwarslooper.cactus.client.addon.v2.ICactusAddon;
import com.dwarslooper.cactus.client.addon.v2.RegistryBus;
import com.dwarslooper.cactus.client.feature.module.Category;
import com.dwarslooper.cactus.client.feature.module.Module;
import com.dwarslooper.cactus.client.feature.command.Command;
import com.dwarslooper.cactus.client.gui.hud.element.HudElement;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhantomAddon implements ICactusAddon {

    public static final Logger LOGGER = LoggerFactory.getLogger("Phantom");
    public static final Category CATEGORY = new Category("Phantom", Items.PHANTOM_MEMBRANE);

    @Override
    public void onInitialize(RegistryBus registryBus) {
        LOGGER.info("Initializing Phantom Addon...");

        registryBus.register(Category.class, ctx -> CATEGORY);
        registryBus.register(Command.class, ctx -> new PhantomCommand());
        registryBus.register(Module.class, ctx -> new HitEffectModule());
        registryBus.register(Module.class, ctx -> new HitSoundModule());
        registryBus.register(Module.class, ctx -> new CustomNameTagModule());
        registryBus.register(Module.class, ctx -> new dev.xxapfelsaft.phantom.feature.modules.ClearElementsModule());
        registryBus.register(Module.class, ctx -> new dev.xxapfelsaft.phantom.feature.modules.BridgeAssistModule());
        registryBus.register(HudElement.class, ctx -> new ServerIPElement());
        registryBus.register(HudElement.class, ctx -> new dev.xxapfelsaft.phantom.feature.modules.MediaHudElement());
        registryBus.register(HudElement.class, ctx -> new dev.xxapfelsaft.phantom.feature.modules.ActiveModulesElement());

        dev.xxapfelsaft.phantom.util.MediaMetadataFetcher.start();
        dev.xxapfelsaft.phantom.util.NametagSync.start();

        LOGGER.info("Phantom Addon registered successfully!");
    }

    @Override
    public void onLoadComplete() {
        LOGGER.info("Cactus has finished loading — Phantom is ready.");
    }

    @Override
    public void onShutdown() {
        LOGGER.info("Phantom Addon shutting down.");
    }
}
