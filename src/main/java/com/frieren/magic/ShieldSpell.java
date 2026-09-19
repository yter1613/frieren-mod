package com.frieren.magic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.List;

public class ShieldSpell {
    private static final DustParticleEffect SHIELD_PARTICLE = new DustParticleEffect(new Vector3f(0.25f, 0.85f, 0.95f), 1.0f);
    private static final DustParticleEffect SHIELD_EDGE = new DustParticleEffect(new Vector3f(0.85f, 0.98f, 1.0f), 0.8f);

    public static void tickShield(ServerWorld world, ServerPlayerEntity player) {
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0f).normalize();
        Vec3d shieldCenter = eyePos.add(lookVec.multiply(1.7));

        Vec3d rightVec = lookVec.crossProduct(new Vec3d(0, 1, 0)).normalize();
        if (rightVec.lengthSquared() < 0.001) {
            rightVec = lookVec.crossProduct(new Vec3d(1, 0, 0)).normalize();
        }
        Vec3d normalUp = rightVec.crossProduct(lookVec).normalize();

        // Фоновый тихий гул щита каждые 20 тиков (1 секунда)
        if (player.age % 20 == 0) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.PLAYERS, 0.4f, 1.8f);
        }

        // Отрисовка контура шестиугольного барьера
        int sides = 6;
        double radius = 1.3;
        for (int i = 0; i < sides; i++) {
            double a1 = (2 * Math.PI / sides) * i;
            double a2 = (2 * Math.PI / sides) * (i + 1);

            Vec3d p1 = shieldCenter.add(rightVec.multiply(Math.cos(a1) * radius)).add(normalUp.multiply(Math.sin(a1) * radius));
            Vec3d p2 = shieldCenter.add(rightVec.multiply(Math.cos(a2) * radius)).add(normalUp.multiply(Math.sin(a2) * radius));

            for (double step = 0; step <= 1.0; step += 0.35) {
                Vec3d point = p1.lerp(p2, step);
                world.spawnParticles(SHIELD_EDGE, point.x, point.y, point.z, 1, 0, 0, 0, 0);
            }
        }

        // Мягкое внутреннее мерцание
        world.spawnParticles(SHIELD_PARTICLE, shieldCenter.x, shieldCenter.y, shieldCenter.z, 2, 0.3, 0.3, 0.3, 0.0);

        // Перехват и отражение снарядов
        Box interceptBox = new Box(shieldCenter.x - 1.5, shieldCenter.y - 1.5, shieldCenter.z - 1.5,
                shieldCenter.x + 1.5, shieldCenter.y + 1.5, shieldCenter.z + 1.5);

        List<Entity> projectiles = world.getOtherEntities(player, interceptBox, e -> e instanceof ProjectileEntity);
        for (Entity p : projectiles) {
            Vec3d v = p.getVelocity();
            // Разворачиваем снаряд точно в обратную сторону с ускорением
            p.setVelocity(-v.x * 1.1, -v.y * 1.1 + 0.1, -v.z * 1.1);
            p.velocityModified = true;

            world.playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.PLAYERS, 1.4f, 2.0f);
            world.playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 1.0f, 1.2f);

            world.spawnParticles(ParticleTypes.FLASH, p.getX(), p.getY(), p.getZ(), 1, 0, 0, 0, 0);
        }
    }
}