package com.frieren.magic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class HealSpell {
    // Изумрудно-золотые частицы священной маны
    private static final DustParticleEffect HEAL_RAY = new DustParticleEffect(new Vector3f(0.3f, 0.95f, 0.5f), 1.0f);
    private static final DustParticleEffect HEAL_SPARKLE = new DustParticleEffect(new Vector3f(1.0f, 0.9f, 0.3f), 0.8f);

    public static void tickHeal(ServerWorld world, ServerPlayerEntity caster) {
        Vec3d start = caster.getEyePos();
        Vec3d look = caster.getRotationVec(1.0f);
        double range = 8.0;
        Vec3d end = start.add(look.multiply(range));

        Box box = caster.getBoundingBox().stretch(look.multiply(range)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.raycast(
                caster,
                start,
                end,
                box,
                entity -> entity instanceof LivingEntity && !entity.isSpectator(),
                range * range
        );

        LivingEntity target = caster; // По умолчанию лечим себя

        if (hit != null && hit.getEntity() instanceof LivingEntity livingTarget) {
            target = livingTarget;
            // Рисуем луч маны от посоха к цели
            Vec3d targetPos = target.getEyePos();
            double dist = start.distanceTo(targetPos);
            for (double d = 0.5; d < dist; d += 0.4) {
                Vec3d p = start.lerp(targetPos, d / dist);
                world.spawnParticles(HEAL_RAY, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }

        // Накладываем эффекты II уровня (время 40 тиков = 2 сек, чтобы они не спадали при удержании)
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 1, false, false, true));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 40, 1, false, false, true));

        // Эффекты вокруг исцеляемого
        world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ(), 2, 0.4, 0.4, 0.4, 0.05);
        world.spawnParticles(HEAL_SPARKLE, target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ(), 3, 0.3, 0.5, 0.3, 0.02);

        // Звук исцеления каждые 15 тиков
        if (caster.age % 15 == 0) {
            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.8f, 1.6f);
        }
    }
}