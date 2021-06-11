package sirttas.elementalcraft.block.shrine.lava;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ObjectHolder;
import sirttas.elementalcraft.api.ElementalCraftApi;
import sirttas.elementalcraft.api.element.ElementType;
import sirttas.elementalcraft.block.shrine.AbstractShrineBlockEntity;
import sirttas.elementalcraft.config.ECConfig;
import sirttas.elementalcraft.tag.ECTags;

public class LavaShrineBlockEntity extends AbstractShrineBlockEntity {

	@ObjectHolder(ElementalCraftApi.MODID + ":" + LavaShrineBlock.NAME) public static final TileEntityType<LavaShrineBlockEntity> TYPE = null;

	private static final Properties PROPERTIES = Properties.create(ElementType.FIRE).periode(ECConfig.COMMON.lavaShrinePeriode.get()).consumeAmount(ECConfig.COMMON.lavaShrineConsumeAmount.get())
			.range(ECConfig.COMMON.lavaShrineRange.get()).capacity(ECConfig.COMMON.shrinesCapacity.get() * 10);

	protected static final List<Direction> UPGRRADE_DIRECTIONS = ImmutableList.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

	public LavaShrineBlockEntity() {
		super(TYPE, PROPERTIES);
	}

	private Optional<BlockPos> findRock() {
		int range = ECConfig.COMMON.lavaShrineRange.get();

		return IntStream.range(-range, range + 1).mapToObj(x -> IntStream.range(-range, range + 1).mapToObj(z -> new BlockPos(worldPosition.getX() + x, worldPosition.getY() + 1, worldPosition.getZ() + z))).flatMap(s -> s)
				.filter(p -> ECTags.Blocks.LAVASHRINE_LIQUIFIABLES.contains(level.getBlockState(p).getBlock())).findAny();

	}

	@Override
	public AxisAlignedBB getRangeBoundingBox() {
		int range = ECConfig.COMMON.lavaShrineRange.get();

		return new AxisAlignedBB(this.getBlockPos()).inflate(range, 0, range).move(0, 1, 0);
	}

	@Override
	protected boolean doTick() {
		return findRock().map(p -> {
			level.setBlockAndUpdate(p, Blocks.LAVA.defaultBlockState());
			level.levelEvent(1501, p, 0);
			return true;
		}).orElse(false);
	}

	@Override
	public List<Direction> getUpgradeDirections() {
		return UPGRRADE_DIRECTIONS;
	}
}
