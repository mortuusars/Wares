package io.github.mortuusars.wares.data.agreement;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class AgreementBuilder {
    private String id = "";
    private Component buyerName = Component.empty();
    private Component buyerAddress = Component.empty();
    private Component title = Component.empty();
    private Component message = Component.empty();
    private String seal = "default";
    private List<ItemStack> requestedItems = new ArrayList<>();
    private List<ItemStack> paymentItems = new ArrayList<>();
    private int orderedQuantity = 0;
    private int delivered = 0;
    private int experience = 0;
    private int deliveryTime = 0;
    private long expireTime = -1;

    public AgreementBuilder id(String id) {
        this.id = id;
        return this;
    }

    public AgreementBuilder buyerName(Component buyerName) {
        this.buyerName = buyerName;
        return this;
    }

    public AgreementBuilder buyerAddress(Component buyerAddress) {
        this.buyerAddress = buyerAddress;
        return this;
    }

    public AgreementBuilder title(Component title) {
        this.title = title;
        return this;
    }

    public AgreementBuilder message(Component message) {
        this.message = message;
        return this;
    }

    public AgreementBuilder seal(String seal) {
        this.seal = seal;
        return this;
    }

    public AgreementBuilder requestedItems(List<ItemStack> requestedItems) {
        this.requestedItems = requestedItems;
        return this;
    }

    public AgreementBuilder addRequestedItem(ItemStack requestedItem) {
        requestedItems.add(requestedItem);
        return this;
    }

    public AgreementBuilder paymentItems(List<ItemStack> paymentItems) {
        this.paymentItems = paymentItems;
        return this;
    }

    public AgreementBuilder addPaymentItem(ItemStack paymentItem) {
        paymentItems.add(paymentItem);
        return this;
    }

    public AgreementBuilder ordered(int orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
        return this;
    }

    public AgreementBuilder delivered(int delivered) {
        this.delivered = delivered;
        return this;
    }

    public AgreementBuilder experience(int experience) {
        this.experience = experience;
        return this;
    }

    public AgreementBuilder deliveryTime(int deliveryTime) {
        this.deliveryTime = deliveryTime;
        return this;
    }

    public AgreementBuilder expireTime(long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public Agreement build() {
        return new Agreement(id, buyerName, buyerAddress, title, message, seal, requestedItems, paymentItems,
                orderedQuantity, delivered, experience, deliveryTime, expireTime, false, false);
    }
}