package io.github.mortuusars.wares.network;

import io.github.mortuusars.wares.Wares;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ModPackets {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Wares.ID, "packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);;


    public static void register() {
        CHANNEL.messageBuilder(DeliveredPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeliveredPacket::toBuffer)
                .decoder(DeliveredPacket::fromBuffer)
                .consumer(DeliveredPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
//        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

//    public static class

    public static class DeliveredPacket {
        public final ItemStack agreementStack;

        public DeliveredPacket(ItemStack agreementStack) {
            this.agreementStack = agreementStack;
        }

        public static DeliveredPacket fromBuffer(FriendlyByteBuf buffer) {
            return new DeliveredPacket(buffer.readItem());
        }

        public void toBuffer(FriendlyByteBuf buffer) {
            buffer.writeItem(agreementStack);
        }

        public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {

            NetworkEvent.Context context = contextSupplier.get();

            // Server Side
            ServerPlayer serverPlayer = context.getSender();

            if (serverPlayer == null) {
                Wares.LOGGER.error("Cannot get ServerPlayer from packet context. GUI will not be opened.");
                return false;
            }

//            AgreementItem.openAgreementGuiAndPlaySound(serverPlayer, agreementStack);
            return true;
        }
    }
}