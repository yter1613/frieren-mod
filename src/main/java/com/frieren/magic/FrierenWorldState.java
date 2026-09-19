package com.frieren.magic;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class FrierenWorldState extends PersistentState {
    public boolean houseGenerated = false;
    public boolean treeGenerated = false;
    public double generationAngle = -1.0; // Сохраняем общий угол направления

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean("HouseGenerated", houseGenerated);
        nbt.putBoolean("TreeGenerated", treeGenerated);
        nbt.putDouble("GenerationAngle", generationAngle);
        return nbt;
    }

    public static FrierenWorldState readNbt(NbtCompound nbt) {
        FrierenWorldState state = new FrierenWorldState();
        state.houseGenerated = nbt.getBoolean("HouseGenerated");
        state.treeGenerated = nbt.getBoolean("TreeGenerated");
        state.generationAngle = nbt.getDouble("GenerationAngle");
        return state;
    }

    public static FrierenWorldState getServerState(ServerWorld world) {
        PersistentState.Type<FrierenWorldState> type = new PersistentState.Type<>(
                FrierenWorldState::new,
                FrierenWorldState::readNbt,
                null
        );
        return world.getPersistentStateManager().getOrCreate(type, "frieren_world_state");
    }
}