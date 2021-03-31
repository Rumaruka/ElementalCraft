package sirttas.elementalcraft.block.pipe;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import sirttas.elementalcraft.ElementalCraft;
import sirttas.elementalcraft.block.AbstractBlockECTileProvider;
import sirttas.elementalcraft.block.shape.ShapeHelper;
import sirttas.elementalcraft.entity.EntityHelper;
import sirttas.elementalcraft.tag.ECTags;

public class BlockElementPipe extends AbstractBlockECTileProvider {

	public static final String NAME = "elementpipe";
	public static final String NAME_IMPAIRED = NAME + "_impaired";
	public static final String NAME_IMPROVED = NAME + "_improved";

	private static final VoxelShape BASE_SHAPE = Block.makeCuboidShape(6.5D, 6.5D, 6.5D, 9.5D, 9.5D, 9.5D);
	private static final VoxelShape WEST_SHAPE = Block.makeCuboidShape(0, 7D, 7D, 6.5D, 9D, 9D);
	private static final VoxelShape DOWN_SHAPE = Block.makeCuboidShape(7D, 0, 7D, 9D, 6.5D, 9D);
	private static final VoxelShape NORTH_SHAPE = Block.makeCuboidShape(7D, 7D, 0, 9D, 9D, 6.5D);
	private static final VoxelShape EAST_SHAPE = Block.makeCuboidShape(9.5D, 7D, 7D, 16D, 9D, 9D);
	private static final VoxelShape UP_SHAPE = Block.makeCuboidShape(7D, 9.5D, 7D, 9D, 16D, 9D);
	private static final VoxelShape SOUTH_SHAPE = Block.makeCuboidShape(7D, 7D, 9.5D, 9D, 9D, 16D);

	private static final VoxelShape FRAME_SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(),
			VoxelShapes.or(Block.makeCuboidShape(0D, 1D, 1D, 16D, 15D, 15D), Block.makeCuboidShape(1D, 0D, 1D, 15D, 16D, 15D), Block.makeCuboidShape(1D, 1D, 0D, 15D, 15D, 16D)),
			IBooleanFunction.ONLY_FIRST);
	
	private static final List<VoxelShape> SHAPES = ImmutableList.of(EAST_SHAPE, NORTH_SHAPE, WEST_SHAPE, SOUTH_SHAPE, UP_SHAPE, DOWN_SHAPE, BASE_SHAPE, FRAME_SHAPE);

	public static final EnumProperty<CoverType> COVER = EnumProperty.create("cover", CoverType.class);
	
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

	public static final BooleanProperty NORTH_EXTRACT = BooleanProperty.create("north_extract");
	public static final BooleanProperty EAST_EXTRACT = BooleanProperty.create("east_extract");
	public static final BooleanProperty SOUTH_EXTRACT = BooleanProperty.create("south_extract");
	public static final BooleanProperty WEST_EXTRACT = BooleanProperty.create("west_extract");
	public static final BooleanProperty UP_EXTRACT = BooleanProperty.create("up_extract");
	public static final BooleanProperty DOWN_EXTRACT = BooleanProperty.create("down_extract");

	private int maxTransferAmount;

	public BlockElementPipe(int maxTransferAmount) {
		super(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(2).sound(SoundType.METAL).harvestTool(ToolType.PICKAXE).harvestLevel(1).notSolid().tickRandomly());
		this.setDefaultState(this.stateContainer.getBaseState()
				.with(COVER, CoverType.NONE)
				.with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false).with(UP, false).with(DOWN, false)
				.with(NORTH_EXTRACT, false).with(EAST_EXTRACT, false).with(SOUTH_EXTRACT, false).with(WEST_EXTRACT, false).with(UP_EXTRACT, false).with(DOWN_EXTRACT, false));
		this.maxTransferAmount = maxTransferAmount;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> container) {
		container.add(COVER, NORTH, SOUTH, EAST, WEST, UP, DOWN, NORTH_EXTRACT, SOUTH_EXTRACT, EAST_EXTRACT, WEST_EXTRACT, UP_EXTRACT, DOWN_EXTRACT);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TileElementPipe(maxTransferAmount);
	}

	@Override
	@Deprecated
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		((TileElementPipe) worldIn.getTileEntity(pos)).refresh();
	}

	@Override
	@Deprecated
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		((TileElementPipe) worldIn.getTileEntity(pos)).refresh();
	}

	private boolean compareShapes(VoxelShape shape1, VoxelShape shape2) {
		return shape1.getBoundingBox().equals(shape2.getBoundingBox());
	}

	private Direction getFace(VoxelShape shape, BlockRayTraceResult hit) {
		if (compareShapes(shape, DOWN_SHAPE)) {
			return Direction.DOWN;
		} else if (compareShapes(shape, UP_SHAPE)) {
			return Direction.UP;
		} else if (compareShapes(shape, NORTH_SHAPE)) {
			return Direction.NORTH;
		} else if (compareShapes(shape, SOUTH_SHAPE)) {
			return Direction.SOUTH;
		} else if (compareShapes(shape, WEST_SHAPE)) {
			return Direction.WEST;
		} else if (compareShapes(shape, EAST_SHAPE)) {
			return Direction.EAST;
		} else if (shape == BASE_SHAPE) {
			return hit.getFace();
		}
		return null;
	}
	
	public static boolean showCover(BlockState state, PlayerEntity player) {
		return isCovered(state)
				&& (player == null || !player.getHeldItemMainhand().isEmpty() && EntityHelper.handStream(player).noneMatch(stack -> ECTags.Items.PIPE_COVER_HIDING.contains(stack.getItem())));
	}

	private static boolean isCovered(BlockState state) {
		return state.get(COVER) == CoverType.COVERED;
	}

	private boolean isRendered(VoxelShape shape, BlockState state) {
		return (state.matchesBlock(this)) && (compareShapes(shape, BASE_SHAPE) 
				|| (compareShapes(shape, DOWN_SHAPE) && Boolean.TRUE.equals(state.get(DOWN)))
				|| (compareShapes(shape, UP_SHAPE) && Boolean.TRUE.equals(state.get(UP))) 
				|| (compareShapes(shape, NORTH_SHAPE) && Boolean.TRUE.equals(state.get(NORTH)))
				|| (compareShapes(shape, SOUTH_SHAPE) && Boolean.TRUE.equals(state.get(SOUTH))) 
				|| (compareShapes(shape, WEST_SHAPE) && Boolean.TRUE.equals(state.get(WEST)))
				|| (compareShapes(shape, EAST_SHAPE) && Boolean.TRUE.equals(state.get(EAST)))
				|| (compareShapes(shape, FRAME_SHAPE) && state.get(COVER) == CoverType.FRAME));
	}

	private VoxelShape getCurentShape(BlockState state, PlayerEntity player) {
		VoxelShape result = VoxelShapes.empty();

		if (showCover(state, player)) {
			return VoxelShapes.fullCube();
		}
		for (final VoxelShape shape : SHAPES) {
			if (isRendered(shape, state)) {
				result = VoxelShapes.or(result, shape);
			}
		}
		return result;
	}

	@Override
	@Deprecated
	public BlockRenderType getRenderType(BlockState state) {
		if (isCovered(state)) {
			return BlockRenderType.ENTITYBLOCK_ANIMATED;
		}
		return super.getRenderType(state);
	}
	
	@Override
	@Deprecated
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		PlayerEntity player = getPlayer(context);

		return worldIn instanceof World && ((World) worldIn).isRemote ? getShape(state, pos, Minecraft.getInstance().objectMouseOver, player) : getCurentShape(state, player);
	}

	private PlayerEntity getPlayer(ISelectionContext context) {
		Entity entity = context.getEntity();
		
		return entity instanceof PlayerEntity ? (PlayerEntity) entity : ElementalCraft.PROXY.getDefaultPlayer();
	}

	public VoxelShape getShape(BlockState state, BlockPos pos, RayTraceResult result, PlayerEntity player) {
		if (!showCover(state, player) && result != null && result.getType() == RayTraceResult.Type.BLOCK && ((BlockRayTraceResult) result).getPos().equals(pos)) {
			final Vector3d hit = result.getHitVec();

			for (final VoxelShape shape : SHAPES) {
				if (isRendered(shape, state) && ShapeHelper.vectorCollideWithShape(shape, pos, hit)) {
					return shape;
				}
			}
		}
		return getCurentShape(state, player);
	}


	@Override
	@Deprecated
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return getCurentShape(state, null);
	}

	@Override
	@Deprecated
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		final TileElementPipe pipe = (TileElementPipe) world.getTileEntity(pos);

		if (pipe != null) {
			final VoxelShape shape = getShape(state, pos, hit, player);
			
			if (compareShapes(shape, FRAME_SHAPE) || state.get(COVER) == CoverType.FRAME) {
				return pipe.setCover(player, hand);
			} else {
				Direction face = getFace(shape, hit);
				ActionResultType value = onShapeActivated(face, pipe);
	
				if (value != ActionResultType.PASS) {
					player.sendStatusMessage(pipe.getConnectionMessage(face), true);
				}
				return value;
			}
		}
		return ActionResultType.PASS;
	}

	private ActionResultType onShapeActivated(Direction face, TileElementPipe pipe) {
		if (face != null) {
			return pipe.activatePipe(face);
		}
		return ActionResultType.PASS;
	}
	
	@Override
	@Deprecated
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			if (isCovered(state)) {
				InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(((TileElementPipe) worldIn.getTileEntity(pos)).getCoverState().getBlock()));
			}
			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}

	public enum CoverType implements IStringSerializable {
		NONE("none"), FRAME("frame"), COVERED("covered");

		public static final Codec<CoverType> CODEC = IStringSerializable.createEnumCodec(CoverType::values, CoverType::byName);

		private final String name;

		private CoverType(String name) {
			this.name = name;
		}

		@Nonnull
		@Override
		public String getString() {
			return this.name;
		}

		public static CoverType byName(String name) {
			for (CoverType bonusType : values()) {
				if (bonusType.name.equals(name)) {
					return bonusType;
				}
			}
			return NONE;
		}
	}
	
}
