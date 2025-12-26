package com.maxwell.qliphoth_armaments.common.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GradientTextUtil {

    
    public static MutableComponent createAnimatedGradient(String text, int speed, Color... colors) {
        if (colors.length < 2) {
            return Component.literal(text).setStyle(Style.EMPTY.withColor(colors[0].getRGB()));
        }
        MutableComponent component = Component.empty();
        int textLength = text.length();
        if (textLength == 0) return component;
        long cycleDuration = (long) speed * textLength;
        float totalOffset = (float) (System.currentTimeMillis() % cycleDuration) / cycleDuration;
        for (int i = 0; i < textLength; i++) {
            float charPosition = (float) i / textLength;
            float effectivePosition = (charPosition + totalOffset) % 1.0f;
            Color finalColor = getColorAt(effectivePosition, colors);
            char character = text.charAt(i);
            component.append(Component.literal(String.valueOf(character))
                    .setStyle(Style.EMPTY.withColor(finalColor.getRGB()).withItalic(false)));
        }
        return component;
    }

    
    private static Color getColorAt(float position, Color... colors) {
        int colorCount = colors.length;
        float segmentSpan = 1.0f / (colorCount - 1);
        int segmentIndex = (int) (position / segmentSpan);
        if (segmentIndex >= colorCount - 1) {
            return colors[colorCount - 1];
        }
        float ratioInSegment = (position - (segmentIndex * segmentSpan)) / segmentSpan;
        Color startColor = colors[segmentIndex];
        Color endColor = colors[segmentIndex + 1];
        return interpolate(startColor, endColor, ratioInSegment);
    }

    
    public static MutableComponent createGradient(String text, Color... colors) {
        if (colors.length < 2) {
            return Component.literal(text).setStyle(Style.EMPTY.withColor(colors[0].getRGB()));
        }
        MutableComponent component = Component.empty();
        int textLength = text.length();
        int colorCount = colors.length;
        int segmentLength = textLength / (colorCount - 1);
        List<Color> gradientColors = new ArrayList<>();
        for (int i = 0; i < colorCount - 1; i++) {
            Color startColor = colors[i];
            Color endColor = colors[i + 1];
            int currentSegmentLength = (i == colorCount - 2) ? (textLength - i * segmentLength) : segmentLength;
            for (int j = 0; j < currentSegmentLength; j++) {
                float ratio = (float) j / (currentSegmentLength - 1);
                if (currentSegmentLength == 1) ratio = 0;
                gradientColors.add(interpolate(startColor, endColor, ratio));
            }
        }
        for (int i = 0; i < textLength; i++) {
            char character = text.charAt(i);
            Color charColor = gradientColors.get(i);
            component.append(Component.literal(String.valueOf(character))
                    .setStyle(Style.EMPTY.withColor(charColor.getRGB()).withItalic(false)));
        }
        return component;
    }

    
    private static Color interpolate(Color color1, Color color2, float ratio) {
        int r = (int) (color1.getRed() * (1 - ratio) + color2.getRed() * ratio);
        int g = (int) (color1.getGreen() * (1 - ratio) + color2.getGreen() * ratio);
        int b = (int) (color1.getBlue() * (1 - ratio) + color2.getBlue() * ratio);
        return new Color(r, g, b);
    }
}