package io.github.mortuusars.wares.client.gui.screen.mailbox;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.wares.client.gui.screen.base.ScreenElement;
import io.github.mortuusars.wares.setup.ModItems;
import io.github.mortuusars.wares.setup.ModLangKeys;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

public class TrashCanElement extends ScreenElement<MailboxScreen> {

    private static final Component TOOLTIP_TEXT = new TranslatableComponent(ModLangKeys.GUI_MAILBOX_TRASH_CAN_TOOLTIP);

    public TrashCanElement(MailboxScreen parentScreen, String id, int posX, int posY) {
        super(parentScreen, id, posX, posY, 18, 18);
    }

    @Override
    public void renderTooltip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        screen.renderTooltip(poseStack, TOOLTIP_TEXT, mouseX, mouseY);
    }

    @Override
    public void renderBg(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        int openedCanOffset = !screen.getMenu().getCarried().is(ModItems.PURCHASE_REQUEST.get()) ? 0 : 17;
        screen.blit(poseStack, posX, posY, 177 + openedCanOffset, 1, width, height);
    }
}
