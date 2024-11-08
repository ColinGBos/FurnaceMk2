package vapourdrive.furnacemk2.furnace;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.config.ConfigSettings;
import vapourdrive.furnacemk2.setup.Registration;
import vapourdrive.vapourware.shared.base.BaseMachineItem;
import vapourdrive.vapourware.shared.utils.DeferredComponent;

import java.text.DecimalFormat;
import java.util.List;

public class FurnaceMk2Item extends BaseMachineItem {
    public FurnaceMk2Item(Block block, Properties properties) {
        super(block, properties, new DeferredComponent(FurnaceMk2.MODID, "furnacemk2.info"));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal(df.format(ConfigSettings.FURNACE_BASE_SPEED.get()*100 )+"% speed").withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.literal(df.format(ConfigSettings.FURNACE_BASE_EXPERIENCE.get()*100 )+"% experience").withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.literal(df.format(ConfigSettings.FURNACE_BASE_EFFICIENCY.get()*100 )+"% fuel efficiency").withStyle(ChatFormatting.BLUE));
    }

    @Override
    protected List<Component> appendAdditionalTagInfo(List<Component> list, CompoundTag tag) {
        float experience = (float)tag.getInt("furnacemk2.exp") / 100f;
        DecimalFormat exp_f = new DecimalFormat("#,###.##");
        list.add(Component.literal("Exp: ").append(exp_f.format(experience)+"/"+exp_f.format(FurnaceMk2Tile.getMaxExp()/100)));
        return list;
    }

    @Override
    protected void updateAdditional(BlockEntity blockEntity, ItemStack stack) {
        if (blockEntity instanceof FurnaceMk2Tile machine) {
            machine.addExperience(stack.getOrDefault(Registration.EXPERIENCE_DATA, 0));
        }
        super.updateAdditional(blockEntity, stack);
    }
}
