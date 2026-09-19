package com.frieren.magic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;

import java.util.*;

public class FlowerFieldSpell {

    // Только одноблочные растения: все ванильные цветы + цветущая азалия + кувшинки/споровые акценты
    private static final BlockState[] FLOWERS = new BlockState[] {
            Blocks.DANDELION.getDefaultState(),
            Blocks.POPPY.getDefaultState(),
            Blocks.BLUE_ORCHID.getDefaultState(),
            Blocks.ALLIUM.getDefaultState(),
            Blocks.AZURE_BLUET.getDefaultState(),
            Blocks.RED_TULIP.getDefaultState(),
            Blocks.ORANGE_TULIP.getDefaultState(),
            Blocks.WHITE_TULIP.getDefaultState(),
            Blocks.PINK_TULIP.getDefaultState(),
            Blocks.OXEYE_DAISY.getDefaultState(),
            Blocks.CORNFLOWER.getDefaultState(),
            Blocks.LILY_OF_THE_VALLEY.getDefaultState(),
            Blocks.TORCHFLOWER.getDefaultState(),
            Blocks.FLOWERING_AZALEA.getDefaultState()
    };

    private static final Random RANDOM = new Random();
    private static final List<ExpandingWave> ACTIVE_WAVES = new ArrayList<>();
    private static boolean tickRegistered = false;

    public static void init() {
        if (tickRegistered) return;
        tickRegistered = true;

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<ExpandingWave> iterator = ACTIVE_WAVES.iterator();
            while (iterator.hasNext()) {
                ExpandingWave wave = iterator.next();
                if (wave.tick()) {
                    iterator.remove();
                }
            }
        });
    }

    public static void cast(ServerPlayerEntity player) {
        if (player == null) return;
        ServerWorld world = player.getServerWorld();
        BlockPos center = player.getBlockPos();

        player.sendMessage(Text.literal("Цветущий сад... заклинание, которое любил Химмель.")
                .formatted(Formatting.AQUA, Formatting.ITALIC), true);

        // Старт: низкий глубокий гул магии и перезвон
        world.playSound(null, center, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 2.5f, 0.6f);
        world.playSound(null, center, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 2.0f, 0.5f);

        // Столп магических частиц прямо в игроке
        world.spawnParticles(ParticleTypes.ENCHANT, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                120, 0.5, 2.0, 0.5, 1.5);
        world.spawnParticles(ParticleTypes.END_ROD, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                40, 0.2, 1.5, 0.2, 0.05);

        // Умиротворение мобов в огромной зоне
        Box box = new Box(center).expand(200);
        List<HostileEntity> monsters = world.getEntitiesByClass(HostileEntity.class, box, entity -> true);
        for (HostileEntity monster : monsters) {
            monster.setTarget(null);
            world.spawnParticles(ParticleTypes.HEART,
                    monster.getX(), monster.getY() + monster.getHeight() + 0.4, monster.getZ(),
                    5, 0.3, 0.3, 0.3, 0.02);
        }

        // Запуск волны: радиус 200 блоков
        ACTIVE_WAVES.add(new ExpandingWave(world, center, 200));
    }

    private static class ExpandingWave {
        private final ServerWorld world;
        private final BlockPos center;
        private final int maxRadius;
        private double currentRadius = 1.0;
        private int tickCounter = 0;

        public ExpandingWave(ServerWorld world, BlockPos center, int maxRadius) {
            this.world = world;
            this.center = center;
            this.maxRadius = maxRadius;
        }

        public boolean tick() {
            tickCounter++;

            // Задержка: сажаем волну каждые 2 тика (10 волн в секунду) для плавной киношной анимации
            if (tickCounter % 2 != 0) {
                return false;
            }

            if (currentRadius > maxRadius) {
                onFinished();
                return true;
            }

            // Плотность: количество точек на текущем кольце зависит от радиуса
            int pointsOnRing = Math.max(16, (int) (currentRadius * 2.8));
            double angleStep = (2 * Math.PI) / pointsOnRing;

            for (int i = 0; i < pointsOnRing; i++) {
                // Небольшой разброс радиуса для естественной кучности без строгих кругов
                double r = currentRadius + (RANDOM.nextDouble() * 2.2 - 1.1);
                double angle = i * angleStep + (RANDOM.nextDouble() * 0.15);

                int x = (int) Math.round(center.getX() + r * Math.cos(angle));
                int z = (int) Math.round(center.getZ() + r * Math.sin(angle));

                // Проверяем только загруженные чанки, чтобы сервер не фризил
                if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;

                BlockPos surfacePos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
                BlockPos groundPos = surfacePos.down();

                BlockState ground = world.getBlockState(groundPos);
                if (ground.isOf(Blocks.GRASS_BLOCK) || ground.isOf(Blocks.DIRT) || ground.isOf(Blocks.MOSS_BLOCK)) {
                    if (world.isAir(surfacePos)) {
                        // Сажаем одноблочный цветок
                        BlockState flower = FLOWERS[RANDOM.nextInt(FLOWERS.length)];
                        world.setBlockState(surfacePos, flower, 2);

                        // Магическая анимация на каждом цветке: частицы лепестков сакуры и магии
                        world.spawnParticles(ParticleTypes.CHERRY_LEAVES,
                                surfacePos.getX() + 0.5, surfacePos.getY() + 0.3, surfacePos.getZ() + 0.5,
                                2, 0.25, 0.3, 0.25, 0.02);

                        if (RANDOM.nextFloat() < 0.25f) {
                            world.spawnParticles(ParticleTypes.WAX_ON,
                                    surfacePos.getX() + 0.5, surfacePos.getY() + 0.4, surfacePos.getZ() + 0.5,
                                    1, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                }
            }

            // Звуковое сопровождение по ходу расширения (плавно растет высота тона)
            if (tickCounter % 6 == 0) {
                float pitch = 0.8f + (float) (currentRadius / maxRadius) * 0.9f;
                world.playSound(null, center, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                        SoundCategory.PLAYERS, 1.2f, pitch);
            }

            // Шаг расширения радиуса за тик
            currentRadius += 1.4;
            return false;
        }

        private void onFinished() {
            // Финал заклинания: колокольный резонанс и взрыв света в небе
            world.playSound(null, center, SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.PLAYERS, 2.5f, 1.0f);
            world.playSound(null, center, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.2f, 1.1f);

            world.spawnParticles(ParticleTypes.FIREWORK,
                    center.getX() + 0.5, center.getY() + 2.5, center.getZ() + 0.5,
                    80, 2.0, 1.5, 2.0, 0.15);
        }
    }
}