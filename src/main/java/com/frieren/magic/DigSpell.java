package com.frieren.magic;

import net.minecraft.block.BlockState;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;

public class DigSpell {
    // Топазово-янтарные частицы для луча
    private static final DustParticleEffect DIG_BEAM = new DustParticleEffect(new Vector3f(1.0f, 0.6f, 0.1f), 1.0f);
    private static final DustParticleEffect DIG_SPARK = new DustParticleEffect(new Vector3f(1.0f, 0.8f, 0.2f), 1.2f);

    public static void tickDig(ServerWorld world, ServerPlayerEntity player) {
        // Копаем не каждый тик (чтобы не было жутких лагов), а каждые 4 тика (5 раз в секунду)
        boolean shouldBreak = player.age % 4 == 0;

        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        double range = 8.0; // Дальность действия луча
        Vec3d end = start.add(look.multiply(range));

        // Пускаем луч, чтобы найти блок, на который смотрит игрок
        BlockHitResult hit = world.raycast(new RaycastContext(
                start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player
        ));

        Vec3d hitPos = end;

        if (hit.getType() == HitResult.Type.BLOCK) {
            hitPos = hit.getPos();
            BlockPos centerPos = hit.getBlockPos();
            Direction face = hit.getSide();

            if (shouldBreak) {
                // Определяем плоскость копания 3х3 в зависимости от того, в какую грань блока смотрим
                for (int a = -1; a <= 1; a++) {
                    for (int b = -1; b <= 1; b++) {
                        BlockPos targetPos = switch (face.getAxis()) {
                            case X -> centerPos.add(0, a, b);
                            case Y -> centerPos.add(a, 0, b);
                            case Z -> centerPos.add(a, b, 0);
                        };

                        BlockState state = world.getBlockState(targetPos);

                        // ПРОВЕРКА ПРОЧНОСТИ:
                        // state.getHardness() возвращает -1.0f для неразрушимых блоков (Бедрок, Рамка портала)
                        if (!state.isAir() && state.getHardness(world, targetPos) >= 0.0f) {
                            // Имитируем добычу (с выпадением лута и частицами самого блока)
                            world.breakBlock(targetPos, true, player);
                        }
                    }
                }

                // Звук крошения камня и гул магии
                world.playSound(null, centerPos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1.0f, 0.8f);
                world.playSound(null, centerPos, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.PLAYERS, 0.5f, 1.5f);

                // Вспышка на месте разрушения
                world.spawnParticles(ParticleTypes.EXPLOSION, hitPos.x, hitPos.y, hitPos.z, 1, 0, 0, 0, 0);
            }

            // Искры распыления породы (каждый тик для визуала)
            world.spawnParticles(DIG_SPARK, hitPos.x, hitPos.y, hitPos.z, 2, 0.3, 0.3, 0.3, 0.05);
        }

        // Отрисовка топазового луча магии (каждый тик)
        double dist = start.distanceTo(hitPos);
        for (double d = 0.5; d < dist; d += 0.5) {
            Vec3d p = start.lerp(hitPos, d / dist);
            world.spawnParticles(DIG_BEAM, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0);
        }
    }
}