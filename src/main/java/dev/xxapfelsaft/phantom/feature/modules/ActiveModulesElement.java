package dev.xxapfelsaft.phantom.feature.modules;

import com.dwarslooper.cactus.client.feature.module.Module;
import com.dwarslooper.cactus.client.feature.module.ModuleManager;
import com.dwarslooper.cactus.client.gui.hud.element.DynamicHudElement;
import com.dwarslooper.cactus.client.systems.config.settings.impl.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2i;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActiveModulesElement extends DynamicHudElement<ActiveModulesElement> {

    private final BooleanSetting rgbMode = new BooleanSetting("rgbMode", true);
    private final BooleanSetting animationsSetting = new BooleanSetting("animations", true);
    private final BooleanSetting reverseSortSetting = new BooleanSetting("reverseSort", false);
    private final BooleanSetting rightAlignSetting = new BooleanSetting("rightAlign", false);

    private final Map<Module, Float> animationStates = new HashMap<>();

    public ActiveModulesElement() {
        super("active_modules");
        elementGroup.add(rgbMode);
        elementGroup.add(animationsSetting);
        elementGroup.add(reverseSortSetting);
        elementGroup.add(rightAlignSetting);
    }

    @Override
    public ActiveModulesElement duplicate() {
        return new ActiveModulesElement();
    }

    @Override
    public boolean canResize() {
        return false;
    }

    private void updateAnimations() {
        for (Module m : ModuleManager.get().getModules().values()) {
            float current = animationStates.getOrDefault(m, m.active() ? 1f : 0f);
            float target = m.active() ? 1f : 0f;
            
            if (animationsSetting.get()) {
                current += (target - current) * 0.15f;
                if (Math.abs(target - current) < 0.005f) {
                    current = target;
                }
            } else {
                current = target;
            }
            animationStates.put(m, current);
        }
    }

    private List<Module> getVisibleModules(Minecraft mc) {
        return ModuleManager.get().getModules().values().stream()
                .filter(m -> animationStates.getOrDefault(m, m.active() ? 1f : 0f) > 0.01f)
                .sorted((m1, m2) -> {
                    int w1 = mc.font != null ? mc.font.width(m1.getDisplayName()) : 0;
                    int w2 = mc.font != null ? mc.font.width(m2.getDisplayName()) : 0;
                    int diff = reverseSortSetting.get() ? (w1 - w2) : (w2 - w1);
                    return diff != 0 ? diff : m1.getDisplayName().compareTo(m2.getDisplayName());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void renderContent(GuiGraphics context, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean focused) {
        var mc = Minecraft.getInstance();
        if (mc.font == null) return;

        updateAnimations();
        List<Module> visibleModules = getVisibleModules(mc);
        
        if (visibleModules.isEmpty()) {
            return;
        }

        int defaultColor = textColor.get().color();
        boolean shadows = textShadows();
        boolean isRightAligned = rightAlignSetting.get();
        boolean isRgb = rgbMode.get();
        
        int currentY = y + 2;
        int index = 0;

        for (Module module : visibleModules) {
            String name = module.getDisplayName();
            float animState = animationStates.getOrDefault(module, module.active() ? 1f : 0f);
            int textWidth = mc.font.width(name);

            int renderX;
            if (isRightAligned) {
                int targetX = x + width - textWidth - 2;
                int startX = x + width; 
                renderX = (int) (startX + (targetX - startX) * animState);
            } else {
                int targetX = x + 2;
                int startX = x - textWidth; 
                renderX = (int) (startX + (targetX - startX) * animState);
            }

            int color = defaultColor;
            if (isRgb) {
                float hue = (System.currentTimeMillis() % 2000L) / 2000.0f - (index * 0.05f);
                color = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            }

            context.drawString(mc.font, name, renderX, currentY, color, shadows);
            currentY += mc.font.lineHeight + 2;
            index++;
        }
    }

    @Override
    public Vector2i getMinSize() {
        var mc = Minecraft.getInstance();
        if (mc.font == null) return new Vector2i(100, 14);

        List<Module> visibleModules = getVisibleModules(mc);
        
        if (visibleModules.isEmpty()) {
            return new Vector2i(mc.font.width("Active Modules") + 4, 14);
        }

        int maxWidth = 0;
        for (Module module : visibleModules) {
            int w = mc.font.width(module.getDisplayName());
            if (w > maxWidth) maxWidth = w;
        }

        int totalHeight = visibleModules.size() * (mc.font.lineHeight + 2) + 4;
        return new Vector2i(maxWidth + 4, totalHeight);
    }
}
