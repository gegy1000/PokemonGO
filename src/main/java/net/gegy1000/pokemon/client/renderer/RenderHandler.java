package net.gegy1000.pokemon.client.renderer;

import POGOProtos.Enums.PokemonIdOuterClass;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.renderer.model.DefaultPokemonModel;
import net.gegy1000.pokemon.client.renderer.pokemon.PokemonRenderer;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.minecraft.client.model.ModelBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@SideOnly(Side.CLIENT)
public class RenderHandler {
    public static final GymRenderer GYM_RENDERER = new GymRenderer();
    public static final PokestopRenderer POKESTOP_RENDERER = new PokestopRenderer();
    public static final PokemonRenderer POKEMON_RENDERER = new PokemonRenderer();

    public static final DefaultPokemonModel DEFAULT_POKEMON_MODEL = new DefaultPokemonModel();

    private static final Map<PokemonIdOuterClass.PokemonId, ModelBase> POKEMON_MODELS = new HashMap<>();
    private static final Map<PokemonIdOuterClass.PokemonId, Callable<Boolean>> POKEMON_TEXTURES = new HashMap<>();

    public static void onPreInit() {
        //TODO Add custom models and textures here
        for (PokemonIdOuterClass.PokemonId pokemon : PokemonIdOuterClass.PokemonId.values()) {
            POKEMON_TEXTURES.putIfAbsent(pokemon, () -> {
                AdvancedDynamicTexture texture = PokemonHandler.getTexture(pokemon);
                if (texture != null) {
                    texture.bind();
                    return true;
                }
                return false;
            });
        }
        for (PokemonIdOuterClass.PokemonId pokemon : PokemonIdOuterClass.PokemonId.values()) {
            POKEMON_MODELS.putIfAbsent(pokemon, DEFAULT_POKEMON_MODEL);
        }
    }

    public static ModelBase getPokemonModel(PokemonIdOuterClass.PokemonId pokemon) {
        return POKEMON_MODELS.get(pokemon);
    }

    public static boolean bindPokemonTexture(PokemonIdOuterClass.PokemonId pokemon) {
        try {
            return POKEMON_TEXTURES.get(pokemon).call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
