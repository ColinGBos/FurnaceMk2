package vapourdrive.furnacemk2;


import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vapourdrive.furnacemk2.config.ConfigSettings;
import vapourdrive.furnacemk2.setup.ClientSetup;
import vapourdrive.furnacemk2.setup.Registration;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FurnaceMk2.MODID)
public class FurnaceMk2
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "furnacemk2";
    public static boolean debugMode = true;

    public FurnaceMk2() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigSettings.SERVER_CONFIG);

        Registration.init();

        // Register the setup method for modloading
        eventBus.addListener(ClientSetup::setup);
        eventBus.addListener(Registration::buildContents);
    }

    public static void debugLog(String toLog) {
        if(isDebugMode()) {
            LOGGER.log(Level.DEBUG, toLog);
        }
    }

    public static boolean isDebugMode() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp") && debugMode;
    }

}
