package vapourdrive.furnacemk2.furnace;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import javax.annotation.Nullable;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.Containers.dropItemStack;

public class FurnaceMk2Block extends FurnaceBlock implements EntityBlock {
    public FurnaceMk2Block() {
        super(BlockBehaviour.Properties.of(Material.STONE)
                .sound(SoundType.STONE)
                .strength(4.0f)
                .lightLevel(state -> state.getValue(LIT) ? 13 : 0)
                .requiresCorrectToolForDrops()
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FurnaceMk2Tile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        } else {
            return (level1, pos, state1, tile) -> {
                if (tile instanceof FurnaceMk2Tile furnace) {
                    furnace.tickServer(state1);
                }
            };
        }
    }

//    @Nullable
//    @Override
//    public BlockState getStateForPlacement(BlockPlaceContext context) {
//        return defaultBlockState().setValue(BlockStateProperties.FACING, context.getHorizontalDirection().getOpposite()).setValue(BlockStateProperties.LIT, false);
//    }

//    @Override
//    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
//        if (worldIn.isClientSide) return InteractionResult.SUCCESS; // on client side, don't do anything
//
//        MenuProvider namedContainerProvider = this.getMenuProvider(state, worldIn, pos);
//        if (namedContainerProvider != null) {
//            if (!(player instanceof ServerPlayer)) return InteractionResult.FAIL;  // should always be true, but just in case...
//            ServerPlayer serverPlayerEntity = (ServerPlayer)player;
//            NetworkHooks.openGui(serverPlayerEntity, namedContainerProvider, pos);
//        }
//        return InteractionResult.SUCCESS;
//    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        if (!level.isClientSide) {
            openContainer(level, pos, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FurnaceMk2Tile) {
            FurnaceMk2Tile furnace = (FurnaceMk2Tile) blockEntity;
            MenuProvider containerProvider = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("screen.furnacemk2.furnacemk2");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                    return new FurnaceMk2Container(windowId, level, pos, playerInventory, playerEntity, furnace.getFurnaceData());
                }
            };
            NetworkHooks.openScreen((ServerPlayer) player, containerProvider, blockEntity.getBlockPos());
        } else {
            throw new IllegalStateException("Our named container provider is missing!");
        }
    }

//    @Override
//    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
//        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.LIT);
//    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileentity = world.getBlockEntity(blockPos);
            if (tileentity instanceof FurnaceMk2Tile) {
                FurnaceMk2Tile tileEntityFurnace = (FurnaceMk2Tile)tileentity;
                dropContents(world, blockPos, tileEntityFurnace.getItemHandler());
            }
            super.onRemove(state, world, blockPos, newState, isMoving);
        }
    }

    private static void dropContents(Level world, BlockPos blockPos, IItemHandler handler) {
        for(int i = 0; i < handler.getSlots(); ++i) {
            dropItemStack(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), handler.getStackInSlot(i));
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, Random rand) {
        if (state.getValue(LIT)) {
            double d0 = (double)pos.getX() + 0.5D;
            double d1 = (double)pos.getY();
            double d2 = (double)pos.getZ() + 0.5D;
            if (rand.nextDouble() < 0.1D) {
                world.playLocalSound(d0, d1, d2, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = state.getValue(FACING);
            Direction.Axis direction$axis = direction.getAxis();
            double d3 = 0.52D;
            double d4 = rand.nextDouble() * 0.6D - 0.3D;
            double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
            double d6 = rand.nextDouble() * 6.0D / 16.0D;
            double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;
            world.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
            world.addParticle(ParticleTypes.FLAME, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState iBlockState) {
        return RenderShape.MODEL;
    }

}
