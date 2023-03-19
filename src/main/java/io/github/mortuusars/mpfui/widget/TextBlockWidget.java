package io.github.mortuusars.mpfui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.mpfui.helper.HorizontalAlignment;
import io.github.mortuusars.mpfui.helper.LeftoverTooltipBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TextBlockWidget extends AbstractWidget {
    private AbstractContainerScreen screen;
    private final Supplier<Component> textSupplier;
    private int maxLines;

    private @Nullable Integer color;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private LeftoverTooltipBehavior tooltipBehavior = LeftoverTooltipBehavior.LEFTOVER_ONLY;
    private int tooltipWidth = 220;

    private List<FormattedCharSequence> leftoverLines = new ArrayList<>();

    public TextBlockWidget(AbstractContainerScreen screen, Component text, int x, int y, int width, int height) {
        this(screen, () -> text, x, y, width, height);
    }

    public TextBlockWidget(AbstractContainerScreen screen, Supplier<Component> textSupplier, int x, int y, int width, int height) {
        super(x, y, width, height, textSupplier.get());
        this.screen = screen;
        this.textSupplier = textSupplier;

        this.maxLines = height / Minecraft.getInstance().font.lineHeight;
    }

    public static int getDesiredHeight(Component text, int width, int maxLines) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> split = font.split(text, width);
        int lines = Math.min(split.size(), maxLines);
        return lines * font.lineHeight;
    }

    public TextBlockWidget setAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        return this;
    }

    public TextBlockWidget setTooltipBehavior(LeftoverTooltipBehavior tooltipBehavior) {
        this.tooltipBehavior = tooltipBehavior;
        return this;
    }

    public TextBlockWidget setTooltipWidth(int tooltipWidth) {
        this.tooltipWidth = tooltipWidth;
        return this;
    }

    public TextBlockWidget setDefaultColor(@Nullable Integer color) {
        this.color = color;
        return this;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) { }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            renderText(poseStack, mouseX, mouseX, partialTick);

            if (isHovered && leftoverLines.size() > 0)
                renderToolTip(poseStack, mouseX, mouseY);
        }
    }

    protected void renderText(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;

        Component text = this.textSupplier.get();

        List<FormattedCharSequence> lines = font.split(text, width);
        boolean hasLeftoverLines = lines.size() > maxLines;

        if (hasLeftoverLines) {
            leftoverLines = new ArrayList<>(lines.stream().skip(maxLines).toList());
            leftoverLines.set(0, FormattedCharSequence.composite(FormattedCharSequence.forward("...", text.getStyle()), leftoverLines.get(0)));
        }
        else
            leftoverLines = new ArrayList<>();

        for (int lineIndex = 0; lineIndex < Math.min(lines.size(), maxLines); lineIndex++) {
            FormattedCharSequence line = lines.get(lineIndex);
            if (lineIndex + 1 == maxLines && hasLeftoverLines)
                line = FormattedCharSequence.composite(line, FormattedCharSequence.forward("...", text.getStyle()));

            int lineWidth = font.width(line);
            float lineX;

            if (horizontalAlignment == HorizontalAlignment.RIGHT)
                lineX = x + (width - lineWidth);
            else if (horizontalAlignment == HorizontalAlignment.CENTER)
                lineX = x + (width / 2f - lineWidth / 2f);
            else
                lineX = x;

            font.draw(poseStack, line, lineX, y + (lineIndex * font.lineHeight), getColor());
        }
    }

    private int getColor() {
        return this.color != null ? this.color : 0;
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        if (this.tooltipBehavior == LeftoverTooltipBehavior.FULL)
            screen.renderTooltip(poseStack, Minecraft.getInstance().font.split(textSupplier.get(), tooltipWidth), mouseX, mouseY);
        else
            screen.renderTooltip(poseStack, leftoverLines, mouseX, mouseY);
    }
}
