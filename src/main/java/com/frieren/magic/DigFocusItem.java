package com.frieren.magic;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

public class DigFocusItem extends FocusItem {
    public DigFocusItem() {
        super("dig", "Магия терраформирования", Formatting.GOLD, 0);
    }

    @Override
    public void cast(ServerWorld world, ServerPlayerEntity player, ItemStack staffStack) {
        // Логика потоковая, выполняется в StaffItem
    }
}