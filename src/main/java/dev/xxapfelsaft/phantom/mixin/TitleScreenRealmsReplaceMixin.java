package dev.xxapfelsaft.phantom.mixin;

import com.dwarslooper.cactus.client.feature.content.ContentPackManager;
import com.dwarslooper.cactus.client.gui.screen.impl.ButtonOptions;
import com.dwarslooper.cactus.client.gui.screen.impl.CactusMainScreen;
import com.dwarslooper.cactus.client.gui.widget.CTextureButtonWidget;
import com.dwarslooper.cactus.client.util.generic.ScreenUtils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Mixin(value = TitleScreen.class, priority = 2000)
public abstract class TitleScreenRealmsReplaceMixin extends Screen {

    protected TitleScreenRealmsReplaceMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void phantom$replaceRealmsWithCactusButton(CallbackInfo ci) {
        ContentPackManager cpm = ContentPackManager.get();
        boolean noRealms = (cpm != null && cpm.isEnabled(cpm.ofId("no_realms")))
                || ButtonOptions.get().removeRealmsButton.get()
                || ScreenUtils.getButton(this, "menu.online") == null;

        if (!noRealms) {
            return;
        }

        // Find and remove / hide the original 20x20 Cactus button (CTextureButtonWidget or any widget with the cactus title)
        AbstractWidget removedCactusButton = null;
        for (GuiEventListener listener : this.children()) {
            if (listener instanceof CTextureButtonWidget btn) {
                removedCactusButton = btn;
                btn.visible = false;
                btn.active = false;
                this.removeWidget(btn);
                break;
            } else if (listener instanceof AbstractWidget widget && widget.getWidth() == 20 && widget.getHeight() == 20) {
                Component msg = widget.getMessage();
                if (msg != null && "gui.screen.cactus.title".equals(msg.getString())) {
                    removedCactusButton = widget;
                    widget.visible = false;
                    widget.active = false;
                    this.removeWidget(widget);
                    break;
                }
            }
        }

        // If cactusByOptionsButton was enabled, Options button width was shrunk by 24 and moved right by 24.
        // Restore Options button to normal width and position if it was shifted.
        if (ButtonOptions.get().cactusByOptionsButton.get()) {
            AbstractWidget optionsBtn = ScreenUtils.getButton(this, "menu.options");
            if (optionsBtn != null && optionsBtn.getWidth() != 98) {
                // Vanilla options button is normally width 98, x = width / 2 - 100
                optionsBtn.setX(this.width / 2 - 100);
                optionsBtn.setWidth(98);
            }
        }

        // Re-center any bottom toolbar 20x20 buttons if Cactus button was on the toolbar
        if (removedCactusButton != null && !ButtonOptions.get().cactusByOptionsButton.get()) {
            List<AbstractWidget> toolbarButtons = new ArrayList<>();
            int toolbarY = -1;
            for (GuiEventListener listener : this.children()) {
                if (listener instanceof AbstractWidget widget && widget != removedCactusButton && widget.getWidth() == 20 && widget.getHeight() == 20) {
                    if (Math.abs(widget.getX() + widget.getWidth() / 2 - this.width / 2) <= 60) {
                        toolbarButtons.add(widget);
                        toolbarY = widget.getY();
                    }
                }
            }
            if (!toolbarButtons.isEmpty() && toolbarY != -1) {
                toolbarButtons.sort(Comparator.comparingInt(AbstractWidget::getX));
                int totalWidth = toolbarButtons.size() * 20 + Math.max(0, toolbarButtons.size() - 1) * 4;
                int startX = this.width / 2 - totalWidth / 2;
                for (AbstractWidget widget : toolbarButtons) {
                    widget.setPosition(startX, toolbarY);
                    startX += 24;
                }
            }
        }

        // Find multiplayer button to accurately place the Cactus button directly in the realms slot
        AbstractWidget mpButton = ScreenUtils.getButton(this, "menu.multiplayer");
        int x = this.width / 2 - 100;
        int y = mpButton != null ? (mpButton.getY() + 24) : (this.height / 4 + 48 + 48);

        Button cactusFullButton = Button.builder(
                Component.translatable("gui.screen.cactus.title"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new CactusMainScreen(this));
                    }
                }
        ).bounds(x, y, 200, 20).build();

        this.addRenderableWidget(cactusFullButton);
    }
}
