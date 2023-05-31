package vapourdrive.furnacemk2.setup;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import vapourdrive.furnacemk2.furnace.*;
import vapourdrive.furnacemk2.items.ItemCrystal;
import vapourdrive.furnacemk2.items.ItemFurnaceCore;

import static vapourdrive.furnacemk2.FurnaceMk2.MODID;

public class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<FurnaceMk2Block> FURNACEMK2_BLOCK = BLOCKS.register("furnacemk2", FurnaceMk2Block::new);
    public static final RegistryObject<Item> FURNACEMK2_ITEM = ITEMS.register("furnacemk2", () -> new FurnaceMk2Item(FURNACEMK2_BLOCK.get(), new Item.Properties().tab(ModSetup.ITEM_GROUP)));
    public static final RegistryObject<BlockEntityType<FurnaceMk2Tile>> FURNACEMK2_TILE = TILES.register("furnacemk2", () -> BlockEntityType.Builder.of(FurnaceMk2Tile::new, FURNACEMK2_BLOCK.get()).build(null));

    public static final RegistryObject<MenuType<FurnaceMk2Container>> FURNACEMK2_CONTAINER = CONTAINERS.register("furnacemk2", () -> IForgeMenuType.create((windowId, inv, data) -> {
        BlockPos pos = data.readBlockPos();
        Level world = inv.player.getCommandSenderWorld();
        return new FurnaceMk2Container(windowId, world, pos, inv, inv.player, new FurnaceData());
    }));

    public static final RegistryObject<ItemFurnaceCore> EXPERIENCE_CORE_ITEM = ITEMS.register("experience_core", () -> new ItemFurnaceCore("experience"));
    public static final RegistryObject<ItemFurnaceCore> INSULATION_CORE_ITEM = ITEMS.register("insulation_core", () -> new ItemFurnaceCore("efficiency"));
    public static final RegistryObject<ItemFurnaceCore> THERMAL_CORE_ITEM = ITEMS.register("thermal_core", () -> new ItemFurnaceCore("speed"));
    public static final RegistryObject<ItemCrystal> CRYSTAL_GEM_ITEM = ITEMS.register("crystal_gem_item", ItemCrystal::new);

}
