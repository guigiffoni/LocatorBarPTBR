package pl.fuzjajadrowa.locatorbar;

import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig;

public final class LocatorBar {
    public static final String MOD_ID = "locatorbar";

    private LocatorBar() {
    }

    public static void init(boolean loadClientConfig) {
        if (loadClientConfig) {
            LocatorBarConfig.load();
        }
        LocatorBarServerConfig.load();
    }
}
