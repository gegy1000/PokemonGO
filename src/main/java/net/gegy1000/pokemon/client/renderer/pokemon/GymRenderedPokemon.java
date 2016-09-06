package net.gegy1000.pokemon.client.renderer.pokemon;

import POGOProtos.Data.Battle.BattlePokemonInfoOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GymRenderedPokemon extends RenderedPokemon {
    private PokemonAnimation animation = PokemonAnimation.NONE;
    private BattlePokemonInfoOuterClass.BattlePokemonInfo pokemon;
    private float renderYaw;

    public GymRenderedPokemon(BattlePokemonInfoOuterClass.BattlePokemonInfo pokemon, float renderYaw) {
        super(false, false);
        this.pokemon = pokemon;
        this.renderYaw = renderYaw;
    }

    @Override
    public PokemonAnimation getAnimation() {
        return this.animation;
    }

    @Override
    public String getNickname() {
        return this.pokemon.getPokemonData().getNickname();
    }

    @Override
    public PokemonIdOuterClass.PokemonId getPokemonID() {
        return this.pokemon.getPokemonData().getPokemonId();
    }

    @Override
    public int getLight() {
        return 0xF000F0;
    }

    @Override
    public float getRenderYaw() {
        return this.renderYaw;
    }
}
