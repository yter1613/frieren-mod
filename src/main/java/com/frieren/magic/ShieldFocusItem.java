package com.frieren.magic;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

public class ShieldFocusItem extends FocusItem {
    public ShieldFocusItem() {
        // 0 тиков кулдауна для мгновенного входа в блок
        super("shield", "Защитный барьер", Formatting.AQUA, 0);
    }

    @Override
    public void cast(ServerWorld world, ServerPlayerEntity player, ItemStack staffStack) {
        // Логика перенесена в usageTick посоха для непрерывного удержания
    }
}