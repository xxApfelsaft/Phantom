package dev.xxapfelsaft.phantom.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {
    
    // Matches: <rainbow>Text</rainbow>, <gradient:#RRGGBB:#RRGGBB>Text</gradient>, <solid:#RRGGBB>Text</solid>
    private static final Pattern TAG_PATTERN = Pattern.compile("<(gradient|rainbow|solid)(?::#([0-9a-fA-F]{6}))?(?::#([0-9a-fA-F]{6}))?>(.*?)</\\1>");

    public static Component parse(String text) {
        // Handle classic Minecraft color codes (&c -> §c)
        text = text.replace("&", "§");

        Matcher matcher = TAG_PATTERN.matcher(text);
        if (!matcher.find()) {
            return Component.literal(text);
        }

        MutableComponent component = Component.empty();
        int lastEnd = 0;

        matcher.reset();
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                appendFormatted(component, text.substring(lastEnd, matcher.start()), 0xFFFFFF, 0xFFFFFF, false, false);
            }

            String tag = matcher.group(1);
            String content = matcher.group(4);

            if (tag.equals("rainbow")) {
                appendFormatted(component, content, 0, 0, false, true);
            } else if (tag.equals("gradient")) {
                int colorStart = Integer.parseInt(matcher.group(2), 16);
                int colorEnd = Integer.parseInt(matcher.group(3), 16);
                appendFormatted(component, content, colorStart, colorEnd, true, false);
            } else if (tag.equals("solid")) {
                int color = Integer.parseInt(matcher.group(2), 16);
                appendFormatted(component, content, color, color, false, false);
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            appendFormatted(component, text.substring(lastEnd), 0xFFFFFF, 0xFFFFFF, false, false);
        }

        return component;
    }

    private static void appendFormatted(MutableComponent parent, String text, int colorStart, int colorEnd, boolean gradient, boolean rainbow) {
        Style currentStyle = Style.EMPTY;
        int actualChars = 0;
        int totalActualChars = 0;

        // First pass: count actual renderable characters for correct gradient interpolation
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                i++; // skip code
            } else {
                totalActualChars++;
            }
        }

        long time = System.currentTimeMillis();

        // Second pass: build components
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (code == 'l') currentStyle = currentStyle.withBold(true);
                else if (code == 'o') currentStyle = currentStyle.withItalic(true);
                else if (code == 'n') currentStyle = currentStyle.withUnderlined(true);
                else if (code == 'm') currentStyle = currentStyle.withStrikethrough(true);
                else if (code == 'k') currentStyle = currentStyle.withObfuscated(true);
                else if (code == 'r') currentStyle = Style.EMPTY;
                i++; // skip the formatting code
                continue;
            }

            int rgb = colorStart;
            if (gradient) {
                float ratio = totalActualChars > 1 ? (float) actualChars / (totalActualChars - 1) : 0f;
                int r = (int) (getRed(colorStart) + ratio * (getRed(colorEnd) - getRed(colorStart)));
                int g = (int) (getGreen(colorStart) + ratio * (getGreen(colorEnd) - getGreen(colorStart)));
                int b = (int) (getBlue(colorStart) + ratio * (getBlue(colorEnd) - getBlue(colorStart)));
                rgb = (r << 16) | (g << 8) | b;
            } else if (rainbow) {
                float hue = (time + (actualChars * 100)) % 2000L / 2000.0f;
                rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
            }

            parent.append(Component.literal(String.valueOf(c))
                .withStyle(currentStyle.withColor(TextColor.fromRgb(rgb))));
            
            actualChars++;
        }
    }

    private static int getRed(int rgb) { return (rgb >> 16) & 0xFF; }
    private static int getGreen(int rgb) { return (rgb >> 8) & 0xFF; }
    private static int getBlue(int rgb) { return rgb & 0xFF; }
}
