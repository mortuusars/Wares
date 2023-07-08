package io.github.mortuusars.wares.client.gui.agreement;

import io.github.mortuusars.wares.data.agreement.Agreement;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class AgreementGUI {
    public static void showAsOverlay(Player player, Supplier<Agreement> agreementSupplier) {
        if (!player.level().isClientSide)
            throw new IllegalStateException("Tried to open Agreement GUI on the server. That's illegal.");

        int containerId = player.containerMenu.containerId + 1;
        AgreementMenu menu = new AgreementMenu(containerId, player.getInventory(), agreementSupplier);
        AgreementScreen screen = new AgreementScreen(menu);

        screen.showAsOverlay();
    }
}
