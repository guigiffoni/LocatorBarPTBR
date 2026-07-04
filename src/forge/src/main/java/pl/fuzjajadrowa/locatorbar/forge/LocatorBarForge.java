package pl.fuzjajadrowa.locatorbar.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

@Mod(LocatorBar.MOD_ID)
public final class LocatorBarForge {
    public LocatorBarForge() {
        LocatorBar.init(FMLEnvironment.dist == Dist.CLIENT);

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(LocatorBarForgeNetworking::register);

        MinecraftForge.EVENT_BUS.addListener(LocatorBarForgeNetworking::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(LocatorBarForgeNetworking::onServerTick);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            LocatorBarForgeClient.init();
        }
    }
}