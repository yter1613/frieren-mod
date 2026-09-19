package com.frieren.magic;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

public class ZoltraakFocusItem extends FocusItem {
    public ZoltraakFocusItem() {
        super("zoltraak", "Золтраак", Formatting.GOLD, 25);
    }

    @Override
    public void cast(ServerWorld world, ServerPlayerEntity player, ItemStack staffStack) {
        ZoltraakSpell.cast(world, player);
    }
}