package net.gegy1000.pokemon.client.renderer.pokemon;

import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import net.minecraft.world.World;

public class DataRenderedPokemon extends RenderedPokemon {
    private World world;
    private PokemonDataOuterClass.PokemonData data;
    private int light;

    public DataRenderedPokemon(World world, PokemonDataOuterClass.PokemonData data, int light, boolean shouldRenderName, boolean shouldFacePlayer) {
        super(shouldRenderName, shouldFacePlayer);
        this.world = world;
        this.data = data;
        this.light = light;
    }

    @Override
    public PokemonAnimation getAnimation() {
        return PokemonAnimation.NONE;
    }

    @Override
    public String getNickname() {
        return "";
    }

    @Override
    public PokemonIdOuterClass.PokemonId getPokemonID() {
        return this.data.getPokemonId();
    }

    @Override
    public int getLight() {
        return this.light;
    }
}
