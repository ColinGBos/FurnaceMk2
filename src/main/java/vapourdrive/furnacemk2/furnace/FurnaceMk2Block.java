package vapourdrive.furnacemk2.furnace;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.setup.Registration;
import vapourdrive.vapourware.shared.base.AbstractBaseMachineBlock;

import javax.annotation.Nullable;

public class FurnaceMk2Block extends AbstractBaseMachineBlock implements EntityBlock {
    public static final MapCodec<FurnaceMk2Block> CODEC = simpleCodec(FurnaceMk2Block::new);

    public FurnaceMk2Block() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM), 0.2f);
    }

    public FurnaceMk2Block(Properties properties) {
        super(properties, 0.2f);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FurnaceMk2Tile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        } else {
            return (level1, pos, state1, tile) -> {
                if (tile instanceof FurnaceMk2Tile machine) {
                    machine.tickServer(state1);
                }
            };
        }
    }

    @Override
    public boolean sneakWrenchMachine(Player player, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FurnaceMk2Tile furnace) {
            if (!level.isClientSide()) {
                player.giveExperiencePoints(furnace.getCurrentExp()/100);
                furnace.getFurnaceData().set(FurnaceData.Data.EXPERIENCE, 0);
                furnace.setChanged();
            } else {
                level.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.05F, 1f);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void openContainer(Level level, @NotNull BlockPos pos, @NotNull Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FurnaceMk2Tile furnace) {
            player.openMenu((MenuProvider) blockEntity, pos);
        }
    }

    @Override
    protected ItemStack putAdditionalInfo(ItemStack stack, BlockEntity blockEntity) {
        super.putAdditionalInfo(stack,blockEntity);
        if(blockEntity instanceof FurnaceMk2Tile machine) {
            stack.set(Registration.EXPERIENCE_DATA, machine.getCurrentExp());
        }
        return stack;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
