package net.gegy1000.pokemon.client.entity;

import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.CapturePokemonGUI;
import net.gegy1000.pokemon.client.renderer.pokemon.RenderedPokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class CatchablePokemonEntity extends PokemonEntity {
    private CatchablePokemon pokemon;
    private RenderedPokemon renderedPokemon;

    public CatchablePokemonEntity(World world, CatchablePokemon pokemon, RenderedPokemon renderedPokemon) {
        super(world, PokemonGO.GENERATOR.fromLong(pokemon.getLongitude()), PokemonGO.GENERATOR.fromLat(pokemon.getLatitude()));
        this.pokemon = pokemon;
        this.renderedPokemon = renderedPokemon;
        this.setSize(1.5F, 2.0F);
    }

    @Override
    public void onInteract() {
        Minecraft.getMinecraft().displayGuiScreen(new CapturePokemonGUI(this.pokemon));
    }

    public RenderedPokemon getPokemon() {
        return this.renderedPokemon;
    }
}
