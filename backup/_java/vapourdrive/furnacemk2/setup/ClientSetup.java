package vapourdrive.furnacemk2.setup;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import vapourdrive.furnacemk2.FurnaceMk2;
import vapourdrive.furnacemk2.furnace.FurnaceMk2Screen;

@Mod.EventBusSubscriber(modid = FurnaceMk2.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void setup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Registration.FURNACEMK2_CONTAINER.get(), FurnaceMk2Screen::new);
        });
    }

//    @SubscribeEvent
//    public void onTooltipPre(RenderTooltipEvent.GatherComponents event) {
//        Item item = event.getItemStack().getItem();
//        if (item.getRegistryName().getNamespace().equals(FurnaceMk2.MODID)) {
//            event.setMaxWidth(200);
//        }
//    }
}
