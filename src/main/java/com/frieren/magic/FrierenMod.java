package com.frieren.magic;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class FrierenMod implements ModInitializer {
    public static final String MOD_ID = "frierenmagic";

    public static final Item FRIEREN_STAFF = new StaffItem(new Item.Settings());
    public static final Item FLOWER_FOCUS = new FlowerFocusItem();
    public static final Item ZOLTRAAK_FOCUS = new ZoltraakFocusItem();
    public static final Item SHIELD_FOCUS = new ShieldFocusItem();
    public static final Item HEAL_FOCUS = new HealFocusItem();
    public static final Item FLIGHT_FOCUS = new FlightFocusItem();
    public static final Item DIG_FOCUS = new DigFocusItem();

    @Override
    public void onInitialize() {
        FlowerFieldSpell.init();
        FocusNetwork.initServer();
        MovementNetwork.initServer();

        // Инициализируем обработчик награды за победу над Эндер-драконом
        DragonRewardHandler.init();

        // Генерация Церкви и Великого Древа в одном направлении при старте мира
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld overworld = server.getOverworld();
            FrierenWorldState state = FrierenWorldState.getServerState(overworld);

            // 1. Церковь Фрирен (генерирует и сохраняет направление)
            if (!state.houseGenerated) {
                FrierenHouseGenerator.generateHouse(overworld, state);
                state.houseGenerated = true;
                state.markDirty();
            }

            // 2. Великое Древо-Данж (использует то же направление)
            if (!state.treeGenerated) {
                GiantTreeGenerator.generateTree(overworld, state);
                state.treeGenerated = true;
                state.markDirty();
            }
        });

        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "frieren_staff"), FRIEREN_STAFF);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "flower_focus"), FLOWER_FOCUS);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "zoltraak_focus"), ZOLTRAAK_FOCUS);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "shield_focus"), SHIELD_FOCUS);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "heal_focus"), HEAL_FOCUS);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "flight_focus"), FLIGHT_FOCUS);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "dig_focus"), DIG_FOCUS);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(FRIEREN_STAFF);
            content.add(FLOWER_FOCUS);
            content.add(ZOLTRAAK_FOCUS);
            content.add(SHIELD_FOCUS);
            content.add(HEAL_FOCUS);
            content.add(FLIGHT_FOCUS);
            content.add(DIG_FOCUS);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("flowerfield")
                    .executes(context -> {
                        FlowerFieldSpell.cast(context.getSource().getPlayer());
                        return 1;
                    }));
        });
    }
}