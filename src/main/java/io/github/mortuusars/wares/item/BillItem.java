package io.github.mortuusars.wares.item;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.bill.Bill;
import net.minecraft.ChatFormatting;
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

public class BillItem extends Item {
    public BillItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Optional<Bill> billOptional = Bill.fromItemStack(stack);

        billOptional.ifPresent(bill -> {
            if (bill.getQuantity() <= 0 && bill.getOrderedQuantity() > 0)
                tooltipComponents.add(1, Wares.translate("COMPLETED"));
            else if (level != null && bill.hasExpirationTime()) {
                MutableComponent expireTooltip = bill.isExpired(level.getGameTime()) ?
                        Wares.translate("EXPIRED").withStyle(ChatFormatting.RED)
                        : Wares.translate("EXPIRES IN " + timeFromTicks(bill.getExpireTime() - level.getGameTime())).withStyle(ChatFormatting.DARK_RED);

                tooltipComponents.add(1, expireTooltip);
            }

            if (isAdvanced.isAdvanced())
                tooltipComponents.add(new TextComponent(bill.toJsonString()).withStyle(ChatFormatting.GRAY));
        });
    }

    private String timeFromTicks(long ticks) {
        long minutes = ticks / 20 / 60;

        if (minutes >= 60) {
            int hours = (int)(minutes / 60);
            return hours >= 24 ? hours / 24 + "d" : hours + "h";
        }
        else {
            return minutes < 1 ? "<1m" : minutes + "m";
        }
    }
}
