package sirttas.elementalcraft.block.extractor;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import sirttas.elementalcraft.block.AbstractECEntityBlock;
import sirttas.elementalcraft.block.entity.BlockEntityHelper;
import sirttas.elementalcraft.particle.ParticleHelper;

public class ExtractorBlock extends AbstractECEntityBlock {

	public static final String NAME = "extractor";

	private static final VoxelShape BASE = Block.box(6D, 0D, 6D, 10D, 4D, 10D);
	private static final VoxelShape PILLAR = Block.box(7D, 4D, 7D, 9D, 13D, 9D);
	private static final VoxelShape TOP = Block.box(6D, 13D, 6D, 10D, 16D, 10D);

	private static final VoxelShape PIPE_N = Block.box(7D, 1D, 3D, 9D, 3D, 6D);
	private static final VoxelShape PIPE_S = Block.box(7D, 1D, 10D, 9D, 3D, 13D);
	private static final VoxelShape PIPE_E = Block.box(10D, 1D, 7D, 13D, 3D, 9D);
	private static final VoxelShape PIPE_W = Block.box(3D, 1D, 7D, 6D, 3D, 9D);

	private static final VoxelShape SHAPE = Shapes.or(BASE, PILLAR, TOP, PIPE_N, PIPE_S, PIPE_E, PIPE_W);

	@Override
	public ExtractorBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ExtractorBlockEntity(pos, state);
	}
	
	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createECServerTicker(level, type, ExtractorBlockEntity.TYPE, ExtractorBlockEntity::serverTick);
	}

	
	@Override
	@Deprecated
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
	
	@Override
	@Deprecated
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		return BlockEntityHelper.isValidContainer(state.getBlock(), world, pos.below());
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, Level world, BlockPos pos, Random rand) {
		BlockEntityHelper.getTileEntityAs(world, pos, ExtractorBlockEntity.class).filter(ExtractorBlockEntity::canExtract)
				.ifPresent(e -> ParticleHelper.createElementFlowParticle(e.getSourceElementType(), world, Vec3.atCenterOf(pos), Direction.DOWN, 1, rand));
	}
}
