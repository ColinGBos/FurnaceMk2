package vapourdrive.furnacemk2.setup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import vapourdrive.furnacemk2.furnace.*;
import vapourdrive.furnacemk2.items.ItemCrystal;
import vapourdrive.furnacemk2.items.ItemFurnaceCore;
import vapourdrive.vapourware.VapourWare;

import java.util.function.Supplier;

import static vapourdrive.furnacemk2.FurnaceMk2.MODID;

public class Registration {
    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, VapourWare.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> EXPERIENCE_DATA = REGISTRAR.registerComponentType(
            "experience", builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);

    public static final Supplier<FurnaceMk2Block> FURNACEMK2_BLOCK = BLOCKS.register("furnacemk2", () -> new FurnaceMk2Block());
    public static final Supplier<Item> FURNACEMK2_ITEM = ITEMS.register("furnacemk2", () -> new FurnaceMk2Item(FURNACEMK2_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<FurnaceMk2Tile>> FURNACEMK2_TILE = TILES.register("furnacemk2",
        () -> BlockEntityType.Builder.of(FurnaceMk2Tile::new, FURNACEMK2_BLOCK.get()).build(null));

    public static final Supplier<MenuType<FurnaceMk2Menu>> FURNACEMK2_MENU = MENUS.register("furnacemk2",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            Level world = inv.player.getCommandSenderWorld();
            return new FurnaceMk2Menu(windowId, world, pos, inv, inv.player, new FurnaceData());
        }));

    public static final Supplier<Item> EXPERIENCE_CORE_ITEM = ITEMS.register("experience_core", () -> new ItemFurnaceCore("experience"));
    public static final Supplier<Item> INSULATION_CORE_ITEM = ITEMS.register("insulation_core", () -> new ItemFurnaceCore("efficiency"));
    public static final Supplier<Item> THERMAL_CORE_ITEM = ITEMS.register("thermal_core", () -> new ItemFurnaceCore("speed"));
    public static final Supplier<Item> CRYSTAL_GEM_ITEM = ITEMS.register("crystal_gem_item", ItemCrystal::new);


    public static void init(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        TILES.register(eventBus);
        MENUS.register(eventBus);
        REGISTRAR.register(eventBus);
    }

    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        // Add to ingredients tab
        if (event.getTab() == vapourdrive.vapourware.setup.Registration.VAPOUR_GROUP.get()) {
            event.accept(FURNACEMK2_ITEM.get());
            event.accept(EXPERIENCE_CORE_ITEM.get());
            event.accept(INSULATION_CORE_ITEM.get());
            event.accept(THERMAL_CORE_ITEM.get());
            event.accept(CRYSTAL_GEM_ITEM.get());
        }
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FURNACEMK2_TILE.get(), FurnaceMk2Tile::getItemHandler);
    }

}
