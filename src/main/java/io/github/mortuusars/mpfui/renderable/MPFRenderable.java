package io.github.mortuusars.mpfui.renderable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class MPFRenderable<T extends MPFRenderable<T>> extends AbstractWidget {
    protected VisibilityPredicate<T> visibilityPredicate = (renderable, poseStack, mouseX, mouseY) -> true;
    protected Supplier<Component> tooltip = () -> TextComponent.EMPTY;
    protected int tooltipWidth = 220;

    public MPFRenderable(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public MPFRenderable(int x, int y, int width, int height) {
        super(x, y, width, height, TextComponent.EMPTY);
    }

    public abstract T getThis();

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.HINT, getMessage());
    }


    public T setTooltip(Supplier<Component> tooltip) {
        this.tooltip = tooltip;
        return getThis();
    }

    public T setTooltip(Component tooltip) {
        this.tooltip = () -> tooltip;
        return getThis();
    }

    public T setTooltipWidth(int tooltipWidth) {
        this.tooltipWidth = tooltipWidth;
        return getThis();
    }

    public T visibility(VisibilityPredicate<T> predicate) {
        this.visibilityPredicate = predicate;
        return getThis();
    }

    public boolean isVisible(PoseStack poseStack, int mouseX, int mouseY) {
        return this.visible && visibilityPredicate.isVisible(getThis(), poseStack, mouseX, mouseY);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (isVisible(poseStack, mouseX, mouseY)) {
            super.render(poseStack, mouseX, mouseY, partialTick);

            if (isHoveredOrFocused())
                renderToolTip(poseStack, mouseX, mouseY);
        }
    }

    public interface VisibilityPredicate<T extends MPFRenderable<?>> {
        boolean isVisible(T renderable, PoseStack poseStack, int mouseX, int mouseY);
    }

    @Override
    public void renderToolTip(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY) {
        Component tooltipComponent = tooltip.get();
        Screen screen = Minecraft.getInstance().screen;
        if (tooltipComponent != TextComponent.EMPTY && screen != null) {
            screen.renderTooltip(pPoseStack, Minecraft.getInstance().font.split(tooltipComponent, tooltipWidth), pMouseX, pMouseY);
        }
    }
}
