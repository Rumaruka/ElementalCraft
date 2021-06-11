package sirttas.elementalcraft.block;

import javax.annotation.Nonnull;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import sirttas.elementalcraft.block.entity.BlockEntityHelper;
import sirttas.elementalcraft.inventory.ECInventoryHelper;
import sirttas.elementalcraft.item.ECItems;
import sirttas.elementalcraft.property.ECProperties;

public abstract class AbstractECBlockEntityProviderBlock extends Block {

	protected AbstractECBlockEntityProviderBlock(AbstractBlock.Properties properties) {
		super(properties);
	}

	protected AbstractECBlockEntityProviderBlock() {
		this(ECProperties.Blocks.BLOCK_NOT_SOLID);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public abstract TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world);

	@Override
	@Deprecated
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	@Deprecated
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			dropItems(worldIn, pos);
			dropRunes(worldIn, pos);
			super.onRemove(state, worldIn, pos, newState, isMoving);
		}
	}

	private void dropItems(World worldIn, BlockPos pos) {
		IItemHandler inv = ECInventoryHelper.getItemHandlerAt(worldIn, pos, null);

		if (inv != null) {
			for (int i = 0; i < inv.getSlots(); i++) {
				InventoryHelper.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(i));
			}
			worldIn.updateNeighbourForOutputSignal(pos, this);
		}
	}

	private void dropRunes(World worldIn, BlockPos pos) {
		BlockEntityHelper.getRuneHandlerAt(worldIn, pos).getRunes().forEach(rune -> InventoryHelper.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), ECItems.RUNE.getRuneStack(rune)));
	}
}
