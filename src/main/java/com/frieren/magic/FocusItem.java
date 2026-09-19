package com.frieren.magic;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class FocusItem extends Item {
    private final String spellId;
    private final String spellName;
    private final Formatting color;
    private final int cooldownTicks;

    public FocusItem(String spellId, String spellName, Formatting color, int cooldownTicks) {
        super(new Settings().maxCount(1).rarity(Rarity.RARE));
        this.spellId = spellId;
        this.spellName = spellName;
        this.color = color;
        this.cooldownTicks = cooldownTicks;
    }

    public String getSpellId() {
        return spellId;
    }

    public Text getDisplayName() {
        return Text.literal(spellName).formatted(color, Formatting.BOLD);
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public abstract void cast(ServerWorld world, ServerPlayerEntity player, ItemStack staffStack);

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("Магический набалдашник").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Вставляется в посох через Shift + ПКМ").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        super.appendTooltip(stack, world, tooltip, context);
    }
}