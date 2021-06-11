package sirttas.elementalcraft.block.shrine.firepylon;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import sirttas.elementalcraft.api.element.ElementType;
import sirttas.elementalcraft.block.shape.Shapes;
import sirttas.elementalcraft.block.shrine.AbstractPylonShrineBlock;
import sirttas.elementalcraft.block.shrine.AbstractShrineBlockEntity;

public class FirePylonBlock extends AbstractPylonShrineBlock {

	public static final String NAME = "firepylon";

	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	private static final VoxelShape BASE = Block.box(6D, 12D, 6D, 10D, 16D, 10D);

	private static final VoxelShape IRON_NORTH = Block.box(7D, 12D, 5D, 9D, 14D, 6D);
	private static final VoxelShape IRON_SOUTH = Block.box(7D, 12D, 10D, 9D, 14D, 11D);
	private static final VoxelShape IRON_EAST = Block.box(10D, 12D, 7D, 11D, 14D, 9D);
	private static final VoxelShape IRON_WEST = Block.box(5D, 12D, 7D, 6D, 14D, 9D);

	private static final VoxelShape UPPER_BASE = Block.box(6D, 0D, 6D, 10D, 7D, 10D);
	private static final VoxelShape UPPER_TOP = Block.box(5D, 7D, 5D, 11D, 11D, 11D);

	private static final VoxelShape LOWER_SHAPE = VoxelShapes.or(Shapes.SHRINE_SHAPE, BASE, IRON_NORTH, IRON_SOUTH, IRON_EAST, IRON_WEST);
	private static final VoxelShape UPPER_SHAPE = VoxelShapes.or(UPPER_BASE, UPPER_TOP);

	public FirePylonBlock() {
		super(ElementType.FIRE);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new FirePylonBlockEntity() : null;
	}

	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		worldIn.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos pos, BlockPos facingPos) {
		return stateIn.getValue(HALF) == DoubleBlockHalf.UPPER && !worldIn.getBlockState(pos.below()).is(this) ? Blocks.AIR.defaultBlockState()
				: super.updateShape(stateIn, facing, facingState, worldIn, pos, facingPos);
	}

	/**
	 * Called before the Block is set to air in the world. Called regardless of if
	 * the player's tool can actually collect this block
	 */
	@Override
	public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!worldIn.isClientSide && player.isCreative()) {
			DoublePlantBlock.preventCreativeDropFromBottomPart(worldIn, pos, state, player);
		}

		super.playerWillDestroy(worldIn, pos, state, player);
	}

	@Override
	@Deprecated
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : UPPER_SHAPE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void doAnimateTick(AbstractShrineBlockEntity shrine, BlockState state, World world, BlockPos pos, Random rand) {
		double x = pos.getX() + (4 + rand.nextDouble() * 7) / 16;
		double y = pos.getY() + 6D / 16;
		double z = pos.getZ() + (4 + rand.nextDouble() * 7) / 16;

		world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
		world.addParticle(ParticleTypes.SMOKE, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D);
	}
}