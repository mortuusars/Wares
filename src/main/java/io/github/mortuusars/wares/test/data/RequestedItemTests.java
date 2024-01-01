package io.github.mortuusars.wares.test.data;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.data.agreement.component.RequestedItem;
import io.github.mortuusars.wares.data.agreement.component.CompoundTagCompareBehavior;
import io.github.mortuusars.wares.test.framework.ITestClass;
import io.github.mortuusars.wares.test.framework.Test;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
                }),

                new Test("RequestedItemDecodesIgnoreTagMatching", player -> {
                    String json = "{`id`:`minecraft:iron_pickaxe`,`count`:2,`tag`:{`Damage`:5},`TagMatching`:`ignore`}".replace('`', '"');
                    JsonObject obj = GsonHelper.parse(json);
                    RequestedItem decoded = RequestedItem.CODEC.decode(JsonOps.INSTANCE, obj)
                            .resultOrPartial(Wares.LOGGER::error).map(Pair::getFirst).orElse(RequestedItem.EMPTY);

                    assertThat(decoded.getTagCompareBehavior() == CompoundTagCompareBehavior.IGNORE, "TagCompareBehaviour not decoded properly");
                }),

                new Test("RequestedItemDecodesWeakTagMatching", player -> {
                    String json = "{`id`:`minecraft:iron_pickaxe`,`count`:2,`tag`:{`Damage`:5},`TagMatching`:`weak`}".replace('`', '"');
                    JsonObject obj = GsonHelper.parse(json);
                    RequestedItem decoded = RequestedItem.CODEC.decode(JsonOps.INSTANCE, obj)
                            .resultOrPartial(Wares.LOGGER::error).map(Pair::getFirst).orElse(RequestedItem.EMPTY);

                    assertThat(decoded.getTagCompareBehavior() == CompoundTagCompareBehavior.WEAK, "TagCompareBehaviour not decoded properly");
                }),

                new Test("RequestedItemDecodesStrongTagMatching", player -> {
                    String json = "{`id`:`minecraft:iron_pickaxe`,`count`:2,`tag`:{`Damage`:5},`TagMatching`:`strong`}".replace('`', '"');
                    JsonObject obj = GsonHelper.parse(json);
                    RequestedItem decoded = RequestedItem.CODEC.decode(JsonOps.INSTANCE, obj)
                            .resultOrPartial(Wares.LOGGER::error).map(Pair::getFirst).orElse(RequestedItem.EMPTY);

                    assertThat(decoded.getTagCompareBehavior() == CompoundTagCompareBehavior.STRONG, "TagCompareBehaviour not decoded properly");
                }),

                new Test("RequestedItemWithIgnoreTagMatches", player -> {
                    CompoundTag requestedTag = new CompoundTag();
                    requestedTag.putString("Test", "Test");
                    RequestedItem requested = new RequestedItem(Either.right(Items.IRON_PICKAXE), 1, requestedTag, CompoundTagCompareBehavior.IGNORE);

                    assertThat(requested.matches(new ItemStack(Items.IRON_PICKAXE)), String.format("'%s' does not match '%s'.", requested, new ItemStack(Items.IRON_PICKAXE)));

                    ItemStack stack = new ItemStack(Items.IRON_PICKAXE);
                    CompoundTag stackTag = new CompoundTag();
                    stackTag.putString("Test", "Test");
                    stack.setTag(stackTag);

                    assertThat(requested.matches(stack), String.format("'%s' does not match '%s'.", requested, stack));
                }),

                new Test("RequestedItemWithIgnoreTagMatchesWithoutTagSpecified", player -> {
                    RequestedItem requested = new RequestedItem(Either.right(Items.IRON_PICKAXE), 1, null, CompoundTagCompareBehavior.IGNORE);

                    assertThat(requested.matches(new ItemStack(Items.IRON_PICKAXE)), String.format("'%s' does not match '%s'.", requested, new ItemStack(Items.IRON_PICKAXE)));

                    ItemStack stack = new ItemStack(Items.IRON_PICKAXE);
                    CompoundTag stackTag = new CompoundTag();
                    stackTag.putString("Test", "Test");
                    stack.setTag(stackTag);

                    assertThat(requested.matches(stack), String.format("'%s' does not match '%s'.", requested, stack));
                }),

                new Test("RequestedItemWithWeakTagMatches", player -> {
                    CompoundTag requestedTag = new CompoundTag();
                    requestedTag.putInt("Damage", 5);
                    RequestedItem requested = new RequestedItem(Either.right(Items.IRON_PICKAXE), 1, requestedTag, CompoundTagCompareBehavior.WEAK);

                    ItemStack stack = new ItemStack(Items.IRON_PICKAXE);
                    CompoundTag stackTag = new CompoundTag();
                    stackTag.putInt("Damage", 5);
                    stackTag.putString("Test", "Test");
                    stack.setTag(stackTag);

                    assertThat(requested.matches(stack), String.format("'%s' does not match '%s'.", requested, stack));

                    ItemStack stack1 = new ItemStack(Items.IRON_PICKAXE);
                    CompoundTag stackTag1 = new CompoundTag();
                    stackTag1.putInt("Damage", 4);
                    stackTag1.putString("Test", "Test");
                    stack1.setTag(stackTag1);

                    assertThat(!requested.matches(stack1), String.format("'%s' matches when shouldn't '%s'.", requested, stack1));
                    assertThat(!requested.matches(new ItemStack(Items.IRON_PICKAXE)), String.format("'%s' matches when shouldn't '%s'.", requested, stack1));

                    requestedTag = new CompoundTag();
                    requestedTag.putInt("Damage", 5);

                    CompoundTag additional = new CompoundTag();
                    additional.putString("String", "SomeString");
                    ListTag tags = new ListTag();
                    tags.add(StringTag.valueOf("2"));
                    additional.put("List", tags);
                    requestedTag.put("AdditionalTag", additional);

                    requested = new RequestedItem(Either.right(Items.IRON_PICKAXE), 1, requestedTag, CompoundTagCompareBehavior.WEAK);

                    ItemStack stack2 = new ItemStack(Items.IRON_PICKAXE);
                    CompoundTag stackTag2 = new CompoundTag();
                    stackTag2.putInt("Damage", 5);

                    CompoundTag stackTag2Add = new CompoundTag();
                    stackTag2Add.putString("String", "SomeString");
                    ListTag tags2 = new ListTag();
                    tags2.add(StringTag.valueOf("1"));
                    tags2.add(StringTag.valueOf("2"));
                    stackTag2Add.put("List", tags2);
                    stackTag2.put("AdditionalTag", stackTag2Add);

                    stack2.setTag(stackTag2);
                    assertThat(requested.matches(stack2), String.format("'%s' does not match '%s'.", requested, stack2));
                }),

                new Test("RequestedItemWithStrongTagMatches", player -> {
                    CompoundTag requestedTag = new CompoundTag();
                    requestedTag.putInt("Damage", 5);
                    RequestedItem requested = new RequestedItem(Either.right(Items.IRON_PICKAXE), 1, requestedTag, CompoundTagCompareBehavior.STRONG);

                    ItemStack stack = new ItemStack(Items.IRON_PICKAXE);
                    CompoundTag stackTag = new CompoundTag();
                    stackTag.putInt("Damage", 5);
                    stack.setTag(stackTag);

                    assertThat(requested.matches(stack), String.format("'%s' does not match '%s'.", requested, stack));

                    ItemStack stack1 = new ItemStack(Items.IRON_PICKAXE);
                    CompoundTag stackTag1 = new CompoundTag();
                    stackTag1.putInt("Damage", 5);
                    stackTag1.putString("Test", "Test");
                    stack1.setTag(stackTag1);

                    assertThat(!requested.matches(stack1), String.format("'%s' matches when shouldn't '%s'.", requested, stack1));
                })
        );
    }
}
