package com.frieren.magic;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

public class FlowerFocusItem extends FocusItem {
    public FlowerFocusItem() {
        super("flower_field", "Цветущий сад", Formatting.LIGHT_PURPLE, 60);
    }

    @Override
    public void cast(ServerWorld world, ServerPlayerEntity player, ItemStack staffStack) {
        FlowerFieldSpell.cast(player);
    }
}