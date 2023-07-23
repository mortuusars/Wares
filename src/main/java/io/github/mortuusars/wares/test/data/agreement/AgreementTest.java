package io.github.mortuusars.wares.test.data.agreement;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.agreement.DeliveryAgreement;
import io.github.mortuusars.wares.data.agreement.component.RequestedItem;
import io.github.mortuusars.wares.test.framework.ITestClass;
import io.github.mortuusars.wares.test.framework.Test;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AgreementTest implements ITestClass {
    @Override
    public List<Test> collect() {
        return List.of(
            new Test("DefaultAgreementIsNotExpired", player -> {
                DeliveryAgreement agreement = DeliveryAgreement.builder()
                        .title(Component.literal("Title Test"))
                        .addRequestedItem(RequestedItem.EMPTY)
                        .build();
                assertThat(!agreement.isExpired(player.level().getGameTime()), "Expired when shouldn't.");
            }),

            new Test("DefaultAgreementIsNotCompleted", player -> {
                DeliveryAgreement agreement = DeliveryAgreement.builder()
                        .title(Component.literal("Title Test"))
                        .addRequestedItem(RequestedItem.EMPTY)
                        .build();
                assertThat(!agreement.isCompleted(), "Completed when shouldn't.");
            }),

            new Test("ExpiredAgreementShouldBeExpired", player -> {
                DeliveryAgreement expiredAgreement = DeliveryAgreement.builder()
                        .addRequestedItem(RequestedItem.EMPTY)
                        .expireTime(player.level().getGameTime() - 5)
                        .build();
                assertThat(expiredAgreement.isExpired(player.level().getGameTime()), "Not expired when it should.");
            }),

            new Test("ExpireMethodExpiresAgreement", player -> {
                DeliveryAgreement expiredAgreement = DeliveryAgreement.builder()
                        .addRequestedItem(RequestedItem.EMPTY)
                        .build();

                expiredAgreement.expire();

                assertThat(expiredAgreement.isExpired(player.level().getGameTime()), "Not expired when it should.");
            }),

            new Test("OnDeliverCompletedAgreementOnLast", player -> {
                DeliveryAgreement completed = DeliveryAgreement.builder()
                        .addRequestedItem(RequestedItem.EMPTY)
                        .ordered(10)
                        .delivered(9)
                        .build();

                completed.onDeliver();

                assertThat(completed.isCompleted(), "Not completed when it should.");
            }),

            new Test("OnDeliverCompletesAgreementAfterRequiredNumberOfTimes", player -> {
                DeliveryAgreement completed = DeliveryAgreement.builder()
                        .addRequestedItem(RequestedItem.EMPTY)
                        .ordered(10)
                        .delivered(5)
                        .build();

                for (int i = 0; i < 5; i++) {
                    assertThat(!completed.isCompleted(), "Completed when it shouldn't.");
                    completed.onDeliver();
                }

                assertThat(completed.isCompleted(), "Not completed when it should.");
            }),

            new Test("CompleteMethodCompletesAgreement", player -> {
                DeliveryAgreement completed = DeliveryAgreement.builder()
                        .addRequestedItem(RequestedItem.EMPTY)
                        .build();

                completed.complete();

                assertThat(completed.isCompleted(), "Not completed when it should.");
            }),

            new Test("AgreementCodecEncodesAndDecodesCorrectly", player -> {
                DeliveryAgreement agreement = DeliveryAgreement.builder()
                        .id("1")
                        .buyerName(Component.literal("Buyer"))
                        .addRequestedItem(new RequestedItem(Items.BAKED_POTATO, 1))
                        .addPaymentItem(new ItemStack(Items.EMERALD))
                        .ordered(99)
                        .delivered(1)
                        .experience(10)
                        .expireTime(10_000)
                        .build();

                ItemStack agreementStack = new ItemStack(Wares.Items.DELIVERY_AGREEMENT.get());
                agreement.toItemStack(agreementStack);

                Optional<DeliveryAgreement> decoded = DeliveryAgreement.fromItemStack(agreementStack);

                assertThat(decoded.isPresent(), "Decoding failed and returned Empty.");

                @SuppressWarnings("OptionalGetWithoutIsPresent") DeliveryAgreement decodedAgreement = decoded.get();

                RequestedItem[] decodedReq = decodedAgreement.getRequested().toArray(RequestedItem[]::new);
                Arrays.sort(decodedReq);
                RequestedItem[] agreementReq = agreement.getRequested().toArray(RequestedItem[]::new);
                Arrays.sort(agreementReq);

                ItemStack[] decodedPay = decodedAgreement.getPayment().toArray(ItemStack[]::new);
                Arrays.sort(decodedPay);
                ItemStack[] agreementPay = agreement.getPayment().toArray(ItemStack[]::new);
                Arrays.sort(agreementPay);

                boolean requestedItemsEqual = decodedReq.length == agreementReq.length;
                for (int i = 0; i < decodedReq.length; i++) {
                    if (!decodedReq[i].equals(agreementReq[i])) {
                        requestedItemsEqual = false;
                        break;
                    }
                }

                boolean paymentItemsEqual = decodedPay.length == agreementPay.length;
                for (int i = 0; i < decodedPay.length; i++) {
                    if (!ItemStack.isSameItemSameTags(agreementPay[i], decodedPay[i])) {
                        paymentItemsEqual = false;
                        break;
                    }
                }

                boolean agreementsMatch =
                        decodedAgreement.getId().equals(agreement.getId()) &&
                        decodedAgreement.getBuyerName().getString(999).equals(agreement.getBuyerName().getString(999)) &&
                        requestedItemsEqual &&
                        paymentItemsEqual &&
                        decodedAgreement.getOrdered() == agreement.getOrdered() &&
                        decodedAgreement.getDelivered() == agreement.getDelivered() &&
                        decodedAgreement.getExperience() == agreement.getExperience() &&
                        decodedAgreement.getExpireTimestamp() == agreement.getExpireTimestamp();

                assertThat(agreementsMatch, "Not matching.");
            })
        );
    }
}
