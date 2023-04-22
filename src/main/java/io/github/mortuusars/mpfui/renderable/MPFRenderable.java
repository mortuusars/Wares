package io.github.mortuusars.mpfui.renderable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class MPFRenderable<T extends MPFRenderable<T>> extends AbstractWidget {
    protected VisibilityPredicate<T> visibilityPredicate = (renderable, poseStack, mouseX, mouseY) -> true;
    protected Supplier<Component> tooltip = Component::empty;
    protected int tooltipWidth = 220;

    public MPFRenderable(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public MPFRenderable(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public abstract T getThis();

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
        }
    }

    public interface VisibilityPredicate<T extends MPFRenderable<?>> {
        boolean isVisible(T renderable, PoseStack poseStack, int mouseX, int mouseY);
    }
}
