package com.frieren.magic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class MovementNetwork {
    public static final Identifier DASH_DIR_PACKET = new Identifier(FrierenMod.MOD_ID, "dash_dir_packet");

    public static void initServer() {
        ServerPlayNetworking.registerGlobalReceiver(DASH_DIR_PACKET, (server, player, handler, buf, responseSender) -> {
            float moveForward = buf.readFloat();
            float moveSideways = buf.readFloat();

            server.execute(() -> {
                ItemStack stack = player.getMainHandStack();
                if (stack.getItem() instanceof StaffItem staffItem) {
                    FlightSpell.castDirectionalDash(player.getServerWorld(), player, staffItem, moveForward, moveSideways);
                }
            });
        });
    }

    @Environment(EnvType.CLIENT)
    public static void sendClientDashInput() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.input == null) return;

        float forward = 0;
        if (client.player.input.pressingForward) forward += 1;
        if (client.player.input.pressingBack) forward -= 1;

        float sideways = 0;
        if (client.player.input.pressingLeft) sideways += 1;
        if (client.player.input.pressingRight) sideways -= 1;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(forward);
        buf.writeFloat(sideways);
        ClientPlayNetworking.send(DASH_DIR_PACKET, buf);
    }
}