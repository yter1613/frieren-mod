package com.frieren.magic;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class DragonRewardHandler {
    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof EnderDragonEntity && entity.getWorld().getRegistryKey() == World.END) {
                // Когда дракон повержен, дадим ближайшему игроку или всем в Энде цветочный фокус
                for (ServerPlayerEntity player : entity.getWorld().getEntitiesByClass(ServerPlayerEntity.class, entity.getBoundingBox().expand(100.0), p -> true)) {
                    ItemStack flowerFocus = new ItemStack(FrierenMod.FLOWER_FOCUS);
                    if (!player.getInventory().insertStack(flowerFocus)) {
                        player.dropItem(flowerFocus, false);
                    }
                    player.sendMessage(net.minecraft.text.Text.literal("§d[FrierenMagic] Вы победили Дракона Края! Магия Цветочных Полей разблокирована!"), false);
                }
            }
        });
    }
}