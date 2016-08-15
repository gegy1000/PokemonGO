package net.gegy1000.pokemon.client.key;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class PokemonKeyBinds {
    public static final KeyBinding KEY_LOGIN = new KeyBinding("Login", Keyboard.KEY_L, "Pokémon GO");
    public static final KeyBinding KEY_POKEMON_VIEW = new KeyBinding("Pokémon View", Keyboard.KEY_I, "Pokémon GO");

    public static void init() {
        ClientRegistry.registerKeyBinding(KEY_LOGIN);
        ClientRegistry.registerKeyBinding(KEY_POKEMON_VIEW);
    }
}
