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

    private void syncSettings() {
        String baseText = textSetting.get();
        String formatting = "";

        if (boldSetting.get()) formatting += "&l";
        if (italicSetting.get()) formatting += "&o";
        if (underlineSetting.get()) formatting += "&n";
        if (strikeSetting.get()) formatting += "&m";
        if (obfSetting.get()) formatting += "&k";

        baseText = formatting + baseText;

        if (rainbowSetting.get()) {
            customText = "<rainbow>" + baseText + "</rainbow>";
        } else if (useGradientSetting.get()) {
            String hex1 = String.format("%06X", (0xFFFFFF & color1Setting.get().color()));
            String hex2 = String.format("%06X", (0xFFFFFF & color2Setting.get().color()));
            customText = "<gradient:#" + hex1 + ":#" + hex2 + ">" + baseText + "</gradient>";
        } else {
            String hex = String.format("%06X", (0xFFFFFF & singleColorSetting.get().color()));
            customText = "<solid:#" + hex + ">" + baseText + "</solid>";
        }
        
        yOffset = yOffsetSetting.get() / 100.0;
    }
}
