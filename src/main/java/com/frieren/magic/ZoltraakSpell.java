package com.frieren.magic;

import net.minecraft.entity.LivingEntity;
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

public class ZoltraakSpell {
    // Ярчайшие цвета для лучей
    private static final DustParticleEffect MAIN_BEAM_CORE = new DustParticleEffect(new Vector3f(1.0f, 1.0f, 1.0f), 2.5f);
    private static final DustParticleEffect MAIN_BEAM_GLOW = new DustParticleEffect(new Vector3f(1.0f, 0.8f, 0.1f), 3.5f);

    private static final DustParticleEffect BARRAGE_CORE = new DustParticleEffect(new Vector3f(1.0f, 1.0f, 1.0f), 1.0f);
    private static final DustParticleEffect BARRAGE_SPIRAL = new DustParticleEffect(new Vector3f(1.0f, 0.7f, 0.1f), 1.2f);

    // ==========================================
    // 1. ОДИНОЧНЫЙ ЗОЛТРААК (СОКРУШИТЕЛЬНЫЙ УДАР)
    // ==========================================
    public static void cast(ServerWorld world, ServerPlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d dir = player.getRotationVec(1.0f).normalize();
        double range = 60.0; // Увеличена дальность
        Vec3d end = start.add(dir.multiply(range));

        Box box = player.getBoundingBox().stretch(dir.multiply(range)).expand(2.0);
        EntityHitResult hit = ProjectileUtil.raycast(
                player, start, end, box,
                entity -> entity instanceof LivingEntity && !entity.isSpectator(),
                range * range
        );

        Vec3d hitPos = end;
        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            hitPos = hit.getPos().add(0, target.getHeight() / 2.0, 0);
            target.damage(world.getDamageSources().magic(), 22.0f); // Гром-урон
        }

        // Векторы для рисования перпендикулярных колец маны
        Vec3d right = dir.crossProduct(new Vec3d(0, 1, 0)).normalize();
        if (right.lengthSquared() < 0.001) right = dir.crossProduct(new Vec3d(1, 0, 0)).normalize();
        Vec3d up = right.crossProduct(dir).normalize();

        // 1. Магическое кольцо-печать на конце посоха (Muzzle ring)
        world.spawnParticles(ParticleTypes.FLASH, start.x + dir.x, start.y + dir.y, start.z + dir.z, 3, 0, 0, 0, 0);
        drawRing(world, start.add(dir.multiply(1.5)), right, up, 1.5, ParticleTypes.END_ROD, 30);

        // 2. Отрисовка гигантского многослойного луча
        double dist = start.distanceTo(hitPos);
        for (double d = 0.5; d < dist; d += 0.4) {
            Vec3d p = start.lerp(hitPos, d / dist);
            // Толстое белое ядро
            world.spawnParticles(MAIN_BEAM_CORE, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0);
            // Широкая золотая аура
            world.spawnParticles(MAIN_BEAM_GLOW, p.x, p.y, p.z, 3, 0.25, 0.25, 0.25, 0.02);
            // Проскакивающие молнии/искры
            if (world.random.nextFloat() < 0.2f) {
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.4, 0.4, 0.4, 0.1);
            }
        }

        // 3. Сокрушительный взрыв на месте попадания
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, hitPos.x, hitPos.y, hitPos.z, 1, 0, 0, 0, 0);
        drawRing(world, hitPos, right, up, 2.5, ParticleTypes.SOUL_FIRE_FLAME, 40);

        // 4. Кинематографичный звук (Смесь Соник-бума Вардена, грома и гула маяка)
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1.8f, 1.1f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.2f, 1.5f);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.5f, 0.7f);

        // 5. Кинетическая отдача (откидывает мага немного назад)
        player.addVelocity(-dir.x * 0.4, -dir.y * 0.4, -dir.z * 0.4);
        player.velocityModified = true;
    }

    // ==========================================
    // 2. ПУЛЕМЁТНАЯ ОЧЕРЕДЬ (АВТОМАТИЧЕСКИЙ ОГОНЬ)
    // ==========================================
    public static void castBarrage(ServerWorld world, ServerPlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);

        // Небольшой разброс снарядов
        double spread = 0.06;
        Vec3d dir = look.add(
                (world.random.nextDouble() - 0.5) * spread,
                (world.random.nextDouble() - 0.5) * spread,
                (world.random.nextDouble() - 0.5) * spread
        ).normalize();

        double range = 40.0;
        Vec3d end = start.add(dir.multiply(range));

        Box box = player.getBoundingBox().stretch(dir.multiply(range)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.raycast(
                player, start, end, box,
                entity -> entity instanceof LivingEntity && !entity.isSpectator(),
                range * range
        );

        Vec3d hitPos = end;
        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            hitPos = hit.getPos().add(0, target.getHeight() / 2, 0);
            target.damage(world.getDamageSources().magic(), 3.5f);
            target.timeUntilRegen = 0; // Плавление ХП без неуязвимости

            // Звук разрыва плоти/брони
            world.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                    SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.5f, 2.0f);
        }

        // Векторы для закручивания спирали
        Vec3d right = dir.crossProduct(new Vec3d(0, 1, 0)).normalize();
        if (right.lengthSquared() < 0.001) right = dir.crossProduct(new Vec3d(1, 0, 0)).normalize();
        Vec3d up = right.crossProduct(dir).normalize();

        double dist = start.distanceTo(hitPos);
        double spiralPhase = world.random.nextDouble() * Math.PI * 2; // Спираль начинается под случайным углом

        for (double d = 0.5; d < dist; d += 0.6) {
            Vec3d p = start.lerp(hitPos, d / dist);

            // Центр снаряда
            world.spawnParticles(BARRAGE_CORE, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);

            // Двойная спираль маны, обвивающая луч!
            double angle = d * 1.5 + spiralPhase;
            double radius = 0.25;
            Vec3d spiral1 = p.add(right.multiply(Math.cos(angle) * radius)).add(up.multiply(Math.sin(angle) * radius));
            Vec3d spiral2 = p.add(right.multiply(Math.cos(angle + Math.PI) * radius)).add(up.multiply(Math.sin(angle + Math.PI) * radius));

            world.spawnParticles(BARRAGE_SPIRAL, spiral1.x, spiral1.y, spiral1.z, 1, 0, 0, 0, 0);
            world.spawnParticles(BARRAGE_SPIRAL, spiral2.x, spiral2.y, spiral2.z, 1, 0, 0, 0, 0);
        }

        // Взрыв мелких осколков кристалла на конце
        world.spawnParticles(ParticleTypes.CRIT, hitPos.x, hitPos.y, hitPos.z, 8, 0.2, 0.2, 0.2, 0.1);

        // Жёсткий звук разряда (Гаст + раскалывающийся Аметист)
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.PLAYERS, 0.9f, 1.4f + (world.random.nextFloat() * 0.3f));
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.PLAYERS, 0.8f, 1.2f + (world.random.nextFloat() * 0.4f));
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНАЯ МАТЕМАТИКА (Рисование кругов)
    // ==========================================
    private static void drawRing(ServerWorld world, Vec3d center, Vec3d right, Vec3d up, double radius, net.minecraft.particle.ParticleEffect particle, int points) {
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            Vec3d p = center.add(right.multiply(Math.cos(angle) * radius)).add(up.multiply(Math.sin(angle) * radius));
            world.spawnParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0.01);
        }
    }
}