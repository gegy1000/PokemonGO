package net.gegy1000.pokemon.client.entity;

import com.pokegoapi.api.map.fort.Pokestop;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.PokestopGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class PokestopEntity extends PokemonEntity {
    private Pokestop pokestop;

    public PokestopEntity(World world, Pokestop pokestop) {
        super(world, PokemonGO.GENERATOR.fromLong(pokestop.getLongitude()), PokemonGO.GENERATOR.fromLat(pokestop.getLatitude()));
        this.pokestop = pokestop;
        this.setSize(1.5F, 6.0F);
    }

    public Pokestop getPokestop() {
        return this.pokestop;
    }

    @Override
    public void onInteract() {
        Minecraft.getMinecraft().displayGuiScreen(new PokestopGUI(this.pokestop));
    }
}
