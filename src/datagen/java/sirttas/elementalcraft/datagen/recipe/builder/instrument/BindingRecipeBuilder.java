package sirttas.elementalcraft.datagen.recipe.builder.instrument;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import sirttas.elementalcraft.ElementalCraft;
import sirttas.elementalcraft.api.element.ElementType;
import sirttas.elementalcraft.api.name.ECNames;
import sirttas.elementalcraft.datagen.recipe.builder.AbstractFinishedRecipe;
import sirttas.elementalcraft.recipe.instrument.binding.AbstractBindingRecipe;
import sirttas.elementalcraft.recipe.instrument.binding.BindingRecipe;

public class BindingRecipeBuilder {
	private final Item result;
	private final List<Ingredient> ingredients = Lists.newArrayList();
	private final ElementType elementType;
	private int elementAmount;
	private final IRecipeSerializer<?> serializer;

	public BindingRecipeBuilder(IRecipeSerializer<?> serializerIn, IItemProvider resultProviderIn, ElementType elementType) {
		this.serializer = serializerIn;
		this.result = resultProviderIn.asItem();
		this.elementType = elementType;
		elementAmount = 2500;
	}

	public static BindingRecipeBuilder bindingRecipe(IItemProvider resultIn, ElementType elementType) {
		return new BindingRecipeBuilder(BindingRecipe.SERIALIZER, resultIn, elementType);
	}

	public BindingRecipeBuilder withElementAmount(int elementAmount) {
		this.elementAmount = elementAmount;
		return this;
	}

	public BindingRecipeBuilder addIngredient(INamedTag<Item> tagIn) {
		return this.addIngredient(Ingredient.of(tagIn));
	}

	public BindingRecipeBuilder addIngredient(IItemProvider itemIn) {
		return this.addIngredient(Ingredient.of(itemIn));
	}

	public BindingRecipeBuilder addIngredient(Ingredient ingredientIn) {
		this.ingredients.add(ingredientIn);
		return this;
	}


	public void build(Consumer<IFinishedRecipe> consumerIn) {
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(this.result);

		this.build(consumerIn, new ResourceLocation(id.getNamespace(), AbstractBindingRecipe.NAME + '/' + id.getPath()));
	}

	public void build(Consumer<IFinishedRecipe> consumerIn, String save) {
		ResourceLocation resourcelocation = ForgeRegistries.ITEMS.getKey(this.result);
		if ((new ResourceLocation(save)).equals(resourcelocation)) {
			throw new IllegalStateException("Binding Recipe " + save + " should remove its 'save' argument");
		} else {
			this.build(consumerIn, ElementalCraft.createRL(AbstractBindingRecipe.NAME + '/' + save));
		}
	}

	public void build(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id) {
		consumerIn.accept(new Result(id, this.serializer, this.ingredients, this.result, elementType, elementAmount));
	}


	public static class Result extends AbstractFinishedRecipe {
		private final List<Ingredient> ingredients;
		private final Item output;
		private final ElementType elementType;
		private final int elementAmount;

		public Result(ResourceLocation id, IRecipeSerializer<?> serializer, List<Ingredient> ingredients, Item resultIn, ElementType elementType, int elementAmount) {
			super(id, serializer);
			this.ingredients = ingredients;
			this.output = resultIn;
			this.elementType = elementType;
			this.elementAmount = elementAmount;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.addProperty(ECNames.ELEMENT_TYPE, this.elementType.getSerializedName());
			json.addProperty(ECNames.ELEMENT_AMOUNT, elementAmount);
			JsonArray jsonarray = new JsonArray();

			for (Ingredient ingredient : this.ingredients) {
				jsonarray.add(ingredient.toJson());
			}

			json.add(ECNames.INGREDIENTS, jsonarray);
			JsonObject outputJson = new JsonObject();
			
			outputJson.addProperty(ECNames.ITEM, ForgeRegistries.ITEMS.getKey(this.output).toString());
			json.add(ECNames.OUTPUT, outputJson);
		}
	}
}
