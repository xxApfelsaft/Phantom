package dev.xxapfelsaft.phantom.feature.modules;

import com.dwarslooper.cactus.client.gui.hud.element.DynamicHudElement;
import dev.xxapfelsaft.phantom.util.MediaMetadataFetcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2i;

public class MediaHudElement extends DynamicHudElement<MediaHudElement> {

    private static final Identifier YTMUSIC_ICON = Identifier.fromNamespaceAndPath("phantom", "textures/gui/ytmusic.png");
    private static final Identifier SPOTIFY_ICON = Identifier.fromNamespaceAndPath("phantom", "textures/gui/spotify.png");
    private static final Identifier SOUNDCLOUD_ICON = Identifier.fromNamespaceAndPath("phantom", "textures/gui/soundcloud.png");
    private static final Identifier APPLE_MUSIC_ICON = Identifier.fromNamespaceAndPath("phantom", "textures/gui/applemusic.png");
    private static final Identifier DEEZER_ICON = Identifier.fromNamespaceAndPath("phantom", "textures/gui/deezer.png");

    public MediaHudElement() {
        super("media");
        MediaMetadataFetcher.start();
    }

    @Override
    public MediaHudElement duplicate() {
        return new MediaHudElement();
    }

    @Override
    public boolean canResize() {
        return false;
    }

    @Override
    public void renderContent(GuiGraphicsExtractor context, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean focused) {
        var mc = Minecraft.getInstance();
        if (mc.font == null) return;

        Vector2i minSize = getMinSize();
        if (getSize().x != minSize.x || getSize().y != minSize.y) {
            resize(minSize.x, minSize.y);
        }

        String title = MediaMetadataFetcher.getTitle();
        String artist = MediaMetadataFetcher.getArtist();
        
        String displayText = title;
        if (artist != null && !artist.isEmpty()) {
            displayText += " - " + artist;
        }

        int color = textColor.get().color();

        MediaMetadataFetcher.Platform platform = MediaMetadataFetcher.getPlatform();
        int iconSize = mc.font.lineHeight;
        int textX = x + 2;

        if (platform == MediaMetadataFetcher.Platform.YOUTUBE_MUSIC) {
            context.blit(YTMUSIC_ICON, textX, y + 2, textX + iconSize, y + 2 + iconSize, 0f, 1f, 0f, 1f);
            textX += iconSize + 2;
        } else if (platform == MediaMetadataFetcher.Platform.SPOTIFY) {
            context.blit(SPOTIFY_ICON, textX, y + 2, textX + iconSize, y + 2 + iconSize, 0f, 1f, 0f, 1f);
            textX += iconSize + 2;
        } else if (platform == MediaMetadataFetcher.Platform.SOUNDCLOUD) {
            context.blit(SOUNDCLOUD_ICON, textX, y + 2, textX + iconSize, y + 2 + iconSize, 0f, 1f, 0f, 1f);
            textX += iconSize + 2;
        } else if (platform == MediaMetadataFetcher.Platform.APPLE_MUSIC) {
            context.blit(APPLE_MUSIC_ICON, textX, y + 2, textX + iconSize, y + 2 + iconSize, 0f, 1f, 0f, 1f);
            textX += iconSize + 2;
        } else if (platform == MediaMetadataFetcher.Platform.DEEZER) {
            context.blit(DEEZER_ICON, textX, y + 2, textX + iconSize, y + 2 + iconSize, 0f, 1f, 0f, 1f);
            textX += iconSize + 2;
        } else {
            context.text(mc.font, "♫", textX, y + 2, color, textShadows());
            textX += mc.font.width("♫ ");
        }

        context.text(mc.font, displayText, textX, y + 2, color, textShadows());

        long length = MediaMetadataFetcher.getLength();
        if (length > 0) {
            long pos = MediaMetadataFetcher.getPosition();
            long lastFetch = MediaMetadataFetcher.getLastFetchTime();
            
            if (MediaMetadataFetcher.isPlaying() && lastFetch > 0) {
                pos += (System.currentTimeMillis() - lastFetch);
            }
            if (pos > length) pos = length;

            int barY = y + height - 2;
            int barX = x + 2;
            int barWidth = width - 4;
            
            context.fill(barX, barY, barX + barWidth, barY + 1, 0x80AAAAAA);
            if (pos > 0) {
                int fillWidth = (int) ((double) pos / length * barWidth);
                context.fill(barX, barY, barX + fillWidth, barY + 1, color | 0xFF000000);
            }
        }
    }

    @Override
    public Vector2i getMinSize() {
        var mc = Minecraft.getInstance();
        if (mc.font == null) return new Vector2i(100, 14);

        String title = MediaMetadataFetcher.getTitle();
        String artist = MediaMetadataFetcher.getArtist();
        
        String displayText = title;
        if (artist != null && !artist.isEmpty()) {
            displayText += " - " + artist;
        }

        int w = 0;
        MediaMetadataFetcher.Platform platform = MediaMetadataFetcher.getPlatform();
        if (platform == MediaMetadataFetcher.Platform.YOUTUBE_MUSIC || platform == MediaMetadataFetcher.Platform.SPOTIFY) {
            w = mc.font.lineHeight + 2 + mc.font.width(displayText);
        } else {
            w = mc.font.width("♫ " + displayText);
        }

        int h = mc.font.lineHeight + 4;
        
        if (MediaMetadataFetcher.getLength() > 0) {
            h += 3;
        }
        
        return new Vector2i(w + 4, h);
    }
}
