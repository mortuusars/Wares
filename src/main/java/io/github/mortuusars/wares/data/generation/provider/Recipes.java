package io.github.mortuusars.wares.data.generation.provider;

import io.github.mortuusars.wares.Wares;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes(DataGenerator generator) {
        super(generator.getPackOutput());
    }


    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> recipeConsumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Wares.Items.DELIVERY_TABLE.get())
                .unlockedBy("has_ink_sac", has(Items.INK_SAC))
                .unlockedBy("has_feather", has(Tags.Items.FEATHERS))
                .pattern("IF ")
                .pattern("WW ")
                .pattern("WW ")
                .define('I', Items.INK_SAC)
                .define('F', Tags.Items.FEATHERS)
                .define('W', ItemTags.PLANKS)
                .save(recipeConsumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Wares.Items.CARDBOARD_BOX.get(), 2)
                .unlockedBy("has_paper", has(Items.PAPER))
                .pattern("PP ")
                .pattern("PP ")
                .define('P', Items.PAPER)
                .save(recipeConsumer);
    }
}
