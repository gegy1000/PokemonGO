package net.gegy1000.pokemon.server.world.gen;

import net.gegy1000.earth.server.world.gen.ChunkGeneratorEarth;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.block.BlockFalling;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class ChunkGeneratorPokemon extends ChunkGeneratorEarth {
    public ChunkGeneratorPokemon(World world, long seed, EarthGenerator earthGenerator) {
        super(world, seed, earthGenerator);
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        BlockFalling.fallInstantly = true;
        int x = chunkX * 16;
        int z = chunkZ * 16;
        BlockPos pos = new BlockPos(x, 0, z);
        Biome biome = this.world.getBiome(pos.add(16, 0, 16));
        this.random.setSeed(this.world.getSeed());
        long i1 = this.random.nextLong() / 2L * 2L + 1L;
        long j1 = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long) chunkX * i1 + (long) chunkZ * j1 ^ this.world.getSeed());
        boolean hasVillageGenerated = false;
        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(this, this.world, this.random, chunkX, chunkZ, hasVillageGenerated));
        if (TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, hasVillageGenerated, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
            WorldEntitySpawner.performWorldGenSpawning(this.world, biome, x + 8, z + 8, 16, 16, this.random);
        }
        pos = pos.add(8, 0, 8);
        boolean freeze = TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, hasVillageGenerated, PopulateChunkEvent.Populate.EventType.ICE);
        for (int xOffset = 0; freeze && xOffset < 16; ++xOffset) {
            for (int zOffset = 0; zOffset < 16; ++zOffset) {
                BlockPos top = this.world.getPrecipitationHeight(pos.add(xOffset, 0, zOffset));
                BlockPos ground = top.down();
                if (this.world.canBlockFreezeWater(ground)) {
                    this.world.setBlockState(ground, Blocks.ICE.getDefaultState(), 2);
                }
                if (this.world.canSnowAt(top, true)) {
                    this.world.setBlockState(top, Blocks.SNOW_LAYER.getDefaultState(), 2);
                }
            }
        }
        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(this, this.world, this.random, chunkX, chunkZ, hasVillageGenerated));
        BlockFalling.fallInstantly = false;
    }
}
