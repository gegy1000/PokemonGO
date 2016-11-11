package net.gegy1000.pokemon.client.renderer.pokemon;

import POGOProtos.Enums.PokemonIdOuterClass;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class RenderedPokemon {
    private final boolean shouldRenderName;
    private final boolean shouldFacePlayer;

    public RenderedPokemon(boolean shouldRenderName, boolean shouldFacePlayer) {
        this.shouldRenderName = shouldRenderName;
        this.shouldFacePlayer = shouldFacePlayer;
    }

    public abstract PokemonAnimation getAnimation();

    public abstract String getNickname();

    public abstract PokemonIdOuterClass.PokemonId getPokemonID();

    public boolean shouldRenderName() {
        return this.shouldRenderName;
    }

    public boolean shouldFacePlayer() {
        return this.shouldFacePlayer;
    }

    public float getRenderYaw() {
        return 0.0F;
    }

    public abstract int getLight();
}
