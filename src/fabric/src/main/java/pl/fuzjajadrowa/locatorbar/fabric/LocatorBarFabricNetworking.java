package pl.fuzjajadrowa.locatorbar.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig;
import pl.fuzjajadrowa.locatorbar.network.ServerConfigPayload;

public final class LocatorBarFabricNetworking {
    private LocatorBarFabricNetworking() {
    }

    public static void initCommon() {
        //? if >=26.1 {
        PayloadTypeRegistry.clientboundPlay().register(ServerConfigPayload.TYPE, ServerConfigPayload.STREAM_CODEC);
        //?} else {
        /*PayloadTypeRegistry.playS2C().register(ServerConfigPayload.TYPE, ServerConfigPayload.STREAM_CODEC);
        *///?}
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var settings = LocatorBarServerConfig.get();
            if (settings != null && ServerPlayNetworking.canSend(handler, ServerConfigPayload.TYPE)) {
                sender.sendPacket(new ServerConfigPayload(settings));
            }
        });
    }

    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(ServerConfigPayload.TYPE, (payload, context) -> {
            LocatorBarConfig.applyServerSettings(payload.settings());
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LocatorBarConfig.clearServerSettings();
        });
    }
}