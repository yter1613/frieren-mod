package com.frieren.magic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class FrierenModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(
                FrierenMod.FRIEREN_STAFF,
                new Identifier(FrierenMod.MOD_ID, "focus"),
                (stack, world, entity, seed) -> {
                    FocusItem focus = StaffItem.getAttachedFocus(stack);
                    if (focus == null) {
                        return 0.0f;
                    }
                    if (focus instanceof ZoltraakFocusItem) {
                        return 0.95f;
                    }
                    if (focus instanceof ShieldFocusItem) {
                        return 0.75f;
                    }
                    if (focus instanceof FlowerFocusItem) {
                        return 0.60f;
                    }
                    if (focus instanceof HealFocusItem) {
                        return 0.45f;
                    }
                    if (focus instanceof DigFocusItem) {
                        return 0.30f;
                    }
                    if (focus instanceof FlightFocusItem) {
                        return 0.15f;
                    }
                    return 0.0f;
                }
        );
    }
}