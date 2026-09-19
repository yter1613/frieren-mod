package com.frieren.magic;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlightSpell {
    private static final DustParticleEffect MANA_FLOW = new DustParticleEffect(new Vector3f(0.35f, 0.8f, 1.0f), 1.1f);
    private static final DustParticleEffect DASH_SPARK = new DustParticleEffect(new Vector3f(0.8f, 0.95f, 1.0f), 1.2f);

    private static final Map<UUID, Integer> DASH_CHARGES = new HashMap<>();
    private static final Map<UUID, Long> LAST_DASH_TIME = new HashMap<>();
    private static final int MAX_CHARGES = 3;
    private static final long DASH_COMBO_RESET_MS = 2500;

    public static void tickFlightPull(ServerWorld world, ServerPlayerEntity player) {
        Vec3d look = player.getRotationVec(1.0f).normalize();

        double speed = 1.1;
        player.setVelocity(look.multiply(speed));
        player.velocityModified = true;
        player.fallDistance = 0.0f;

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 20, 0, false, false, false));

        if (player.age % 2 == 0) {
            double r = 0.7;
            for (int i = 0; i < 6; i++) {
                double angle = i * (Math.PI / 3) + (player.age * 0.15);
                world.spawnParticles(MANA_FLOW,
                        player.getX() + Math.cos(angle) * r,
                        player.getY() + 0.15,
                        player.getZ() + Math.sin(angle) * r,
                        1, 0, 0, 0, 0);
            }
        }

        if (player.age % 14 == 0) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_ELYTRA_FLYING, SoundCategory.PLAYERS, 0.35f, 1.6f);
        }
    }

    public static void castDirectionalDash(ServerWorld world, ServerPlayerEntity player, StaffItem staffItem, float forward, float sideways) {
        UUID id = player.getUuid();
        long now = System.currentTimeMillis();

        if (now - LAST_DASH_TIME.getOrDefault(id, 0L) > DASH_COMBO_RESET_MS) {
            DASH_CHARGES.put(id, MAX_CHARGES);
        }

        int charges = DASH_CHARGES.getOrDefault(id, MAX_CHARGES);

        float yaw = player.getYaw();
        double radians = Math.toRadians(yaw);

        Vec3d forwardVec = new Vec3d(-Math.sin(radians), 0, Math.cos(radians));
        Vec3d rightVec = new Vec3d(Math.cos(radians), 0, Math.sin(radians));

        Vec3d targetDir = Vec3d.ZERO;
        if (forward != 0 || sideways != 0) {
            targetDir = forwardVec.multiply(forward).add(rightVec.multiply(-sideways)).normalize();
        } else {
            targetDir = player.getRotationVec(1.0f).normalize();
        }

        double power = 2.4;
        double vertical = Math.max(targetDir.y * 1.2, 0.32);
        player.setVelocity(targetDir.x * power, vertical, targetDir.z * power);
        player.velocityModified = true;
        player.fallDistance = 0.0f;

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 0.8f, 1.7f);

        // --- ОСТАТОЧНЫЙ СЛЕД (Afterimage Trail) ---
        Vec3d startPos = player.getPos();
        // Рассчитываем, куда именно улетит игрок с учётом силы рывка
        Vec3d endPos = startPos.add(targetDir.x * power, vertical, targetDir.z * power);
        double dist = startPos.distanceTo(endPos);

        // Рисуем непрерывную линию из висящей в воздухе маны (DustParticleEffect имеет 0 скорость и не разлетается)
        for (double d = 0; d <= dist; d += 0.2) {
            Vec3d p = startPos.lerp(endPos, d / dist);
            world.spawnParticles(MANA_FLOW, p.x, p.y + 1.0, p.z, 2, 0.15, 0.15, 0.15, 0);
            world.spawnParticles(DASH_SPARK, p.x, p.y + 1.0, p.z, 1, 0.05, 0.05, 0.05, 0);
        }

        // Вспышка только в конечной точке
        world.spawnParticles(ParticleTypes.FLASH, endPos.x, endPos.y + 1.0, endPos.z, 1, 0, 0, 0, 0);

        charges--;
        LAST_DASH_TIME.put(id, now);

        if (charges > 0) {
            DASH_CHARGES.put(id, charges);
            String dots = "◆ ".repeat(charges) + "◇ ".repeat(MAX_CHARGES - charges);
            player.sendMessage(Text.literal("✦ Рывок [" + dots.trim() + "]").formatted(Formatting.AQUA), true);
        } else {
            DASH_CHARGES.put(id, MAX_CHARGES);
            player.getItemCooldownManager().set(staffItem, 60);
            player.sendMessage(Text.literal("✧ Заряды рывка исчерпаны").formatted(Formatting.RED), true);
        }
    }
}