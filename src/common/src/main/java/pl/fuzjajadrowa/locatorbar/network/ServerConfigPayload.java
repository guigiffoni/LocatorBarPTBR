package pl.fuzjajadrowa.locatorbar.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings;

public record ServerConfigPayload(ServerSettings settings) implements CustomPacketPayload {
    public static final Type<ServerConfigPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(LocatorBar.MOD_ID, "server_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerConfigPayload> STREAM_CODEC = StreamCodec.ofMember(
            ServerConfigPayload::write,
            ServerConfigPayload::read
    );

    private static ServerConfigPayload read(RegistryFriendlyByteBuf buffer) {
        return new ServerConfigPayload(new ServerSettings(
                LocatorBarStyle.values()[buffer.readVarInt()],
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readVarInt()
        ));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(settings.style().ordinal());
        buffer.writeBoolean(settings.showCoordinates());
        buffer.writeBoolean(settings.showDays());
        buffer.writeBoolean(settings.showWorldDirections());
        buffer.writeBoolean(settings.showPlayerHeads());
        buffer.writeVarInt(settings.maxVisiblePlayers());
        buffer.writeFloat(settings.playerHeadFadeStartDistance());
        buffer.writeFloat(settings.playerHeadFadeToMinDistance());
        buffer.writeFloat(settings.playerHeadHideDistance());
        buffer.writeFloat(settings.playerHeadMinAlphaPercent());
        buffer.writeBoolean(settings.showWaypoints());
        buffer.writeVarInt(settings.maxVisibleWaypoints());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}