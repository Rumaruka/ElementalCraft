package sirttas.elementalcraft.block.entity;

import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import sirttas.elementalcraft.block.retriever.RetrieverBlock;
import sirttas.elementalcraft.recipe.IInventoryTileRecipe;

public abstract class AbstractECCraftingBlockEntity<T extends ICraftingBlockEntity, R extends IInventoryTileRecipe<T>> extends AbstractECContainerBlockEntity implements ICraftingBlockEntity {

	protected final IRecipeType<R> recipeType;
	protected final int transferSpeed;

	protected IInventoryTileRecipe<T> recipe;
	protected int outputSlot = 0;
	
	protected AbstractECCraftingBlockEntity(TileEntityType<?> tileEntityTypeIn, IRecipeType<R> recipeType, int transferSpeed) {
		super(tileEntityTypeIn);
		this.recipeType = recipeType;
		this.transferSpeed = transferSpeed;
	}

	@Override
	public boolean isRecipeAvailable() {
		if (recipe != null && recipe.matches(cast())) {
			return true;
		}
		if (!this.getInventory().isEmpty()) {
			recipe = this.lookupRecipe();
			if (recipe != null) {
				this.setChanged();
				return true;
			}
		}
		return false;
	}

	@Override
	public void process() {
		if (!level.isClientSide) {
			recipe.process(cast());
			RetrieverBlock.sendOutputToRetriever(level, worldPosition, getInventory(), outputSlot);
		}
		recipe = null;
		this.setChanged();
	}

	protected IInventoryTileRecipe<T> lookupRecipe() {
		return lookupRecipe(this.getLevel(), recipeType);
	}

	@Override
	public void clearContent() {
		super.clearContent();
		recipe = null;
	}
}
