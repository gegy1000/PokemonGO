package net.gegy1000.pokemon.client.renderer.pokemon;

import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import net.gegy1000.pokemon.PokemonGO;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CatchableRenderedPokemon extends RenderedPokemon {
    private World world;
    private CatchablePokemon pokemon;

    public CatchableRenderedPokemon(World world, CatchablePokemon pokemon, boolean shouldRenderName, boolean shouldFacePlayer) {
        super(shouldRenderName, shouldFacePlayer);
        this.world = world;
        this.pokemon = pokemon;
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
        return this.pokemon.getPokemonId();
    }

    @Override
    public int getLight() {
        double x = PokemonGO.GENERATOR.fromLong(this.pokemon.getLongitude());
        double z = PokemonGO.GENERATOR.fromLat(this.pokemon.getLatitude());
        return this.world.getCombinedLight(this.world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)), 0);
    }

    public CatchablePokemon getPokemon() {
        return this.pokemon;
    }
}
