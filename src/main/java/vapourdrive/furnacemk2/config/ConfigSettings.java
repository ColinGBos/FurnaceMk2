package vapourdrive.furnacemk2.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigSettings {
    public static final String CATEGORY_FURNACE = "furnacemk2";
    public static final String SUBCATEGORY_FURNACEMK2 = "furnace";
    public static final String SUBCATEGORY_CRYSTAL = "crystal";

    public static ForgeConfigSpec SERVER_CONFIG;
    public static ForgeConfigSpec.DoubleValue FURNACE_BASE_EFFICIENCY;
    public static ForgeConfigSpec.DoubleValue FURNACE_BASE_EXPERIENCE;
    public static ForgeConfigSpec.DoubleValue FURNACE_BASE_SPEED;
    public static ForgeConfigSpec.DoubleValue FURNACE_UPGRADED_EFFICIENCY;
    public static ForgeConfigSpec.DoubleValue FURNACE_UPGRADED_EXPERIENCE;
    public static ForgeConfigSpec.DoubleValue FURNACE_UPGRADED_SPEED;
    public static ForgeConfigSpec.IntValue CRYSTAL_EXPERIENCE_STORAGE;

    static {
        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

        SERVER_BUILDER.comment("FurnaceMk2 settings").push(CATEGORY_FURNACE);

        setupFirstBlockConfig(SERVER_BUILDER);

        SERVER_BUILDER.pop();


        SERVER_CONFIG = SERVER_BUILDER.build();
    }

    private static void setupFirstBlockConfig(ForgeConfigSpec.Builder SERVER_BUILDER) {
        SERVER_BUILDER.comment("Furnace settings").push(SUBCATEGORY_FURNACEMK2);

        FURNACE_BASE_EFFICIENCY = SERVER_BUILDER.comment("Base efficiency multiplier for the FurnaceMk2").defineInRange("furnaceBaseEfficiency", 1.25, 0.5, 10.0);
        FURNACE_BASE_EXPERIENCE = SERVER_BUILDER.comment("Base experience multiplier for the FurnaceMk2").defineInRange("furnaceBaseExperience", 1.25, 0.5, 10.0);
        FURNACE_BASE_SPEED = SERVER_BUILDER.comment("Base speed multiplier for the FurnaceMk2").defineInRange("furnaceBaseSpeed", 2.0, 0.0, 10.0);
        FURNACE_UPGRADED_EFFICIENCY = SERVER_BUILDER.comment("Upgraded efficiency multiplier for the FurnaceMk2").defineInRange("furnaceUpgradedEfficiency", 1.5, 0.5, 10.0);
        FURNACE_UPGRADED_EXPERIENCE = SERVER_BUILDER.comment("Upgraded experience multiplier for the FurnaceMk2").defineInRange("furnaceUpgradedExperience", 2.0, 0.5, 10.0);
        FURNACE_UPGRADED_SPEED = SERVER_BUILDER.comment("Upgraded speed multiplier for the FurnaceMk2").defineInRange("furnaceUpgradedSpeed", 2.0, 0.5, 10.0);

        SERVER_BUILDER.pop();

        SERVER_BUILDER.comment("Attuned Crystal settings").push(SUBCATEGORY_CRYSTAL);

        CRYSTAL_EXPERIENCE_STORAGE = SERVER_BUILDER.comment("Storage capacity for the Attuned Crystal").defineInRange("crystalXPStorage", 10000, 100, 1000000);

        SERVER_BUILDER.pop();
    }

}
