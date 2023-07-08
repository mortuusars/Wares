package io.github.mortuusars.mpfui.renderable;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mpfui.component.TooltipBehavior;
import io.github.mortuusars.mpfui.component.HorizontalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class TextBlockRenderable extends MPFRenderable<TextBlockRenderable> {
    private final Supplier<Component> textSupplier;
    private final int maxLines;
    private @Nullable Integer color;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private TooltipBehavior tooltipBehavior = TooltipBehavior.REGULAR_AND_LEFTOVER;
    private List<FormattedCharSequence> leftoverTooltipLines = new ArrayList<>();
    private final List<FormattedCharSequence> tooltipSeparatorLines = Minecraft.getInstance().font.split(Component.literal("\n-\n"), 30);

    public TextBlockRenderable(Component text, int x, int y, int width, int height) {
        this(() -> text, x, y, width, height);
    }

    public TextBlockRenderable(Supplier<Component> textSupplier, int x, int y, int width, int height) {
        super(x, y, width, height, textSupplier.get());
        this.textSupplier = textSupplier;
        this.maxLines = height / Minecraft.getInstance().font.lineHeight;
    }

    public static int getDesiredHeight(Component text, int width, int maxLines) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> split = font.split(text, width);
        int lines = Math.min(split.size(), maxLines);
        return lines * font.lineHeight;
    }

    @Override
    public TextBlockRenderable getThis() {
        return this;
    }

    public TextBlockRenderable setAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }

    public TextBlockRenderable setTooltipBehavior(TooltipBehavior tooltipBehavior) {
        this.tooltipBehavior = tooltipBehavior;
        return this;
    }

    public TextBlockRenderable setDefaultColor(@Nullable Integer color) {
        this.color = color;
        return this;
    }

    private int getColor() {
        return this.color != null ? this.color : 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isVisible(mouseX, mouseY)) {
            this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + this.width && mouseY < getY() + this.height;
            renderText(graphics, mouseX, mouseY, partialTick);

            if (isHovered)
                renderToolTip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    protected void renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;

        Component text = this.textSupplier.get();

        List<FormattedCharSequence> lines = font.split(text, width);
        boolean hasLeftoverLines = lines.size() > maxLines;

        if (hasLeftoverLines) {
            leftoverTooltipLines = new ArrayList<>(lines.stream().skip(maxLines).toList());
            leftoverTooltipLines.set(0, FormattedCharSequence.composite(FormattedCharSequence.forward("...", text.getStyle()), leftoverTooltipLines.get(0)));
        }
        else
            leftoverTooltipLines.clear();

        for (int lineIndex = 0; lineIndex < Math.min(lines.size(), maxLines); lineIndex++) {
            FormattedCharSequence line = lines.get(lineIndex);
            if (lineIndex + 1 == maxLines && hasLeftoverLines)
                line = FormattedCharSequence.composite(line, FormattedCharSequence.forward("...", text.getStyle()));

            int lineWidth = font.width(line);
            float lineX;

            if (horizontalAlignment == HorizontalAlignment.RIGHT)
                lineX = getX() + (width - lineWidth);
            else if (horizontalAlignment == HorizontalAlignment.CENTER)
                lineX = getX() + (width / 2f - lineWidth / 2f);
            else
                lineX = getX();

            graphics.drawString(font, line, (int) lineX, getY() + (lineIndex * font.lineHeight), getColor(), false);
        }
    }

    public void renderToolTip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.tooltipBehavior == TooltipBehavior.NONE)
            return;

        Screen screen = Minecraft.getInstance().screen;
        if (screen == null)
            return;

        if (this.tooltipBehavior == TooltipBehavior.LEFTOVER_ONLY) {
            graphics.renderTooltip(Minecraft.getInstance().font, leftoverTooltipLines, mouseX, mouseY);
            return;
        }

        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> regularTooltipLines = font.split(this.tooltip.get(), tooltipWidth);

        if (this.tooltipBehavior == TooltipBehavior.REGULAR_ONLY && regularTooltipLines.size() > 0) {
            graphics.renderTooltip(font, regularTooltipLines, mouseX, mouseY);
        }
        else {
            List<FormattedCharSequence> lines = new ArrayList<>();
            if (regularTooltipLines.size() > 0) {
                lines.addAll(regularTooltipLines);
                lines.addAll(tooltipSeparatorLines);
            }

            if (this.tooltipBehavior == TooltipBehavior.FULL)
                lines.addAll(font.split(textSupplier.get(), tooltipWidth));
            else if (this.tooltipBehavior == TooltipBehavior.REGULAR_AND_LEFTOVER && leftoverTooltipLines.size() > 0)
                lines.addAll(leftoverTooltipLines);

            graphics.renderTooltip(font, lines, mouseX, mouseY);
        }
    }
}
