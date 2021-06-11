package sirttas.elementalcraft.block.pureinfuser.pedestal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import sirttas.elementalcraft.api.element.ElementType;
import sirttas.elementalcraft.api.element.IElementTypeProvider;
import sirttas.elementalcraft.block.AbstractECContainerBlock;
import sirttas.elementalcraft.block.pipe.IPipeConnectedBlock;

public class PedestalBlock extends AbstractECContainerBlock implements IElementTypeProvider, IPipeConnectedBlock {

	private static final VoxelShape BASE_1 = Block.box(0D, 0D, 0D, 16D, 3D, 16D);
	private static final VoxelShape BASE_2 = Block.box(2D, 3D, 2D, 14D, 9D, 14D);
	private static final VoxelShape BASE_3 = Block.box(0D, 9D, 0D, 16D, 12D, 16D);

	private static final VoxelShape CONNECTOR_NORTH = Block.box(7D, 7D, 0D, 9D, 9D, 2D);
	private static final VoxelShape CONNECTOR_SOUTH = Block.box(7D, 7D, 14D, 9D, 9D, 16D);
	private static final VoxelShape CONNECTOR_WEST = Block.box(0D, 7D, 7D, 2D, 9D, 9D);
	private static final VoxelShape CONNECTOR_EAST = Block.box(14D, 7D, 7D, 16D, 9D, 9D);

	private static final VoxelShape BASE = VoxelShapes.or(BASE_1, BASE_2, BASE_3);
	private static final VoxelShape AIR = VoxelShapes.or(Block.box(5D, 0D, 5D, 11D, 3D, 11D), BASE_2, BASE_3);
	private static final VoxelShape EARTH = VoxelShapes.or(BASE, Block.box(4D, 3D, 0D, 12D, 8D, 16D), Block.box(0D, 3D, 4D, 16D, 8D, 12D));

	public static final String NAME = "pedestal";
	public static final String NAME_FIRE = NAME + "_fire";
	public static final String NAME_WATER = NAME + "_water";
	public static final String NAME_EARTH = NAME + "_earth";
	public static final String NAME_AIR = NAME + "_air";

	private ElementType elementType;

	public PedestalBlock(ElementType type) {
		elementType = type;
		this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return this.getElementType() != ElementType.NONE;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		switch (this.getElementType()) {
		case AIR:
			return PedestalBlockEntity.createAir();
		case EARTH:
			return PedestalBlockEntity.createEarth();
		case FIRE:
			return PedestalBlockEntity.createFire();
		case WATER:
			return PedestalBlockEntity.createWater();
		default:
			return null;
		}
	}

	@Override
	@Deprecated
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		return onSingleSlotActivated(world, pos, player, hand);
	}

	@Override
	@Deprecated
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		VoxelShape shape = getBaseShape();

		if (Boolean.TRUE.equals(state.getValue(NORTH))) {
			shape = VoxelShapes.or(shape, CONNECTOR_NORTH);
		}
		if (Boolean.TRUE.equals(state.getValue(SOUTH))) {
			shape = VoxelShapes.or(shape, CONNECTOR_SOUTH);
		}
		if (Boolean.TRUE.equals(state.getValue(EAST))) {
			shape = VoxelShapes.or(shape, CONNECTOR_EAST);
		}
		if (Boolean.TRUE.equals(state.getValue(WEST))) {
			shape = VoxelShapes.or(shape, CONNECTOR_WEST);
		}
		return shape;
	}

	private VoxelShape getBaseShape() {
		if (elementType == ElementType.AIR) {
			return AIR;
		} else if (elementType == ElementType.EARTH) {
			return EARTH;
		}
		return BASE;
	}

	@Override
	public ElementType getElementType() {
		return elementType;
	}
	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> container) {
		container.add(NORTH, SOUTH, EAST, WEST);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return doGetStateForPlacement(context.getLevel(), context.getClickedPos());
	}

	@Override
	@Deprecated
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return doUpdatePostPlacement(stateIn, facing, facingState);
	}
}
