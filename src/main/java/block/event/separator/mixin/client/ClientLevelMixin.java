package block.event.separator.mixin.client;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import block.event.separator.BlockEventCounters;
import block.event.separator.interfaces.mixin.IClientLevel;
import block.event.separator.interfaces.mixin.IPistonMovingBlockEntity;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level implements IClientLevel {

	private ClientLevelMixin(WritableLevelData data, ResourceKey<Level> dimension, DimensionType dimensionType, Supplier<ProfilerFiller> profiler, boolean isClient, boolean isDebug, long seed) {
		super(data, dimension, dimensionType, profiler, isClient, isDebug, seed);
	}

	@Inject(
		method = "tickEntities",
		cancellable = true,
		at = @At(
			value = "HEAD"
		)
	)
	private void cancelTick(CallbackInfo ci) {
		if (BlockEventCounters.frozen) {
			ci.cancel();
		}
	}

	@Override
	public void tickMovingBlocks_bes() {
		for (int i = 0; i < tickableBlockEntities.size(); i++) {
			BlockEntity blockEntity = tickableBlockEntities.get(i);

			if (blockEntity.isRemoved() || !blockEntity.hasLevel()) {
				continue;
			}
			if (!(blockEntity instanceof PistonMovingBlockEntity)) {
				continue;
			}

			BlockPos pos = blockEntity.getBlockPos();

			if (!getChunkSource().isTickingChunk(pos) || !getWorldBorder().isWithinBounds(pos)) {
				continue;
			}
			if (!blockEntity.getType().isValid(getBlockState(pos).getBlock())) {
				continue;
			}

			((IPistonMovingBlockEntity)blockEntity).extraTick_bes();
		}
	}
}
