package net.gegy1000.pokemon.client.util;

import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import com.pokegoapi.api.settings.MapSettings;
import com.pokegoapi.api.settings.Settings;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.renderer.pokemon.CatchableRenderedPokemon;
import net.minecraft.client.Minecraft;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PokemonMapHandler {
    public static final List<NearbyPokemon> NEARBY_POKEMONS = new LinkedList<>();
    public static final List<CatchablePokemon> CATCHABLE_POKEMON = new LinkedList<>();
    public static final List<CatchableRenderedPokemon> CATCHABLE_RENDERED_POKEMON = new LinkedList<>();
    public static final List<Gym> GYMS = new LinkedList<>();
    public static final List<Pokestop> POKESTOPS = new LinkedList<>();

    public static long lastMapUpdate;

    public static List<NearbyPokemon> getNearbyPokemon() {
        synchronized (NEARBY_POKEMONS) {
            return NEARBY_POKEMONS;
        }
    }

    public static List<CatchablePokemon> getCatchablePokemon() {
        synchronized (CATCHABLE_POKEMON) {
            return CATCHABLE_POKEMON;
        }
    }

    public static List<CatchableRenderedPokemon> getCatchableRenderedPokemon() {
        synchronized (CATCHABLE_RENDERED_POKEMON) {
            return CATCHABLE_RENDERED_POKEMON;
        }
    }

    public static List<Gym> getGyms() {
        synchronized (GYMS) {
            return GYMS;
        }
    }

    public static List<Pokestop> getPokestops() {
        synchronized (POKESTOPS) {
            return POKESTOPS;
        }
    }

    public static void removePokemon(CatchablePokemon pokemon) {
        synchronized (CATCHABLE_POKEMON) {
            CATCHABLE_POKEMON.remove(pokemon);
        }
        PokemonMapHandler.updateRenderedPokemon();
    }

    public static void updateRenderedPokemon() {
        synchronized (CATCHABLE_RENDERED_POKEMON) {
            CATCHABLE_RENDERED_POKEMON.clear();
            for (CatchablePokemon pokemon : CATCHABLE_POKEMON) {
                CATCHABLE_RENDERED_POKEMON.add(new CatchableRenderedPokemon(Minecraft.getMinecraft().theWorld, pokemon, true, true));
            }
        }
    }

    public static void refresh() {
        long time = PokemonHandler.API.currentTimeMillis();
        Settings settings = PokemonHandler.API.getSettings();
        if (settings != null && settings.getMapSettings() != null) {
            MapSettings mapSettings = settings.getMapSettings();
            try {
                if (mapSettings.getMinRefresh() > 0 && time - lastMapUpdate > mapSettings.getMinRefresh()) {
                    lastMapUpdate = time;
                    Map map = PokemonHandler.API.getMap();
                    List<NearbyPokemon> nearbyPokemon = map.getNearbyPokemon();
                    synchronized (NEARBY_POKEMONS) {
                        NEARBY_POKEMONS.clear();
                        NEARBY_POKEMONS.addAll(nearbyPokemon);
                    }
                    List<CatchablePokemon> catchablePokemon = map.getCatchablePokemon();
                    synchronized (CATCHABLE_POKEMON) {
                        CATCHABLE_POKEMON.clear();
                        CATCHABLE_POKEMON.addAll(catchablePokemon);
                    }
                    PokemonMapHandler.updateRenderedPokemon();
                    List<Gym> gyms = map.getGyms();
                    for (Gym gym : gyms) {
                        gym.getGymMembers();
                    }
                    synchronized (GYMS) {
                        GYMS.clear();
                        GYMS.addAll(gyms);
                    }
                    Collection<Pokestop> pokestops = map.getMapObjects().getPokestops();
                    synchronized (POKESTOPS) {
                        POKESTOPS.clear();
                        POKESTOPS.addAll(pokestops);
                    }
                    PokemonGO.LOGGER.info("Detected " + pokestops.size() + " Pokestops, " + gyms.size() + " Gyms, " + catchablePokemon.size() + " Pokemon, and " + nearbyPokemon.size() + " nearby!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clear() {
        synchronized (CATCHABLE_POKEMON) {
            CATCHABLE_POKEMON.clear();
        }
        synchronized (CATCHABLE_RENDERED_POKEMON) {
            CATCHABLE_RENDERED_POKEMON.clear();
        }
        synchronized (GYMS) {
            GYMS.clear();
        }
        synchronized (POKESTOPS) {
            POKESTOPS.clear();
        }
    }
}
