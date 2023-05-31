package vapourdrive.furnacemk2.furnace;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.config.ConfigSettings;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public class FurnaceMk2Item extends BlockItem {
    public FurnaceMk2Item(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
        DecimalFormat df = new DecimalFormat("#,###");
        list.add(Component.literal(df.format(ConfigSettings.FURNACE_BASE_SPEED.get()*100 )+"% speed").withStyle(ChatFormatting.BLUE));
        list.add(Component.literal(df.format(ConfigSettings.FURNACE_BASE_EXPERIENCE.get()*100 )+"% experience").withStyle(ChatFormatting.BLUE));
        list.add(Component.literal(df.format(ConfigSettings.FURNACE_BASE_EFFICIENCY.get()*100 )+"% fuel efficiency").withStyle(ChatFormatting.BLUE));
    }
}
