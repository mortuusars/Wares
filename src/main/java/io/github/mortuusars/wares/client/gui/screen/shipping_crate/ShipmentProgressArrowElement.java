package io.github.mortuusars.wares.client.gui.screen.shipping_crate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.wares.client.gui.screen.base.ScreenElement;
import io.github.mortuusars.wares.client.gui.screen.util.Cursor;
import io.github.mortuusars.wares.core.ware.data.Ware;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ShipmentProgressArrowElement extends ScreenElement<ShippingCrateScreen> {

    private final static int TEXTURE_X = 0;
    private final static int TEXTURE_Y = 231;
    private final static int WIDTH = 23;
    private final static int HEIGHT = 17;

    private final Ware ware;

    public ShipmentProgressArrowElement(ShippingCrateScreen parentScreen, String id, int posX, int posY, Ware ware) {
        super(parentScreen, id, posX, posY, WIDTH, HEIGHT);
        this.ware = ware;
    }

    @Override
    public Cursor getCursor() {
        return isProgressFull() ? Cursor.HAND : Cursor.ARROW;
    }

    @Override
    public void renderBg(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Pair<Integer, Integer> fillProgress = screen.getMenu().getProgress();
        float progress = fillProgress.getFirst() == 0 ? 0f : fillProgress.getSecond() / (float)fillProgress.getFirst();
        int progressPixels = (int)(progress * 24);

        if (progress >= 1f){
            if (isMouseOver(mouseX, mouseY))
                screen.blit(poseStack, posX, posY, TEXTURE_X + WIDTH * 2, TEXTURE_Y, WIDTH, HEIGHT);
            else
                screen.blit(poseStack, posX, posY, TEXTURE_X + WIDTH, TEXTURE_Y, WIDTH, HEIGHT);
        }
        else
            screen.blit(poseStack, posX, posY, TEXTURE_X, TEXTURE_Y, progressPixels, HEIGHT);
    }

    @Override
    public void renderTooltip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        Pair<Integer, Integer> progress = screen.getMenu().getProgress();
        ArrayList<Component> tooltipComponents = new ArrayList<>();

//        if (ware == null)
//            tooltipComponents.add(new TextComponent(Math.min(progress.getFirst(), progress.getSecond()) + " / " + progress.getFirst()));
//        else {
//            for (var reqItem : ware.requestedItems){
//                Component itemComponent = reqItem.isTag() ?
//                        new TextComponent("#" + reqItem.tag)
//                        : reqItem.getItem().orElse(Items.AIR).getName(ItemStack.EMPTY);
//
//                int currentCount = 0;
//                for (int i = 0; i < ShippingCrate.SLOTS; i++) {
//                    ItemStack stack = screen.getMenu().slots.get(i).getItem();
//                    if (reqItem.matches(stack))
//                        currentCount += stack.getCount();
//                }
//
//                ((BaseComponent) itemComponent).append(new TextComponent(" " + currentCount + "/" + reqItem.count)
//                        .withStyle(reqItem.count > currentCount ? ChatFormatting.DARK_RED : ChatFormatting.DARK_GREEN));
//
//                tooltipComponents.add(itemComponent);
//            }
//        }
//
//        if (progress.getSecond() >= progress.getFirst()){
//            tooltipComponents.add(new TextComponent(""));
//            tooltipComponents.add(new TranslatableComponent("gui.shipping_crate.ready_to_ship").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD));
//        }
//
//        screen.renderTooltip(poseStack, tooltipComponents, Optional.empty(), mouseX, mouseY, screen.getFontRenderer());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isProgressFull()){
            LocalPlayer player = screen.getMinecraft().player;
            player.level.playSound(player, player.position().x, player.position().y, player.position().z, SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, 0.5f, 1f);
            screen.getMinecraft().gameMode.handleInventoryButtonClick(screen.getMenu().containerId, ShippingCrateScreen.SHIP_WARE_BUTTON_ID);
        }
    }

    private boolean isProgressFull(){
        Pair<Integer, Integer> progress = screen.getMenu().getProgress();
        return progress.getSecond() >= progress.getFirst();
    }
}
