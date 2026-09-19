package com.frieren.magic;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public class FrierenHouseGenerator {

    public static void generateHouse(ServerWorld world, FrierenWorldState state) {
        BlockPos spawnPos = world.getSpawnPos();
        Random random = world.getRandom();

        // Выбираем случайный угол (в радианах) и сохраняем его в стейт мира
        double angle = random.nextDouble() * 2 * Math.PI;
        state.generationAngle = angle;
        state.markDirty();

        // Церковь будет на расстоянии 60-90 блоков от спавна в этом направлении
        int distance = 60 + random.nextInt(30);
        int offsetX = (int) (Math.cos(angle) * distance);
        int offsetZ = (int) (Math.sin(angle) * distance);

        BlockPos surfacePos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, spawnPos.add(offsetX, 0, offsetZ));
        buildAbandonedChurch(world, surfacePos, random);
    }

    private static void buildAbandonedChurch(ServerWorld world, BlockPos startPos, Random random) {
        int width = 13;
        int length = 25;
        int height = 15;

        BlockState[] stoneTypes = {
                Blocks.STONE_BRICKS.getDefaultState(),
                Blocks.CRACKED_STONE_BRICKS.getDefaultState(),
                Blocks.MOSSY_STONE_BRICKS.getDefaultState(),
                Blocks.COBBLESTONE.getDefaultState(),
                Blocks.MOSSY_COBBLESTONE.getDefaultState()
        };

        // ФУНДАМЕНТ
        for (int x = -width / 2 - 1; x <= width / 2 + 1; x++) {
            for (int z = -2; z <= length + 2; z++) {
                for (int y = -6; y < 0; y++) {
                    BlockPos p = startPos.add(x, y, z);
                    if (world.getBlockState(p).isAir() || world.getBlockState(p).isOf(Blocks.WATER) || world.getBlockState(p).isOf(Blocks.DIRT)) {
                        world.setBlockState(p, Blocks.COBBLESTONE.getDefaultState(), 2);
                    }
                }
            }
        }

        // КАРКАС И СТЕНЫ
        for (int x = -width / 2; x <= width / 2; x++) {
            for (int z = -2; z <= length + 2; z++) {
                for (int y = 0; y < height; y++) {
                    BlockPos pos = startPos.add(x, y, z);

                    if (x > -width/2 && x < width/2 && z >= 0 && z <= length && y < 10) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                    }

                    if (y == 0 && x >= -width / 2 && x <= width / 2 && z >= 0 && z <= length) {
                        if (random.nextFloat() > 0.15f) {
                            world.setBlockState(pos, stoneTypes[random.nextInt(stoneTypes.length)], 2);
                        } else {
                            world.setBlockState(pos, Blocks.COARSE_DIRT.getDefaultState(), 2);
                        }
                        continue;
                    }

                    boolean isWallX = (x == -width / 2 || x == width / 2);
                    boolean isWallZ = (z == 0 || z == length);
                    boolean isWall = (isWallX && z >= 0 && z <= length) || (isWallZ && x >= -width / 2 && x <= width / 2);

                    if (isWall) {
                        float decayChance = (float) y / height + random.nextFloat() * 0.3f;

                        if (z == 0 && Math.abs(x) <= 1 && y < 4) continue;
                        if (isWallX && y >= 3 && y <= 8 && z % 4 == 0) continue;
                        if (z == length && Math.abs(x) <= 2 && y >= 4 && y <= 10) continue;

                        if (decayChance < 0.7f) {
                            world.setBlockState(pos, stoneTypes[random.nextInt(stoneTypes.length)], 2);
                            if (random.nextFloat() < 0.04f) {
                                world.setBlockState(pos.add(x > 0 ? -1 : 1, 0, 0), Blocks.OAK_LEAVES.getDefaultState(), 2);
                            }
                        }
                    }

                    if (y > 0 && y < 10 && (x == -3 || x == 3) && z > 2 && z < length - 3 && z % 4 == 0) {
                        world.setBlockState(pos, Blocks.CHISELED_STONE_BRICKS.getDefaultState(), 2);
                    }

                    if (y >= 10 && z >= 0 && z <= length) {
                        int distFromCenter = Math.abs(x);
                        int expectedHeight = 10 + ((width / 2) - distFromCenter);

                        if (y == expectedHeight) {
                            float roofChance = random.nextFloat();
                            if (roofChance < 0.6f) {
                                world.setBlockState(pos, Blocks.DARK_OAK_PLANKS.getDefaultState(), 2);
                            } else if (roofChance < 0.85f) {
                                world.setBlockState(pos, Blocks.DARK_OAK_SLAB.getDefaultState(), 2);
                            }
                        }
                    }

                    if (y == 1 && (x >= -4 && x <= -1 || x >= 1 && x <= 4) && z > 3 && z < length - 6 && z % 3 == 0) {
                        if (random.nextFloat() > 0.4f) {
                            world.setBlockState(pos, Blocks.OAK_STAIRS.getDefaultState(), 2);
                        }
                    }
                }
            }
        }

        // КРЕСТ НА КРЫШЕ
        BlockPos roofPeak = startPos.add(0, 10 + (width / 2), 4);
        for (int cy = 0; cy < 4; cy++) {
            world.setBlockState(roofPeak.up(cy), Blocks.ANDESITE_WALL.getDefaultState(), 2);
        }
        world.setBlockState(roofPeak.up(2).west(), Blocks.ANDESITE_WALL.getDefaultState(), 2);
        world.setBlockState(roofPeak.up(2).east(), Blocks.ANDESITE_WALL.getDefaultState(), 2);
        world.setBlockState(roofPeak.up(3), Blocks.POLISHED_ANDESITE.getDefaultState(), 2);

        // АЛТАРЬ
        for (int ax = -3; ax <= 3; ax++) {
            for (int az = length - 5; az <= length - 1; az++) {
                world.setBlockState(startPos.add(ax, 0, az), Blocks.POLISHED_ANDESITE.getDefaultState(), 2);
                if (az == length - 5) {
                    world.setBlockState(startPos.add(ax, 1, az), Blocks.STONE_BRICK_STAIRS.getDefaultState(), 2);
                }
            }
        }

        // СУНДУК
        BlockPos chestPos = startPos.add(0, 1, length - 2);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);

        for (int i = 0; i < 12; i++) {
            BlockPos webPos = chestPos.add(random.nextInt(5) - 2, random.nextInt(3), random.nextInt(5) - 2);
            if (world.getBlockState(webPos).isAir()) {
                world.setBlockState(webPos, Blocks.COBWEB.getDefaultState(), 2);
            }
        }

        BlockEntity blockEntity = world.getBlockEntity(chestPos);
        if (blockEntity instanceof ChestBlockEntity chest) {
            populateChest(chest, random);
        }

        System.out.println("[FrierenMagic] Заброшенная церковь создана на: " + chestPos.toShortString());
    }

    private static void populateChest(ChestBlockEntity chest, Random random) {
        ItemStack staff = new ItemStack(FrierenMod.FRIEREN_STAFF);
        String[] focuses = {
                "frierenmagic:zoltraak_focus",
                "frierenmagic:shield_focus",
                "frierenmagic:heal_focus",
                "frierenmagic:flight_focus",
                "frierenmagic:dig_focus"
        };

        String randomFocus = focuses[random.nextInt(focuses.length)];
        StaffItem.setAttachedFocus(staff, randomFocus);
        chest.setStack(13, staff);

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        NbtCompound nbt = book.getOrCreateNbt();
        nbt.putString("title", "Завещание Фрирен");
        nbt.putString("author", "Фрирен");

        NbtList pages = new NbtList();
        String text = "{\"text\":\"Здравствуй.\\n\\nЕсли ты читаешь это, значит, меня уже нет, а этот мир нуждается в защите.\\n\\nЯ оставляю тебе свой посох. Возьми его, освой магию и одолей великого дракона Края.\\n\\nВремя скоротечно.\\nНе трать его впустую.\\n\\n— Фрирен\"}";
        pages.add(NbtString.of(text));
        nbt.put("pages", pages);

        chest.setStack(12, book);
    }
}