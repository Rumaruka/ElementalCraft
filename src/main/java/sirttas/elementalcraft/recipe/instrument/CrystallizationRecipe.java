package sirttas.elementalcraft.recipe.instrument;

import java.util.List;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.ObjectHolder;
import sirttas.dpanvil.api.codec.CodecHelper;
import sirttas.elementalcraft.ElementalCraft;
import sirttas.elementalcraft.api.ElementalCraftApi;
import sirttas.elementalcraft.api.element.ElementType;
import sirttas.elementalcraft.api.name.ECNames;
import sirttas.elementalcraft.api.rune.Rune.BonusType;
import sirttas.elementalcraft.block.evaporator.EvaporatorBlock;
import sirttas.elementalcraft.block.instrument.crystallizer.CrystallizerBlockEntity;
import sirttas.elementalcraft.config.ECConfig;
import sirttas.elementalcraft.item.elemental.ShardItem;
import sirttas.elementalcraft.recipe.RecipeHelper;

public class CrystallizationRecipe extends AbstractInstrumentRecipe<CrystallizerBlockEntity> {

	public static final String NAME = "crystallization";
	public static final RecipeType<CrystallizationRecipe> TYPE = Registry.register(Registry.RECIPE_TYPE, ElementalCraft.createRL(NAME), new RecipeType<CrystallizationRecipe>() {
		@Override
		public String toString() {
			return NAME;
		}
	});
	@ObjectHolder(ElementalCraftApi.MODID + ":" + NAME) public static final RecipeSerializer<CrystallizationRecipe> SERIALIZER = null;
	
	private final NonNullList<Ingredient> ingredients;
	private final List<ResultEntry> outputs;
	private final int elementAmount;

	public CrystallizationRecipe(ResourceLocation id, ElementType type, int elementAmount, List<ResultEntry> outputs, List<Ingredient> ingredients) {
		super(id, type);
		this.ingredients = NonNullList.of(Ingredient.EMPTY, ingredients.stream().toArray(s -> new Ingredient[s]));
		this.outputs = ImmutableList.copyOf(outputs);
		this.elementAmount = elementAmount;
	}

	@Override
	public int getElementAmount() {
		return elementAmount;
	}

	@Override
	public boolean matches(CrystallizerBlockEntity crystallizer) {
		if (crystallizer.getContainerElementType() == getElementType() && crystallizer.getItemCount() >= 2) {
			for (int i = 0; i < 2; i++) {
				if (!ingredients.get(i).test(crystallizer.getInventory().getItem(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}
	
	public List<ResultEntry> getOutputs() {
		return outputs;
	}

	@Override
	public RecipeType<?> getType() {
		return TYPE;
	}

	@Override
	public void process(CrystallizerBlockEntity instrument) {
		int luck = (int) Math.round(instrument.getRuneHandler().getBonus(BonusType.LUCK) * ECConfig.COMMON.crystallizerLuckRatio.get());
		Container inv = instrument.getInventory();
		
		for (int i = 2; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			
			if (EvaporatorBlock.getShardElementType(stack) == this.elementType) {
				luck += ((ShardItem) stack.getItem()).getElementAmount();
			}
		}
		instrument.clearContent();
		instrument.getInventory().setItem(0, this.getCraftingResult(instrument, luck));
	}

	@Override
	public ItemStack getCraftingResult(CrystallizerBlockEntity instrument) {
		return getCraftingResult(instrument, 0);
	}

	@SuppressWarnings("resource")
	public ItemStack getCraftingResult(CrystallizerBlockEntity instrument, float luck) {
		ItemStack gem = instrument.getInventory().getItem(0);
		int index = IntStream.range(0, outputs.size()).filter(i -> ItemHandlerHelper.canItemStacksStack(outputs.get(i).result, gem)).findFirst().orElse(0);
		int weight = getTotalWeight(outputs.subList(index, outputs.size()), luck);
		int roll = Math.min(instrument.getLevel().random.nextInt(weight), weight - 1);
		
		for (ResultEntry entry : outputs) {
			roll -= entry.getEffectiveWeight(luck) + luck;
			if (roll < 0) {
				return entry.getResult();
			}
		}
		return ItemStack.EMPTY;
	}

	public int getTotalWeight() {
		return getTotalWeight(outputs, 0);
	}
	
	public int getTotalWeight(List<ResultEntry> list, float luck) {
		return list.stream().mapToInt(result -> result.getEffectiveWeight(luck)).sum();
	}
	
	public float getWeight(ItemStack stack) {
		return outputs.stream().filter(r -> ItemHandlerHelper.canItemStacksStack(r.result, stack)).findAny().map(r -> r.weight).orElse(0F);
	}
	
	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	public static ResultEntry createResult(ItemStack result, float weight) {
		return createResult(result, weight, 1);
	}
	
	public static ResultEntry createResult(ItemStack result, float weight, float quality) {
		return new ResultEntry(result, weight, quality);
	}
	
	public static class ResultEntry {
		public static final Codec<ResultEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				ItemStack.CODEC.fieldOf(ECNames.RESULT).forGetter(r -> r.result),
				Codec.FLOAT.fieldOf(ECNames.WEIGHT).forGetter(r -> r.weight),
				Codec.FLOAT.optionalFieldOf(ECNames.QUALITY, 1F).forGetter(r -> r.quality)
		).apply(builder, ResultEntry::new));
		public static final Codec<List<ResultEntry>> LIST_CODEC = CODEC.listOf();
		
		private final float weight;
		private final float quality;
		private final ItemStack result;
		
		private ResultEntry(ItemStack result, float weight, float quality) {
			this.result = result;
			this.weight = weight;
			this.quality = quality;
		}
		
		public ItemStack getResult() {
			return result.copy();
		}
		
		public int getEffectiveWeight(float luck) {
			return Math.max(Mth.floor(weight + quality * luck), 0);
		}
	}
	
	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<CrystallizationRecipe> {

		private static final Codec<List<ResultEntry>> OUTPUT_CODEC = ResultEntry.LIST_CODEC.fieldOf(ECNames.OUTPUTS).codec();

		@Override
		public CrystallizationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			ElementType type = ElementType.byName(GsonHelper.getAsString(json, ECNames.ELEMENT_TYPE));
			int elementAmount = GsonHelper.getAsInt(json, ECNames.ELEMENT_AMOUNT);
			NonNullList<Ingredient> ingredients = readIngredients(GsonHelper.getAsJsonObject(json, ECNames.INGREDIENTS));
			List<ResultEntry> outputs = CodecHelper.decode(ResultEntry.LIST_CODEC, json.get(ECNames.OUTPUTS));
			
			return new CrystallizationRecipe(recipeId, type, elementAmount, outputs, ingredients);
		}

		public static NonNullList<Ingredient> readIngredients(JsonObject json) {
			NonNullList<Ingredient> list = NonNullList.create();

			list.add(RecipeHelper.deserializeIngredient(json, "gem"));
			list.add(RecipeHelper.deserializeIngredient(json, "crystal"));
			list.add(RecipeHelper.deserializeIngredient(json, "shard"));
			return list;
		}

		@Override
		public CrystallizationRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			ElementType type = ElementType.byName(buffer.readUtf());
			int elementAmount = buffer.readInt();
			List<ResultEntry> outputs = CodecHelper.decode(OUTPUT_CODEC, buffer);
			
			int i = buffer.readInt();
			NonNullList<Ingredient> ingredients = NonNullList.withSize(i, Ingredient.EMPTY);

			for (int j = 0; j < i; ++j) {
				ingredients.set(j, Ingredient.fromNetwork(buffer));
			}

			return new CrystallizationRecipe(recipeId, type, elementAmount, outputs, ingredients);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, CrystallizationRecipe recipe) {
			buffer.writeUtf(recipe.getElementType().getSerializedName());
			buffer.writeInt(recipe.getElementAmount());
			CodecHelper.encode(OUTPUT_CODEC, recipe.outputs, buffer);
			buffer.writeInt(recipe.getIngredients().size());
			recipe.getIngredients().forEach(ingredient -> ingredient.toNetwork(buffer));
		}
	}
}
