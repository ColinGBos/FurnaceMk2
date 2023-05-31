package vapourdrive.furnacemk2;


import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vapourdrive.furnacemk2.setup.ClientSetup;
import vapourdrive.furnacemk2.config.ConfigSettings;
import vapourdrive.furnacemk2.setup.ModSetup;
import vapourdrive.furnacemk2.setup.Registration;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FurnaceMk2.MODID)
public class FurnaceMk2
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "furnacemk2";
    public static boolean debugMode = false;

    public FurnaceMk2() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigSettings.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigSettings.SERVER_CONFIG);

        Registration.init();

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::setup);
    }

    public static void debugLog(String toLog) {
        if(debugMode) {
            log(toLog);
        }
    }

    private static void log(String toLog) {
        LOGGER.log(Level.INFO, toLog);
    }
}
