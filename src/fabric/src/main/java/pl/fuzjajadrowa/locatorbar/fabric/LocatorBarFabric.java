package pl.fuzjajadrowa.locatorbar.fabric;

import net.fabricmc.api.ModInitializer;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

public final class LocatorBarFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        LocatorBar.init();
    }
}