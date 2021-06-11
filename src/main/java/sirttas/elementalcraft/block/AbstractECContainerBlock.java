package sirttas.elementalcraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import sirttas.elementalcraft.inventory.ECInventoryHelper;

public abstract class AbstractECContainerBlock extends AbstractECBlockEntityProviderBlock {

	protected AbstractECContainerBlock() {
		super();
	}

	protected AbstractECContainerBlock(AbstractBlock.Properties properties) {
		super(properties);
	}

	private boolean canInsertStack(IItemHandler inventory, ItemStack stack, ItemStack heldItem, int slot) {
		return ItemHandlerHelper.canItemStacksStack(stack, heldItem) && stack.getCount() < stack.getMaxStackSize() && stack.getCount() < inventory.getSlotLimit(slot);
	}

	public ActionResultType onSlotActivated(IItemHandler inventory, PlayerEntity player, ItemStack heldItem, int slot) {
		ItemStack stack = inventory.getStackInSlot(slot);
		World world = player.getCommandSenderWorld();

		if (heldItem.isEmpty() || player.isShiftKeyDown() || (!stack.isEmpty() && !canInsertStack(inventory, stack, heldItem, slot))) {
			if (!stack.isEmpty()) {
				if (!world.isClientSide()) {
					ItemStack extracted = inventory.extractItem(slot, stack.getCount(), false);

					world.addFreshEntity(new ItemEntity(world, player.getX(), player.getY() + 0.25, player.getZ(), extracted));
				}
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.PASS;
		} else if (stack.isEmpty() && inventory.isItemValid(slot, heldItem)) {
			int size = Math.min(heldItem.getCount(), inventory.getSlotLimit(slot));

			stack = heldItem.copy();
			stack.setCount(size);
			if (!player.isCreative()) {
				heldItem.shrink(size);
			}
			inventory.insertItem(slot, stack, false);
			return ActionResultType.SUCCESS;
		} else if (!stack.isEmpty() && canInsertStack(inventory, stack, heldItem, slot)) {
			int size = Math.min(heldItem.getCount(), inventory.getSlotLimit(slot) - stack.getCount());

			if (!player.isCreative()) {
				heldItem.shrink(size);
			}
			stack.grow(size);
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	protected ActionResultType onSingleSlotActivated(World world, BlockPos pos, PlayerEntity player, Hand hand) {
		final IItemHandler inv = ECInventoryHelper.getItemHandlerAt(world, pos, null);
		ItemStack heldItem = player.getItemInHand(hand);

		if (inv != null && (hand == Hand.MAIN_HAND || !heldItem.isEmpty())) {
			return this.onSlotActivated(inv, player, heldItem, 0);
		}
		return ActionResultType.PASS;
	}

	@Override
	@Deprecated
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	@Deprecated
	public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
		return Container.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
	}
}
