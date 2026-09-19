package com.frieren.magic;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

public class HealFocusItem extends FocusItem {
    public HealFocusItem() {
        super("heal", "Исцеление", Formatting.GREEN, 0);
    }

    @Override
    public void cast(ServerWorld world, ServerPlayerEntity player, ItemStack staffStack) {
        // Логика выполняется непрерывно через usageTick
    }
}