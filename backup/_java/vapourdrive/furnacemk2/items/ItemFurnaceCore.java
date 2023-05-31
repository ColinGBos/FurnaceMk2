package vapourdrive.furnacemk2.items;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vapourdrive.furnacemk2.config.ConfigSettings;
import vapourdrive.furnacemk2.setup.ModSetup;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ItemFurnaceCore extends Item {
    public String type;

    public ItemFurnaceCore(String type) {
        super(new Item.Properties().stacksTo(1).tab(ModSetup.ITEM_GROUP));
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flags) {
        list.add(Component.translatable("message.furnace_core."+type+".1").withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("message.furnace_core."+type+".2").withStyle(ChatFormatting.BLUE));
        DecimalFormat df = new DecimalFormat("#,###");
        if(type == "speed") {
            list.add(Component.literal(df.format(ConfigSettings.FURNACE_UPGRADED_SPEED.get()*ConfigSettings.FURNACE_BASE_SPEED.get()/ConfigSettings.FURNACE_BASE_SPEED.get()*100 )+"% faster").withStyle(ChatFormatting.BLUE));
        }
        else if(type == "experience") {
            list.add(Component.literal(df.format(ConfigSettings.FURNACE_UPGRADED_EXPERIENCE.get()*ConfigSettings.FURNACE_BASE_EXPERIENCE.get()/ConfigSettings.FURNACE_BASE_EXPERIENCE.get()*100 )+"% more experience").withStyle(ChatFormatting.BLUE));
        }
        else if(type == "efficiency") {
            list.add(Component.literal(df.format(ConfigSettings.FURNACE_UPGRADED_EFFICIENCY.get()*ConfigSettings.FURNACE_BASE_EFFICIENCY.get()/ConfigSettings.FURNACE_BASE_EFFICIENCY.get()*100 )+"% less fuel").withStyle(ChatFormatting.BLUE));
        }

    }
}
