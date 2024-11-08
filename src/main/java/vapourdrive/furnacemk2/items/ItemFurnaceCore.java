package vapourdrive.furnacemk2.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import vapourdrive.furnacemk2.config.ConfigSettings;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class ItemFurnaceCore extends Item {
    public String type;

    public ItemFurnaceCore(String type) {
        super(new Item.Properties().stacksTo(1));
        this.type = type;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("message.furnace_core."+type+".1").withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable("message.furnace_core."+type+".2").withStyle(ChatFormatting.BLUE));
        DecimalFormat df = new DecimalFormat("#,###");
        if(Objects.equals(type, "speed")) {
            tooltipComponents.add(Component.literal(df.format(ConfigSettings.FURNACE_UPGRADED_SPEED.get()*100 )+"% speed").withStyle(ChatFormatting.BLUE));
        }
        else if(Objects.equals(type, "experience")) {
            tooltipComponents.add(Component.literal(df.format(ConfigSettings.FURNACE_UPGRADED_EXPERIENCE.get()*100 )+"% experience").withStyle(ChatFormatting.BLUE));
        }
        else if(Objects.equals(type, "efficiency")) {
            tooltipComponents.add(Component.literal(df.format(ConfigSettings.FURNACE_UPGRADED_EFFICIENCY.get()*100 )+"% fuel efficiency").withStyle(ChatFormatting.BLUE));
        }

    }
}
