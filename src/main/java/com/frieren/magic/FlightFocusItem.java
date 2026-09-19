package com.frieren.magic;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

public class FlightFocusItem extends FocusItem {
    public FlightFocusItem() {
        super("flight", "Магия полёта", Formatting.BLUE, 0);
    }

    @Override
    public void cast(ServerWorld world, ServerPlayerEntity player, ItemStack staffStack) {
        // Вся логика выполняется в StaffItem (короткий клик = рывок, удержание = тяга)
    }
}