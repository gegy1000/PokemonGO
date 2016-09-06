package net.gegy1000.pokemon.client.renderer;

import net.minecraft.client.Minecraft;

public abstract class PokemonObjectRenderer<T extends Object> {
    protected static final Minecraft MC = Minecraft.getMinecraft();

    public abstract void render(T object, double x, double y, double z, float partialTicks);
}
