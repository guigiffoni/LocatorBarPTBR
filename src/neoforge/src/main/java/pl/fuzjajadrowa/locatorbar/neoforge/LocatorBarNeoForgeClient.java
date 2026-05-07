package pl.fuzjajadrowa.locatorbar.neoforge;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarConfigScreen;
import pl.fuzjajadrowa.locatorbar.client.LocatorBarHudRenderer;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;

public final class LocatorBarNeoForgeClient {
    private LocatorBarNeoForgeClient() {
    }

    public static void init(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parentScreen) -> new LocatorBarConfigScreen(parentScreen));
        NeoForge.EVENT_BUS.addListener(LocatorBarNeoForgeClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(LocatorBarNeoForgeClient::onLoggingOut);
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        LocatorBarHudRenderer.render(event.getGuiGraphics());
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        LocatorBarConfig.clearServerSettings();
    }
}