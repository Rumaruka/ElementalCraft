package sirttas.elementalcraft.block.instrument.io;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import sirttas.elementalcraft.block.instrument.AbstractInstrumentBlockEntity;
import sirttas.elementalcraft.block.instrument.IInstrument;
import sirttas.elementalcraft.recipe.instrument.io.IIOInstrumentRecipe;

public abstract class AbstractIOInstrumentBlockEntity<T extends IInstrument, R extends IIOInstrumentRecipe<T>> extends AbstractInstrumentBlockEntity<T, R> {

	protected AbstractIOInstrumentBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, RecipeType<R> recipeType, int transferSpeed, int maxRunes) {
		super(blockEntityType, pos, state, recipeType, transferSpeed, maxRunes);
		outputSlot = 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void assemble() {
		T self = (T) this;
		Container inv = getInventory();
		ItemStack in = inv.getItem(0);
		ItemStack result = inv.getItem(1);
		ItemStack craftingResult = recipe.assemble(self);
		int luck = recipe.getLuck(self);

		if (craftingResult.sameItem(result) && result.getCount() + craftingResult.getCount() <= result.getMaxStackSize()) {
			in.shrink(1);
			result.grow(craftingResult.getCount());
		} else if (result.isEmpty()) {
			in.shrink(1);
			inv.setItem(1, craftingResult.copy());
		}
		if (luck > 0 && recipe.getRand(self).nextInt(100) < luck) {
			result.grow(1);
		}
		if (in.isEmpty()) {
			inv.removeItemNoUpdate(0);
		}
	}
	
}
