package io.github.mortuusars.wares.item;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.client.gui.tooltip.AgreementTooltip;
import io.github.mortuusars.wares.data.LangKeys;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AgreementItem extends Item {
    public AgreementItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Optional<DeliveryAgreement> agreementOptional = DeliveryAgreement.fromItemStack(stack);

        agreementOptional.ifPresent(agreement -> {
            if (agreement.getRemaining() <= 0 && agreement.getOrdered() > 0)
                tooltipComponents.add(1, Wares.translate("COMPLETED"));
            else if (level != null && agreement.hasExpirationTime()) {
                MutableComponent expireTooltip = agreement.isExpired(level.getGameTime()) ?
                        Wares.translate(LangKeys.GUI_EXPIRED).withStyle(ChatFormatting.RED)
                        : Wares.translate(LangKeys.GUI_EXPIRES_IN)
                            .append(AgreementTooltip.timeFromTicks(agreement.getExpireTime() - level.getGameTime()))
                        .withStyle(ChatFormatting.DARK_RED);

                tooltipComponents.add(1, expireTooltip);
            }

            if (Screen.hasShiftDown() && isAdvanced.isAdvanced())
                tooltipComponents.add(new TextComponent(agreement.toJsonString()).withStyle(ChatFormatting.GRAY));
        });
    }
}
