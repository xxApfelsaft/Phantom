package dev.xxapfelsaft.phantom.feature.modules;

import com.dwarslooper.cactus.client.event.EventHandler;
import com.dwarslooper.cactus.client.event.impl.ClientTickEvent;
import com.dwarslooper.cactus.client.feature.module.Module;
import com.dwarslooper.cactus.client.feature.module.Module.Options;
import com.dwarslooper.cactus.client.systems.config.settings.impl.BooleanSetting;
import com.dwarslooper.cactus.client.systems.config.settings.impl.ColorSetting;
import com.dwarslooper.cactus.client.systems.config.settings.impl.IntegerSetting;
import com.dwarslooper.cactus.client.systems.config.settings.impl.StringSetting;
import dev.xxapfelsaft.phantom.PhantomAddon;
import java.awt.Color;

public class CustomNameTagModule extends Module {

    public static CustomNameTagModule instance;
    public static String customText = "";
    public static double yOffset = 0.35;

    private final StringSetting textSetting;
    private final IntegerSetting yOffsetSetting;
    public final BooleanSetting broadcastWhenDisabledSetting;
    
    private final BooleanSetting rainbowSetting;
    private final BooleanSetting useGradientSetting;
    private final ColorSetting color1Setting;
    private final ColorSetting color2Setting;
    private final ColorSetting singleColorSetting;

    private final BooleanSetting boldSetting;
    private final BooleanSetting italicSetting;
    private final BooleanSetting underlineSetting;
    private final BooleanSetting strikeSetting;
    private final BooleanSetting obfSetting;

    public CustomNameTagModule() {
        super("custom_name_tag", PhantomAddon.CATEGORY, new Options());
        instance = this;

        textSetting = new StringSetting("Text", "Sub to me!");
        textSetting.setMaxLength(64);
        mainGroup.add(textSetting);

        yOffsetSetting = new IntegerSetting("YOffset", 35).min(-300).max(300);
        mainGroup.add(yOffsetSetting);

        broadcastWhenDisabledSetting = new BooleanSetting("broadcastWhenDisabled", true);
        mainGroup.add(broadcastWhenDisabledSetting);

        rainbowSetting = new BooleanSetting("rainbow", false);
        mainGroup.add(rainbowSetting);

        useGradientSetting = new BooleanSetting("useGradient", true);
        mainGroup.add(useGradientSetting);

        color1Setting = new ColorSetting("gradientStart", ColorSetting.ColorValue.of(Color.RED, false));
        mainGroup.add(color1Setting);

        color2Setting = new ColorSetting("gradientEnd", ColorSetting.ColorValue.of(Color.BLUE, false));
        mainGroup.add(color2Setting);

        singleColorSetting = new ColorSetting("singleColor", ColorSetting.ColorValue.of(Color.WHITE, false));
        mainGroup.add(singleColorSetting);

        boldSetting = new BooleanSetting("bold", false);
        mainGroup.add(boldSetting);

        italicSetting = new BooleanSetting("italic", false);
        mainGroup.add(italicSetting);

        underlineSetting = new BooleanSetting("underline", false);
        mainGroup.add(underlineSetting);

        strikeSetting = new BooleanSetting("strikethrough", false);
        mainGroup.add(strikeSetting);

        obfSetting = new BooleanSetting("obfuscated", false);
        mainGroup.add(obfSetting);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        syncSettings();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        customText = "";
    }

    @EventHandler
    public void onTick(ClientTickEvent event) {
        if (!active()) return;
        syncSettings();
    }

    public static String getCurrentText() {
        if (instance == null) return "";
        String baseText = instance.textSetting.get();
        String formatting = "";

        if (instance.boldSetting.get()) formatting += "&l";
        if (instance.italicSetting.get()) formatting += "&o";
        if (instance.underlineSetting.get()) formatting += "&n";
        if (instance.strikeSetting.get()) formatting += "&m";
        if (instance.obfSetting.get()) formatting += "&k";

        baseText = formatting + baseText;

        if (instance.rainbowSetting.get()) {
            return "<rainbow>" + baseText + "</rainbow>";
        } else if (instance.useGradientSetting.get()) {
            String hex1 = String.format("%06X", (0xFFFFFF & instance.color1Setting.get().color()));
            String hex2 = String.format("%06X", (0xFFFFFF & instance.color2Setting.get().color()));
            return "<gradient:#" + hex1 + ":#" + hex2 + ">" + baseText + "</gradient>";
        } else {
            String hex = String.format("%06X", (0xFFFFFF & instance.singleColorSetting.get().color()));
            return "<solid:#" + hex + ">" + baseText + "</solid>";
        }
    }

    public static double getCurrentYOffset() {
        if (instance == null) return 0.35;
        return instance.yOffsetSetting.get() / 100.0;
    }

    private void syncSettings() {
        customText = getCurrentText();
        yOffset = getCurrentYOffset();
    }
}
