package com.frieren.magic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class FocusNetwork {
    // Идентификатор пакета
    public static final Identifier CHANGE_FOCUS = new Identifier(FrierenMod.MOD_ID, "change_focus");

    // ==========================================
    // СЕРВЕРНАЯ ЧАСТЬ (Принимает пакет и меняет предметы)
    // ==========================================
    public static void initServer() {
        ServerPlayNetworking.registerGlobalReceiver(CHANGE_FOCUS, (server, player, handler, buf, responseSender) -> {
            String newFocusId = buf.readString();

            server.execute(() -> {
                ItemStack staff = player.getMainHandStack();
                if (!(staff.getItem() instanceof StaffItem)) {
                    staff = player.getOffHandStack();
                }
                if (!(staff.getItem() instanceof StaffItem)) return;

                String oldFocusId = "none";
                if (staff.hasNbt() && staff.getNbt().contains(StaffItem.FOCUS_KEY)) {
                    oldFocusId = staff.getNbt().getString(StaffItem.FOCUS_KEY);
                    if (oldFocusId.isEmpty()) oldFocusId = "none";
                }

                if (oldFocusId.equals(newFocusId)) return;

                // 1. ВОЗВРАЩАЕМ СТАРЫЙ НАБАЛДАШНИК В ИНВЕНТАРЬ
                if (!oldFocusId.equals("none")) {
                    Item oldItem = Registries.ITEM.get(new Identifier(oldFocusId));
                    if (oldItem != Items.AIR) {
                        ItemStack returnStack = new ItemStack(oldItem);
                        if (!player.getInventory().insertStack(returnStack)) {
                            player.dropItem(returnStack, false, true);
                        }
                    }
                }

                // 2. ЗАБИРАЕМ НОВЫЙ НАБАЛДАШНИК ИЗ ИНВЕНТАРЯ
                if (!newFocusId.equals("none") && !player.isCreative()) {
                    Item newItem = Registries.ITEM.get(new Identifier(newFocusId));
                    if (newItem != Items.AIR) {
                        int slot = player.getInventory().getSlotWithStack(new ItemStack(newItem));
                        if (slot != -1) {
                            player.getInventory().getStack(slot).decrement(1);
                        } else {
                            StaffItem.setAttachedFocus(staff, "none");
                            return;
                        }
                    }
                }

                // 3. ОБНОВЛЯЕМ NBT ПОСОХА
                StaffItem.setAttachedFocus(staff, newFocusId);
            });
        });
    }

    // ==========================================
    // КЛИЕНТСКАЯ ЧАСТЬ (Отправляет пакет при клике в меню)
    // ==========================================
    @Environment(EnvType.CLIENT)
    public static void sendSelectPacket(String focusId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(focusId);
        ClientPlayNetworking.send(CHANGE_FOCUS, buf);
    }
}