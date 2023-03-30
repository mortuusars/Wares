package io.github.mortuusars.wares.test.data.agreement;

import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.agreement.Agreement;
import io.github.mortuusars.wares.test.framework.ITestClass;
import io.github.mortuusars.wares.test.framework.Test;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public class AgreementTest implements ITestClass {
    @Override
    public List<Test> collect() {
        return List.of(
            new Test("DefaultAgreementIsNotExpired", player -> {
                Agreement agreement = Agreement.builder()
                        .title(new TextComponent("Title Test"))
                        .addRequestedItem(ItemStack.EMPTY)
                        .build();
                assertThat(!agreement.isExpired(player.level.getGameTime()), "Expired when shouldn't.");
            }),

            new Test("DefaultAgreementIsNotCompleted", player -> {
                Agreement agreement = Agreement.builder()
                        .title(new TextComponent("Title Test"))
                        .addRequestedItem(ItemStack.EMPTY)
                        .build();
                assertThat(!agreement.isCompleted(), "Completed when shouldn't.");
            }),

            new Test("ExpiredAgreementShouldBeExpired", player -> {
                Agreement expiredAgreement = Agreement.builder()
                        .addRequestedItem(ItemStack.EMPTY)
                        .expireTime(player.level.getGameTime() - 5)
                        .build();
                assertThat(expiredAgreement.isExpired(player.level.getGameTime()), "Not expired when it should.");
            }),

            new Test("ExpireMethodExpiresAgreement", player -> {
                Agreement expiredAgreement = Agreement.builder()
                        .addRequestedItem(ItemStack.EMPTY)
                        .build();

                expiredAgreement.expire();

                assertThat(expiredAgreement.isExpired(player.level.getGameTime()), "Not expired when it should.");
            }),

            new Test("OnDeliverCompletedAgreementOnLast", player -> {
                Agreement completed = Agreement.builder()
                        .addRequestedItem(ItemStack.EMPTY)
                        .ordered(10)
                        .delivered(9)
                        .build();

                completed.onDeliver();

                assertThat(completed.isCompleted(), "Not completed when it should.");
            }),

            new Test("OnDeliverCompletesAgreementAfterRequiredNumberOfTimes", player -> {
                Agreement completed = Agreement.builder()
                        .addRequestedItem(ItemStack.EMPTY)
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
                Agreement completed = Agreement.builder()
                        .addRequestedItem(ItemStack.EMPTY)
                        .build();

                completed.complete();

                assertThat(completed.isCompleted(), "Not completed when it should.");
            }),

            new Test("AgreementCodecEncodesAndDecodesCorrectly", player -> {
                Agreement agreement = Agreement.builder()
                        .id("1")
                        .buyerName(new TextComponent("Buyer"))
                        .addRequestedItem(new ItemStack(Items.BAKED_POTATO))
                        .addPaymentItem(new ItemStack(Items.EMERALD))
                        .ordered(99)
                        .delivered(1)
                        .experience(10)
                        .expireTime(10_000)
                        .build();

                ItemStack agreementStack = new ItemStack(Wares.Items.DELIVERY_AGREEMENT.get());
                agreement.toItemStack(agreementStack);

                Optional<Agreement> decoded = Agreement.fromItemStack(agreementStack);

                assertThat(decoded.isPresent(), "Decoding failed and returned Empty.");

                Agreement decodedAgreement = decoded.get();

                boolean agreementsMatch =
                        decodedAgreement.getId().equals(agreement.getId()) &&
                        decodedAgreement.getBuyerName().getString(999).equals(agreement.getBuyerName().getString(999)) &&
                        ItemStack.isSameItemSameTags(decodedAgreement.getRequestedItems().get(0), agreement.getRequestedItems().get(0)) &&
                        ItemStack.isSameItemSameTags(decodedAgreement.getPaymentItems().get(0), agreement.getPaymentItems().get(0)) &&
                        decodedAgreement.getOrdered() == agreement.getOrdered() &&
                        decodedAgreement.getDelivered() == agreement.getDelivered() &&
                        decodedAgreement.getExperience() == agreement.getExperience() &&
                        decodedAgreement.getExpireTime() == agreement.getExpireTime();

                assertThat(agreementsMatch, "Not matching.");
            })
        );
    }
}
