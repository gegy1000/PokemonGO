package net.gegy1000.pokemon.server.world.gen;

import net.gegy1000.earth.server.world.gen.EarthGenerator;

public class PokemonEarthGenerator extends EarthGenerator {
    private static final double WORLD_SCALE = 40.0;

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
        double height = this.extractHeight(x, y);
        if (height < 62) {
            height /= 10.4;
        } else {
            height -= 55;
        }
        return Math.max(1, height * 3.1);
    }
}