package io.github.mortuusars.wares.test.data;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.agreement.component.RequestedItem;
import io.github.mortuusars.wares.test.framework.ITestClass;
import io.github.mortuusars.wares.test.framework.Test;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class RequestedItemTests implements ITestClass {
    @Override
    public List<Test> collect() {
        return List.of(
                new Test("RequestedItemEqualsProperly", player -> {
                    RequestedItem requestedItem1 = new RequestedItem(Items.EMERALD, 2);
                    RequestedItem requestedItem2 = new RequestedItem(Items.EMERALD, 2);

                    assertThat(requestedItem1.equals(requestedItem2) && requestedItem2.equals(requestedItem1),
                            String.format("'%s' and '%s' is not equal.", requestedItem1, requestedItem2));
                }),

                new Test("RequestedItemMatchesItemStack", player -> {
                    RequestedItem requestedItem = new RequestedItem(Items.EMERALD, 2);
                    ItemStack stack = new ItemStack(Items.EMERALD, 2);

                    assertThat(requestedItem.matches(stack),
                            String.format("'%s' does not match '%s'.", requestedItem, stack));
                }),

                new Test("RequestedItemMatchesItemStackWithTag", player -> {
                    CompoundTag requestTag = new CompoundTag();
                    requestTag.putString("Test", "Test");
                    RequestedItem requestedItem = new RequestedItem(Either.right(Items.EMERALD), 2, requestTag);

                    ItemStack stack = new ItemStack(Items.EMERALD, 2);
                    CompoundTag stackTag = new CompoundTag();
                    stackTag.putString("Test", "Test");
                    stack.setTag(stackTag);

                    assertThat(requestedItem.matches(stack),
                            String.format("'%s' does not match '%s'.", requestedItem, stack));
                }),

                new Test("RequestedItemDecodesItemFromJsonProperly", player -> {
                    String json = "{`id`:`minecraft:emerald`,`count`:2,`tag`:{`Test`:`Test`}}".replace('`', '"');
                    JsonObject obj = GsonHelper.parse(json);
                    RequestedItem decoded = RequestedItem.CODEC.decode(JsonOps.INSTANCE, obj)
                            .resultOrPartial(Wares.LOGGER::error).map(Pair::getFirst).orElse(RequestedItem.EMPTY);

                    ItemStack stack = new ItemStack(Items.EMERALD, 2);
                    CompoundTag stackTag = new CompoundTag();
                    stackTag.putString("Test", "Test");
                    stack.setTag(stackTag);

                    assertThat(decoded.matches(stack),
                            String.format("'%s' does not match '%s'.", decoded, stack));
                }),

                new Test("RequestedItemDecodesTagFromJsonProperly", player -> {
                    String json = "{`id`:`#minecraft:logs`,`count`:2,`tag`:{`Test`:`Test`}}".replace('`', '"');
                    JsonObject obj = GsonHelper.parse(json);
                    RequestedItem decoded = RequestedItem.CODEC.decode(JsonOps.INSTANCE, obj)
                            .resultOrPartial(Wares.LOGGER::error).map(Pair::getFirst).orElse(RequestedItem.EMPTY);

                    assertThat(decoded.getTagOrItem().left().isPresent(),  decoded + " is not a tag.");

                    ItemStack stack = new ItemStack(Items.OAK_LOG, 2);
                    CompoundTag stackTag = new CompoundTag();
                    stackTag.putString("Test", "Test");
                    stack.setTag(stackTag);

                    assertThat(decoded.matches(stack),
                            String.format("'%s' does not match '%s'.", decoded, stack));
                })
        );
    }
}
