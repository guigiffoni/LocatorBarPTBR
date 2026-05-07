package pl.fuzjajadrowa.locatorbar.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

import java.lang.reflect.InvocationTargetException;

@Mod(LocatorBar.MOD_ID)
public final class LocatorBarNeoForge {
    public LocatorBarNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        LocatorBar.init();
        //? if >=1.21.11 {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            initClient(modContainer);
        }
        //?} else {
        /*if (FMLEnvironment.dist == Dist.CLIENT) {
            initClient(modContainer);
        }
        *///?}
    }

    private static void initClient(ModContainer modContainer) {
        try {
            Class<?> clientHooks = Class.forName("pl.fuzjajadrowa.locatorbar.neoforge.LocatorBarNeoForgeClient");
            clientHooks.getMethod("init", ModContainer.class).invoke(null, modContainer);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("Failed to initialize Locator Bar client hooks", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Failed to initialize Locator Bar client hooks", cause);
        }
    }
}