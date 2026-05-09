package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pl.fuzjajadrowa.locatorbar.network.PlayerLocatorPayload;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PlayerLocatorClient {
    private static final long MAX_PAYLOAD_AGE_MILLIS = 2000L;
    private static List<PlayerLocatorPayload.Entry> entries = List.of();
    private static long lastUpdateMillis;

    private PlayerLocatorClient() {
    }

    public static void apply(PlayerLocatorPayload payload) {
        entries = List.copyOf(payload.entries());
        lastUpdateMillis = System.currentTimeMillis();
    }

    public static void clear() {
        entries = List.of();
        lastUpdateMillis = 0L;
    }

    public static List<Marker> collectMarkers(Player localPlayer, AlphaFunction alphaFunction) {
        if (entries.isEmpty() || System.currentTimeMillis() - lastUpdateMillis > MAX_PAYLOAD_AGE_MILLIS) {
            return collectEntityMarkers(localPlayer, alphaFunction);
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return List.of();
        }

        List<Marker> markers = new ArrayList<>();
        for (PlayerLocatorPayload.Entry entry : entries) {
            double dx = entry.x() - localPlayer.getX();
            double dz = entry.z() - localPlayer.getZ();
            if (dx * dx + dz * dz < 1.0E-6D) {
                continue;
            }

            float distance = (float) Math.sqrt(dx * dx + dz * dz);
            float alpha = alphaFunction.compute(distance);
            if (alpha <= 0.0F) {
                continue;
            }

            PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(entry.playerId());
            if (playerInfo == null) {
                continue;
            }

            PlayerSkin playerSkin = playerInfo.getSkin();
            float directionYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            markers.add(new Marker(skinTexture(playerSkin), directionYaw, alpha, distance));
        }

        markers.sort(Comparator.comparingDouble(Marker::distance));
        return markers;
    }

    private static List<Marker> collectEntityMarkers(Player localPlayer, AlphaFunction alphaFunction) {
        List<Marker> markers = new ArrayList<>();
        for (Player otherPlayer : localPlayer.level().players()) {
            if (otherPlayer == localPlayer || shouldHidePlayerHead(localPlayer, otherPlayer)) {
                continue;
            }

            double dx = otherPlayer.getX() - localPlayer.getX();
            double dz = otherPlayer.getZ() - localPlayer.getZ();
            if (dx * dx + dz * dz < 1.0E-6D) {
                continue;
            }

            float distance = (float) Math.sqrt(dx * dx + dz * dz);
            float alpha = alphaFunction.compute(distance);
            if (alpha <= 0.0F) {
                continue;
            }

            PlayerSkin playerSkin = Minecraft.getInstance().getSkinManager()
                    //? if >=1.21.11
                    .createLookup(otherPlayer.getGameProfile(), false).get();
                    //? if <1.21.11
                    /*.getInsecureSkin(otherPlayer.getGameProfile());*/
            float directionYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            markers.add(new Marker(skinTexture(playerSkin), directionYaw, alpha, distance));
        }

        markers.sort(Comparator.comparingDouble(Marker::distance));
        return markers;
    }

    private static boolean shouldHidePlayerHead(Player localPlayer, Player otherPlayer) {
        if (!otherPlayer.level().dimension().equals(localPlayer.level().dimension())) {
            return true;
        }
        if (otherPlayer.isCrouching()) {
            return true;
        }

        ItemStack helmet = otherPlayer.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) {
            return false;
        }

        Item helmetItem = helmet.getItem();
        return helmetItem == Items.CARVED_PUMPKIN
                || helmetItem == Items.SKELETON_SKULL
                || helmetItem == Items.WITHER_SKELETON_SKULL
                || helmetItem == Items.ZOMBIE_HEAD
                || helmetItem == Items.CREEPER_HEAD
                || helmetItem == Items.DRAGON_HEAD
                || helmetItem == Items.PIGLIN_HEAD;
    }

    private static Identifier skinTexture(PlayerSkin playerSkin) {
        //? if >=1.21.11
        return playerSkin.body().texturePath();
        //? if <1.21.11
        /*return playerSkin.texture();*/
    }

    @FunctionalInterface
    public interface AlphaFunction {
        float compute(float distance);
    }

    public record Marker(Identifier skinTexture, float directionYaw, float alpha, float distance) {
    }
}