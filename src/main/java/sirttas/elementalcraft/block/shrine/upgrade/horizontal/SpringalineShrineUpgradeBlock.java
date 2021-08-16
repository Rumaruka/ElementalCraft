package sirttas.elementalcraft.block.shrine.upgrade.horizontal;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import sirttas.elementalcraft.ElementalCraft;
import sirttas.elementalcraft.block.shrine.budding.BuddingShrineBlock;
import sirttas.elementalcraft.block.shrine.budding.BuddingShrineBlock.CrystalType;

public class SpringalineShrineUpgradeBlock extends AbstractHorizontalShrineUpgradeBlock {

	public static final String NAME = "shrine_upgrade_springaline";

	private static final VoxelShape BASE_NORTH = Block.box(5D, 5D, 4D, 11D, 11D, 10D);
	private static final VoxelShape PLATE_SOUTH_NORTH = Block.box(6D, 6D, 10D, 10D, 10D, 11D);
	private static final VoxelShape PLATE_UP_NORTH = Block.box(6D, 11D, 5D, 10D, 12D, 9D);
	private static final VoxelShape PLATE_DOWN_NORTH = Block.box(6D, 4D, 5D, 10D, 5D, 9D);
	private static final VoxelShape PLATE_WEST_NORTH = Block.box(4D, 6D, 5D, 5D, 10D, 9D);
	private static final VoxelShape PLATE_EAST_NORTH = Block.box(11D, 6D, 5D, 12D, 10D, 9D);
	private static final VoxelShape PIPE_NORTH = Block.box(7D, 7D, 0D, 9D, 9D, 4D);
	private static final VoxelShape SHAPE_NORTH = Shapes.or(BASE_NORTH, PIPE_NORTH, PLATE_SOUTH_NORTH, PLATE_UP_NORTH, PLATE_DOWN_NORTH, PLATE_WEST_NORTH, PLATE_EAST_NORTH);

	private static final VoxelShape BASE_SOUTH = Block.box(5D, 5D, 6D, 11D, 11D, 12D);
	private static final VoxelShape PLATE_NORTH_SOUTH = Block.box(6D, 6D, 5D, 10D, 10D, 6D);
	private static final VoxelShape PLATE_UP_SOUTH = Block.box(6D, 11D, 7D, 10D, 12D, 11D);
	private static final VoxelShape PLATE_DOWN_SOUTH = Block.box(6D, 4D, 7D, 10D, 5D, 11D);
	private static final VoxelShape PLATE_WEST_SOUTH = Block.box(4D, 6D, 7D, 5D, 10D, 11D);
	private static final VoxelShape PLATE_EAST_SOUTH = Block.box(11D, 6D, 7D, 12D, 10D, 11D);
	private static final VoxelShape PIPE_SOUTH = Block.box(7D, 7D, 12D, 9D, 9D, 16D);
	private static final VoxelShape SHAPE_SOUTH = Shapes.or(BASE_SOUTH, PIPE_SOUTH, PLATE_NORTH_SOUTH, PLATE_UP_SOUTH, PLATE_DOWN_SOUTH, PLATE_WEST_SOUTH, PLATE_EAST_SOUTH);

	private static final VoxelShape BASE_WEST = Block.box(4D, 5D, 5D, 10D, 11D, 11D);
	private static final VoxelShape PLATE_EAST_WEST = Block.box(10D, 6D, 6D, 11D, 10D, 10D);
	private static final VoxelShape PLATE_UP_WEST = Block.box(5D, 11D, 6D, 9D, 12D, 10D);
	private static final VoxelShape PLATE_DOWN_WEST = Block.box(5D, 4D, 6D, 9D, 5D, 10D);
	private static final VoxelShape PLATE_NORTH_WEST = Block.box(5D, 6D, 4D, 9D, 10D, 5D);
	private static final VoxelShape PLATE_SOUTH_WEST = Block.box(5D, 6D, 11D, 9D, 10D, 12D);
	private static final VoxelShape PIPE_WEST = Block.box(0D, 7D, 7D, 4D, 9D, 9D);
	private static final VoxelShape SHAPE_WEST = Shapes.or(BASE_WEST, PIPE_WEST, PLATE_EAST_WEST, PLATE_UP_WEST, PLATE_DOWN_WEST, PLATE_NORTH_WEST, PLATE_SOUTH_WEST);

	private static final VoxelShape BASE_EAST = Block.box(6D, 5D, 5D, 12D, 11D, 11D);
	private static final VoxelShape PLATE_WEST_EAST = Block.box(5D, 6D, 6D, 6D, 10D, 10D);
	private static final VoxelShape PLATE_UP_EAST = Block.box(7D, 11D, 6D, 11D, 12D, 10D);
	private static final VoxelShape PLATE_DOWN_EAST = Block.box(7D, 4D, 6D, 11D, 5D, 10D);
	private static final VoxelShape PLATE_NORTH_EAST = Block.box(7D, 6D, 4D, 11D, 10D, 5D);
	private static final VoxelShape PLATE_SOUTH_EAST = Block.box(7D, 6D, 11D, 11D, 10D, 12D);
	private static final VoxelShape PIPE_EAST = Block.box(12D, 7D, 7D, 16D, 9D, 9D);
	private static final VoxelShape SHAPE_EAST = Shapes.or(BASE_EAST, PIPE_EAST,PLATE_WEST_EAST, PLATE_UP_EAST, PLATE_DOWN_EAST, PLATE_NORTH_EAST, PLATE_SOUTH_EAST);

	public SpringalineShrineUpgradeBlock() {
		super(ElementalCraft.createRL(NAME));
	}
	
	public static VoxelShape getShape(BlockState state) {
		switch (state.getValue(FACING)) {
		case EAST:
			return SHAPE_EAST;
		case NORTH:
			return SHAPE_NORTH;
		case SOUTH:
			return SHAPE_SOUTH;
		case WEST:
			return SHAPE_WEST;
		default:
			return SHAPE_NORTH;
		}
	}
	
	@Override
	@Deprecated
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return getShape(state);
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		setShrineState(level, pos.relative(getFacing(state)), CrystalType.SPRINGALINE);
	}


	
	@Override
	@Deprecated
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		super.onRemove(state, level, pos, newState, isMoving);
		setShrineState(level, pos.relative(getFacing(state)), CrystalType.AMETHYST);
	}
	
	private void setShrineState(Level level, BlockPos pos, CrystalType type) {
		level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(BuddingShrineBlock.CRYSTAL_TYPE, type));
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(new TranslatableComponent("tooltip.elementalcraft.springaline_shrine_upgrade").withStyle(ChatFormatting.BLUE));
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}
}
