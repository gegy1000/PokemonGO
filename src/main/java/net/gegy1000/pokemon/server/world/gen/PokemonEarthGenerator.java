package net.gegy1000.pokemon.server.world.gen;

import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class PokemonEarthGenerator extends EarthGenerator {
    private static final double WORLD_SCALE = 232;

    @Override
    protected double getWorldScale() {
        return WORLD_SCALE;
    }

    @Override
    public double extractHeight(int x, int y) {
        if (x < 0 || x >= this.heightmap.getWidth() || y < 0 || y >= this.heightmap.getHeight()) {
            return 0;
        }
        return this.heightmap.getData(x, y);
    }

    @Override
    public double getHeight(int x, int y) {
        return 23 + new Random(y * 43200L + x).nextInt(5);
    }

    @Override
    public Biome getBiomeForCoords(int x, int z) {
        return Biomes.PLAINS;
    }
}