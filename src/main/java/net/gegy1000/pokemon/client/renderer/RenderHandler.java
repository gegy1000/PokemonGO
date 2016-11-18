package net.gegy1000.pokemon.client.renderer;

import POGOProtos.Enums.PokemonIdOuterClass;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.entity.CatchablePokemonEntity;
import net.gegy1000.pokemon.client.entity.GymEntity;
import net.gegy1000.pokemon.client.entity.PokemonEntity;
import net.gegy1000.pokemon.client.entity.PokestopEntity;
import net.gegy1000.pokemon.client.renderer.model.DefaultPokemonModel;
import net.gegy1000.pokemon.client.renderer.pokemon.PokemonEntityRenderer;
import net.gegy1000.pokemon.client.renderer.pokemon.PokemonRenderer;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
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
    public static final PokemonEntityRenderer POKEMON_ENTITY_RENDERER = new PokemonEntityRenderer();

    public static final PokemonRenderer POKEMON_RENDERER = new PokemonRenderer();

    public static final DefaultPokemonModel DEFAULT_POKEMON_MODEL = new DefaultPokemonModel();

    private static final Map<Class<? extends PokemonEntity>, PokemonObjectRenderer<?>> ENTITY_RENDERERS = new HashMap<>();

    private static final Map<PokemonIdOuterClass.PokemonId, ModelBase> POKEMON_MODELS = new HashMap<>();
    private static final Map<PokemonIdOuterClass.PokemonId, Callable<Boolean>> POKEMON_TEXTURES = new HashMap<>();

    public static void onPreInit() {
        ENTITY_RENDERERS.put(GymEntity.class, GYM_RENDERER);
        ENTITY_RENDERERS.put(PokestopEntity.class, POKESTOP_RENDERER);
        ENTITY_RENDERERS.put(CatchablePokemonEntity.class, POKEMON_ENTITY_RENDERER);

        //TODO Add custom models and textures here
        for (PokemonIdOuterClass.PokemonId pokemon : PokemonIdOuterClass.PokemonId.values()) {
            POKEMON_TEXTURES.putIfAbsent(pokemon, () -> {
                AdvancedDynamicTexture texture = PokemonGUIHandler.getTexture(pokemon);
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

    public static <T extends PokemonEntity> PokemonObjectRenderer<T> getRenderer(T entity) {
        return (PokemonObjectRenderer<T>) ENTITY_RENDERERS.get(entity.getClass());
    }
}
