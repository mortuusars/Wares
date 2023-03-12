package io.github.mortuusars.mpfui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mpfui.MPFScreen;
import io.github.mortuusars.mpfui.helper.HorizontalAlignment;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TextElement<T extends MPFScreen<?>> extends UIElement{

    public enum TooltipBehavior {
        FULL,
        OVERFLOW
    }

    public static final int DEFAULT_LINE_SPACING = 0;
    public static final int DEFAULT_FONT_COLOR = 0xFF000000;

    private T screen;
    protected Supplier<Component> textSupplier;

    public HorizontalAlignment horizontalAlignment;
    public int fontColor = DEFAULT_FONT_COLOR;
    public int lineSpacing = DEFAULT_LINE_SPACING;
    public TooltipBehavior tooltipBehavior;
    public int tooltipWidth = 240;

    public List<FormattedCharSequence> leftoverLines;

    private boolean hasLeftoverText = false;

    public TextElement(T parentScreen, String id, int posX, int posY, int width, int height, Supplier<Component> textComponentSupplier) {
        super(id, posX, posY, width, height);
        this.screen = parentScreen;
        this.textSupplier = textComponentSupplier;
    }

    public TextElement(T parentScreen, String id, int posX, int posY, int width, int height, Component textComponent) {
        this(parentScreen, id, posX, posY, width, height, () -> textComponent);
    }

    public TextElement(T parentScreen, String id, int posX, int posY, int width, int height, String text) {
        this(parentScreen, id, posX, posY, width, height, () -> new TextComponent(text));
    }

    public TextElement<T> horizontalAlignment(HorizontalAlignment alignment){
        this.horizontalAlignment = alignment;
        return this;
    }

    public TextElement<T> tooltipBehavior(TooltipBehavior behavior){
        this.tooltipBehavior = behavior;
        return this;
    }

    public TextElement<T> lineSpacing(int spacing){
        lineSpacing = spacing;
        return this;
    }

    public TextElement<T> color(int color){
        fontColor = color;
        return this;
    }


    @Override
    public void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        Font fontRenderer = screen.getFontRenderer();

        Component component = textSupplier.get();
        List<FormattedCharSequence> lines = fontRenderer.split(component, width);

        int maxLines = height / (fontRenderer.lineHeight + lineSpacing);

        hasLeftoverText = maxLines < lines.size();
        leftoverLines = new ArrayList<>(lines.stream().skip(maxLines).toList());

        for (int index = 0; index < maxLines; index++) {
            FormattedCharSequence line = lines.get(index);

            int x = posX - screen.getGuiLeft();
            int y = posY - screen.getGuiTop() + (index * (fontRenderer.lineHeight + lineSpacing));

            switch (horizontalAlignment) {
                case CENTER -> x += width / 2 - fontRenderer.width(line) / 2;
                case RIGHT -> x += width - fontRenderer.width(line);
            }

            if (hasLeftoverText && index == maxLines -1) // draw 3 dots at the end.
                line = FormattedCharSequence.composite(line, FormattedCharSequence.forward("...", Style.EMPTY));

            fontRenderer.draw(poseStack, line, x, y, fontColor);
        }
    }

    @Override
    public void renderTooltip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        if (hasLeftoverText){

            if (tooltipBehavior == TooltipBehavior.OVERFLOW && leftoverLines.size() > 0){
                leftoverLines.set(0, FormattedCharSequence.composite(FormattedCharSequence.forward("...", Style.EMPTY), leftoverLines.get(0)));
                screen.renderTooltip(poseStack, leftoverLines, mouseX, mouseY);
            }
            else {
                Font fontRenderer = screen.getFontRenderer();
                List<FormattedCharSequence> lines = fontRenderer.split(textSupplier.get(), tooltipWidth);
                screen.renderTooltip(poseStack, lines, mouseX, mouseY);
            }
        }
    }

    /**
     * Gets the desired height of the text.
     * @return Height in pixels - how many pixels is needed to draw text fully.
     */
    public static int measure(Font font, FormattedText text, int width, int lineSpacing){
        List<FormattedCharSequence> sequences = font.split(text, width);
        return sequences.size() * (font.lineHeight + lineSpacing);
    }
}