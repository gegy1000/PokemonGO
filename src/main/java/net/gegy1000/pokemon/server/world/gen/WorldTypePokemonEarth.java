package net.gegy1000.pokemon.server.world.gen;

import net.gegy1000.earth.server.world.gen.BiomeProviderEarth;
import net.gegy1000.earth.server.world.gen.ChunkGeneratorEarth;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;

public class WorldTypePokemonEarth extends WorldTypeEarth {
    private final PokemonEarthGenerator generator;

    public WorldTypePokemonEarth(PokemonEarthGenerator generator) {
        super("earth_pokemongo", generator);
        this.generator = generator;
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        return new ChunkGeneratorEarth(world, world.getSeed(), this.generator);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        return new BiomeProviderEarth(this.generator);
    }

    @Override
    public int getMapZoomX() {
        return 65;
    }

    @Override
    public int getMapZoomY() {
        return 1;
    }

    @Override
    public int getMapZoom() {
        return 20;
    }

    @Override
    public int getMapDownloadScale() {
        return 16;
    }
}
