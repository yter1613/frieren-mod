package com.frieren.magic;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public class GiantTreeGenerator {

    public static void generateTree(ServerWorld world, FrierenWorldState state) {
        BlockPos spawnPos = world.getSpawnPos();
        Random random = world.getRandom();

        double angle = (state.generationAngle >= 0) ? state.generationAngle : random.nextDouble() * 2 * Math.PI;

        int distance = 200 + random.nextInt(100);
        int offsetX = (int) (Math.cos(angle) * distance);
        int offsetZ = (int) (Math.sin(angle) * distance);

        BlockPos surfacePos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, spawnPos.add(offsetX, 0, offsetZ));
        buildGreatTree(world, surfacePos, random);
    }

    private static void buildGreatTree(ServerWorld world, BlockPos startPos, Random random) {
        int height = 50;
        int maxRadius = 10;
        BlockState log = Blocks.DARK_OAK_LOG.getDefaultState();
        BlockState leaves = Blocks.DARK_OAK_LEAVES.getDefaultState().with(net.minecraft.block.LeavesBlock.PERSISTENT, true);
        BlockState air = Blocks.AIR.getDefaultState();

        // 0. КОРНЕВЫЕ ЛАПЫ
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                double dist = Math.sqrt(x * x + z * z);
                boolean isRootBeam = (Math.abs(x) <= 2 || Math.abs(z) <= 2) && dist <= 11;

                if (dist <= maxRadius + 2 || isRootBeam) {
                    for (int y = -4; y < 0; y++) {
                        BlockPos p = startPos.add(x, y, z);
                        if (world.getBlockState(p).isAir() || world.getBlockState(p).isOf(Blocks.WATER) || world.getBlockState(p).isOf(Blocks.DIRT)) {
                            world.setBlockState(p, Blocks.DIRT.getDefaultState(), 2);
                        }
                    }
                    if (isRootBeam && dist <= 9) {
                        world.setBlockState(startPos.add(x, 0, z), log, 2);
                    }
                }
            }
        }

        // 1. СТВОЛ И ПОЛОСТЬ
        for (int y = -3; y <= height; y++) {
            double currentRadius = maxRadius * (1.0 - (double) y / (height * 1.2));
            if (currentRadius < 3.5) currentRadius = 3.5;
            double innerRadius = currentRadius - 1.8;

            for (int x = -12; x <= 12; x++) {
                for (int z = -12; z <= 12; z++) {
                    double noiseOffset = Math.sin(y * 0.3 + x * 0.5) * 0.5;
                    double dist = Math.sqrt(x * x + z * z) + noiseOffset;
                    BlockPos pos = startPos.add(x, y, z);

                    if (dist <= currentRadius && dist >= innerRadius) {
                        world.setBlockState(pos, log, 2);

                        if (random.nextFloat() < 0.08f && y < 30) {
                            world.setBlockState(pos.add(x > 0 ? 1 : -1, 0, 0), Blocks.VINE.getDefaultState(), 2);
                        }
                    } else if (dist < innerRadius && y > 0 && y < 42) {
                        world.setBlockState(pos, air, 2);
                    }
                }
            }

            if (y > 0 && y < 42) {
                double angle = y * 0.45;
                int sx1 = (int) (Math.cos(angle) * (innerRadius - 0.8));
                int sz1 = (int) (Math.sin(angle) * (innerRadius - 0.8));
                world.setBlockState(startPos.add(sx1, y, sz1), Blocks.DARK_OAK_SLAB.getDefaultState(), 2);

                int sx2 = (int) (Math.cos(angle + 0.2) * (innerRadius - 0.8));
                int sz2 = (int) (Math.sin(angle + 0.2) * (innerRadius - 0.8));
                world.setBlockState(startPos.add(sx2, y, sz2), Blocks.DARK_OAK_SLAB.getDefaultState(), 2);
            }

            if (y == 12 || y == 28) {
                for (int dx = -5; dx <= 5; dx++) {
                    for (int dz = -5; dz <= 5; dz++) {
                        if (Math.sqrt(dx * dx + dz * dz) <= innerRadius) {
                            world.setBlockState(startPos.add(dx, y, dz), Blocks.DARK_OAK_PLANKS.getDefaultState(), 2);
                        }
                    }
                }
                world.setBlockState(startPos.add(0, y, 0), air, 2);
            }
        }

        // ЭТАЖ 1 (Пауки)
        BlockPos spawnerPos1 = startPos.add(-2, 13, -2);
        world.setBlockState(spawnerPos1, Blocks.SPAWNER.getDefaultState(), 2);
        if (world.getBlockEntity(spawnerPos1) instanceof MobSpawnerBlockEntity spawner) {
            spawner.setEntityType(EntityType.CAVE_SPIDER, random);
        }
        for (int i = 0; i < 15; i++) {
            world.setBlockState(startPos.add(random.nextInt(6) - 3, 13 + random.nextInt(3), random.nextInt(6) - 3), Blocks.COBWEB.getDefaultState(), 2);
        }

        // ЭТАЖ 2 (Ловушка с TNT)
        BlockPos spawnerPos2 = startPos.add(2, 29, 2);
        world.setBlockState(spawnerPos2, Blocks.SPAWNER.getDefaultState(), 2);
        if (world.getBlockEntity(spawnerPos2) instanceof MobSpawnerBlockEntity spawner) {
            spawner.setEntityType(EntityType.ZOMBIE, random);
        }
        BlockPos trapPos = startPos.add(-2, 29, 0);
        world.setBlockState(trapPos, Blocks.STONE_PRESSURE_PLATE.getDefaultState(), 2);
        world.setBlockState(trapPos.down(), Blocks.TNT.getDefaultState(), 2);

        // --- ЖИЛАЯ КОМНАТА ВНУТРИ ЛИСТВЫ НА ВЕРШИНЕ (Y = 42 - 46) ---
        int roomY = 42;

        BlockPos roomCenter = startPos.add(0, roomY + 2, 0);
        int leafRadius = 7;

        for (int lx = -leafRadius; lx <= leafRadius; lx++) {
            for (int ly = -leafRadius; ly <= leafRadius; ly++) {
                for (int lz = -leafRadius; lz <= leafRadius; lz++) {
                    double dist = Math.sqrt(lx * lx + (ly * 1.3) * (ly * 1.3) + lz * lz);
                    if (dist <= leafRadius) {
                        BlockPos p = roomCenter.add(lx, ly, lz);
                        if (dist >= leafRadius - 1.8) {
                            world.setBlockState(p, leaves, 2);
                        } else {
                            world.setBlockState(p, log, 2);
                        }
                    }
                }
            }
        }

        for (int rx = -4; rx <= 4; rx++) {
            for (int rz = -4; rz <= 4; rz++) {
                world.setBlockState(startPos.add(rx, roomY, rz), Blocks.DARK_OAK_PLANKS.getDefaultState(), 2);

                for (int ry = 1; ry <= 4; ry++) {
                    world.setBlockState(startPos.add(rx, roomY + ry, rz), air, 2);
                }
            }
        }

        for (int d = -1; d <= 1; d++) {
            world.setBlockState(startPos.add(d, roomY + 1, 4), air, 2);
            world.setBlockState(startPos.add(d, roomY + 2, 4), air, 2);
            world.setBlockState(startPos.add(d, roomY + 1, -4), air, 2);
            world.setBlockState(startPos.add(d, roomY + 2, -4), air, 2);
            world.setBlockState(startPos.add(4, roomY + 1, d), air, 2);
            world.setBlockState(startPos.add(4, roomY + 2, d), air, 2);
            world.setBlockState(startPos.add(-4, roomY + 1, d), air, 2);
            world.setBlockState(startPos.add(-4, roomY + 2, d), air, 2);
        }

        world.setBlockState(startPos.add(-2, roomY + 1, -2), Blocks.RED_BED.getDefaultState().with(net.minecraft.block.BedBlock.PART, net.minecraft.block.enums.BedPart.FOOT), 2);
        world.setBlockState(startPos.add(-2, roomY + 1, -3), Blocks.RED_BED.getDefaultState().with(net.minecraft.block.BedBlock.PART, net.minecraft.block.enums.BedPart.HEAD), 2);
        world.setBlockState(startPos.add(2, roomY + 1, -2), Blocks.CRAFTING_TABLE.getDefaultState(), 2);
        world.setBlockState(startPos.add(2, roomY + 1, -3), Blocks.FURNACE.getDefaultState(), 2);
        world.setBlockState(startPos.add(3, roomY + 1, -3), Blocks.BOOKSHELF.getDefaultState(), 2);

        BlockPos chestPos1 = startPos.add(-2, roomY + 1, 2);
        BlockPos chestPos2 = startPos.add(2, roomY + 1, 2);
        world.setBlockState(chestPos1, Blocks.CHEST.getDefaultState(), 2);
        world.setBlockState(chestPos2, Blocks.CHEST.getDefaultState(), 2);

        if (world.getBlockEntity(chestPos1) instanceof ChestBlockEntity chest1 &&
                world.getBlockEntity(chestPos2) instanceof ChestBlockEntity chest2) {
            populateDungeonChest(chest1, chest2, random);
        }

        BlockPos topPos = startPos.add(0, height + 2, 0);
        int[] clustersX = {7, -7, 0, 0, 10, -10, 6, -6};
        int[] clustersZ = {0, 0, 7, -7, 6, -6, -10, 10};
        for (int i = 0; i < clustersX.length; i++) {
            generateLeafCluster(world, topPos.add(clustersX[i], -2, clustersZ[i]), 9, leaves, log, random);
        }

        for (int h = 0; h < 3; h++) {
            world.setBlockState(startPos.add(0, h, maxRadius - 1), air, 2);
            world.setBlockState(startPos.add(0, h, maxRadius - 2), air, 2);
        }

        System.out.println("[FrierenMagic] Сказочное Великое Древо создано на: " + startPos.toShortString());
    }

    private static void generateLeafCluster(ServerWorld world, BlockPos center, int radius, BlockState leaves, BlockState log, Random random) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius / 2; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + (y * 1.5) * (y * 1.5) + z * z <= radius * radius) {
                        BlockPos leafPos = center.add(x, y, z);
                        if (world.getBlockState(leafPos).isAir()) {
                            if (Math.abs(x) <= 1 && Math.abs(z) <= 1 && y <= 0) {
                                world.setBlockState(leafPos, log, 2);
                            } else {
                                world.setBlockState(leafPos, leaves, 2);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void populateDungeonChest(ChestBlockEntity chest1, ChestBlockEntity chest2, Random random) {
        String[] focuses = {
                "frierenmagic:zoltraak_focus",
                "frierenmagic:shield_focus",
                "frierenmagic:heal_focus",
                "frierenmagic:flight_focus",
                "frierenmagic:dig_focus"
        };

        for (int i = 0; i < focuses.length; i++) {
            Item focusItem = Registries.ITEM.get(new Identifier(focuses[i]));
            ChestBlockEntity targetChest = (i < 3) ? chest1 : chest2;
            targetChest.setStack(random.nextInt(27), new ItemStack(focusItem));
        }

        chest1.setStack(random.nextInt(27), new ItemStack(Items.DIAMOND, 3 + random.nextInt(3)));
        chest2.setStack(random.nextInt(27), new ItemStack(Items.EMERALD, 5 + random.nextInt(5)));
        chest1.setStack(random.nextInt(27), new ItemStack(Items.GOLDEN_APPLE, 2 + random.nextInt(2)));
    }
}