package io.github.mortuusars.mpfui.renderable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CombinedContentWidget extends Button {
    public List<AbstractWidget> elements = new ArrayList<>();

    public CombinedContentWidget(int x, int y, int width, int height, Component message, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, width, height, message, onPress, onTooltip);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            for (AbstractWidget element : elements) {
                element.render(poseStack, mouseX, mouseY, partialTick);
            }

            if (isHoveredOrFocused())
                onTooltip.onTooltip(this, poseStack, mouseX, mouseY);
        }
    }

//    @Override
//    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
//        super.renderButton(pPoseStack, pMouseX, pMouseY, pPartialTick);
//    }
}
