package vapourdrive.furnacemk2.furnace;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.config.ConfigSettings;
import vapourdrive.vapourware.shared.base.BaseMachineItem;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public class FurnaceMk2Item extends BaseMachineItem {
    public FurnaceMk2Item(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);
        DecimalFormat df = new DecimalFormat("#,###");
        list.add(Component.literal(df.format(ConfigSettings.FURNACE_BASE_SPEED.get()*100 )+"% speed").withStyle(ChatFormatting.BLUE));
        list.add(Component.literal(df.format(ConfigSettings.FURNACE_BASE_EXPERIENCE.get()*100 )+"% experience").withStyle(ChatFormatting.BLUE));
        list.add(Component.literal(df.format(ConfigSettings.FURNACE_BASE_EFFICIENCY.get()*100 )+"% fuel efficiency").withStyle(ChatFormatting.BLUE));
    }

    @Override
    protected void updateAdditional(BlockEntity blockentity, CompoundTag tag) {
        if (blockentity instanceof FurnaceMk2Tile machine) {
            machine.addExperience(tag.getInt(FurnaceMk2.MODID + ".exp"));
        }
        super.updateAdditional(blockentity, tag);
    }
}
