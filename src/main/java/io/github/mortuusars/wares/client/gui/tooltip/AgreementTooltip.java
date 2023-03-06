package io.github.mortuusars.wares.client.gui.tooltip;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.LangKeys;
import net.minecraft.network.chat.MutableComponent;

public class AgreementTooltip {
    public static MutableComponent timeFromTicks(long ticks) {
        long minutes = ticks / 20 / 60;

        if (minutes >= 60) {
            int hours = (int)(minutes / 60);
            return hours >= 24 ?
                    Wares.translate(LangKeys.GUI_TIME_DAYS, hours / 24)
                    : Wares.translate(LangKeys.GUI_TIME_HOURS, hours);
        }
        else {
            return Wares.translate(LangKeys.GUI_TIME_MINUTES, minutes < 1 ? "<1" : minutes);
        }
    }
}
