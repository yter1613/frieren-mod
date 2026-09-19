package com.frieren.magic;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class StaffItem extends Item {
    public static final String FOCUS_KEY = "ActiveFocusId";

    public StaffItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack staffStack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            if (world.isClient() && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                ClientStaffHandler.openRadialMenu();
            }
            return TypedActionResult.success(staffStack);
        }

        FocusItem focus = getAttachedFocus(staffStack);
        if (focus == null) {
            return TypedActionResult.pass(staffStack);
        }

        // Потоковые заклинания
        if (focus instanceof ShieldFocusItem || focus instanceof HealFocusItem || focus instanceof FlightFocusItem || focus instanceof ZoltraakFocusItem || focus instanceof DigFocusItem) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(staffStack);
        }

        // Одиночные (Цветение)
        if (!world.isClient() && world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity serverPlayer) {
            focus.cast(serverWorld, serverPlayer, staffStack);
            user.getItemCooldownManager().set(this, focus.getCooldownTicks());
        }

        return TypedActionResult.success(staffStack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            FocusItem focus = getAttachedFocus(stack);

            if (focus instanceof ShieldFocusItem) {
                ShieldSpell.tickShield(serverWorld, player);
            } else if (focus instanceof HealFocusItem) {
                HealSpell.tickHeal(serverWorld, player);
            } else if (focus instanceof FlightFocusItem) {
                int heldTicks = getMaxUseTime(stack) - remainingUseTicks;
                if (heldTicks >= 4) {
                    FlightSpell.tickFlightPull(serverWorld, player);
                }
            } else if (focus instanceof DigFocusItem) {
                DigSpell.tickDig(serverWorld, player);
            } else if (focus instanceof ZoltraakFocusItem) {
                int heldTicks = getMaxUseTime(stack) - remainingUseTicks;
                if (heldTicks >= 4 && heldTicks % 3 == 0) {
                    ZoltraakSpell.castBarrage(serverWorld, player);
                }
            }
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        FocusItem focus = getAttachedFocus(stack);
        int heldTicks = getMaxUseTime(stack) - remainingUseTicks;

        if (focus instanceof FlightFocusItem) {
            if (heldTicks < 4) {
                if (world.isClient() && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                    MovementNetwork.sendClientDashInput();
                }
            }
        } else if (focus instanceof ZoltraakFocusItem) {
            if (!world.isClient() && world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
                if (heldTicks < 4) {
                    focus.cast(serverWorld, player, stack);
                    player.getItemCooldownManager().set(this, focus.getCooldownTicks());
                } else {
                    player.getItemCooldownManager().set(this, 15);
                }
            }
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        FocusItem focus = getAttachedFocus(stack);
        if (focus instanceof ShieldFocusItem) {
            return UseAction.BLOCK;
        }
        if (focus instanceof FlightFocusItem) {
            return UseAction.SPEAR;
        }
        if (focus instanceof HealFocusItem || focus instanceof ZoltraakFocusItem || focus instanceof DigFocusItem) {
            return UseAction.BOW;
        }
        return UseAction.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        FocusItem focus = getAttachedFocus(stack);
        if (focus instanceof ShieldFocusItem || focus instanceof HealFocusItem || focus instanceof FlightFocusItem || focus instanceof ZoltraakFocusItem || focus instanceof DigFocusItem) {
            return 72000;
        }
        return 0;
    }

    public static FocusItem getAttachedFocus(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(FOCUS_KEY)) {
            String idStr = nbt.getString(FOCUS_KEY);
            if (!idStr.isEmpty() && !idStr.equals("none")) {
                Item item = Registries.ITEM.get(new Identifier(idStr));
                if (item instanceof FocusItem focus) {
                    return focus;
                }
            }
        }
        return null;
    }

    public static void setAttachedFocus(ItemStack stack, String focusId) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (focusId == null || focusId.isEmpty() || focusId.equals("none")) {
            nbt.remove(FOCUS_KEY);
        } else {
            nbt.putString(FOCUS_KEY, focusId);
        }
    }
}