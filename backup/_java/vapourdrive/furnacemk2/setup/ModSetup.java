package vapourdrive.furnacemk2.setup;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.commands.ModCommands;

@Mod.EventBusSubscriber(modid = FurnaceMk2.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab("furnacemk2") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.FURNACEMK2_BLOCK.get());
        }
    };

    public static void init(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public static void serverLoad(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
