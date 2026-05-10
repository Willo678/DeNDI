package net.willo678.nodami;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ReNDI.MOD_ID)
public class ReNDI {
    public static final String MOD_ID = "rendi";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static IEventBus bus;

    public ReNDI(IEventBus modEventBus, ModContainer modContainer) {
        bus = modEventBus;
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("reNDI: Serverside operations started.");
        Config.cacheValues();
        bus.register(new Config());
    }
}

