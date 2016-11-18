package net.gegy1000.pokemon.client.util;

import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import com.pokegoapi.api.settings.MapSettings;
import com.pokegoapi.api.settings.Settings;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.entity.CatchablePokemonEntity;
import net.gegy1000.pokemon.client.entity.GymEntity;
import net.gegy1000.pokemon.client.entity.PokemonEntity;
import net.gegy1000.pokemon.client.entity.PokestopEntity;
import net.gegy1000.pokemon.client.renderer.pokemon.CatchableRenderedPokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PokemonMapHandler {
    public static final List<NearbyPokemon> NEARBY_POKEMONS = new LinkedList<>();
    public static final List<CatchablePokemon> CATCHABLE_POKEMON = new LinkedList<>();
    public static final List<Gym> GYMS = new LinkedList<>();
    public static final List<Pokestop> POKESTOPS = new LinkedList<>();

    public static final List<PokemonEntity> ENTITIES = new LinkedList<>();

    public static final Object MAP_LOCK = new Object();

    public static long lastMapUpdate;

    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    public static List<NearbyPokemon> getNearbyPokemon() {
        synchronized (MAP_LOCK) {
            return NEARBY_POKEMONS;
        }
    }

    public static List<CatchablePokemon> getCatchablePokemon() {
        synchronized (MAP_LOCK) {
            return CATCHABLE_POKEMON;
        }
    }

    public static List<Gym> getGyms() {
        synchronized (MAP_LOCK) {
            return GYMS;
        }
    }

    public static List<Pokestop> getPokestops() {
        synchronized (MAP_LOCK) {
            return POKESTOPS;
        }
    }

    public static List<PokemonEntity> getEntities() {
        synchronized (MAP_LOCK) {
            return ENTITIES;
        }
    }

    public static void removePokemon(CatchablePokemon pokemon) {
        synchronized (MAP_LOCK) {
            CATCHABLE_POKEMON.remove(pokemon);
        }
        PokemonMapHandler.updateEntities();
    }

    public static void updateEntities() {
        synchronized (MAP_LOCK) {
            ENTITIES.clear();
            WorldClient world = MINECRAFT.theWorld;
            for (CatchablePokemon catchablePokemon : CATCHABLE_POKEMON) {
                ENTITIES.add(new CatchablePokemonEntity(world, catchablePokemon, new CatchableRenderedPokemon(world, catchablePokemon, true, true)));
            }
            for (Pokestop pokestop : POKESTOPS) {
                ENTITIES.add(new PokestopEntity(world, pokestop));
            }
            for (Gym gym : GYMS) {
                ENTITIES.add(new GymEntity(world, gym));
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
                    synchronized (MAP_LOCK) {
                        NEARBY_POKEMONS.clear();
                        NEARBY_POKEMONS.addAll(nearbyPokemon);
                    }
                    List<CatchablePokemon> catchablePokemon = map.getCatchablePokemon();
                    synchronized (MAP_LOCK) {
                        CATCHABLE_POKEMON.clear();
                        CATCHABLE_POKEMON.addAll(catchablePokemon);
                    }
                    List<Gym> gyms = map.getGyms();
                    for (Gym gym : gyms) {
                        gym.getDefendingPokemon();
                    }
                    synchronized (MAP_LOCK) {
                        GYMS.clear();
                        GYMS.addAll(gyms);
                    }
                    Collection<Pokestop> pokestops = map.getMapObjects().getPokestops();
                    synchronized (MAP_LOCK) {
                        POKESTOPS.clear();
                        POKESTOPS.addAll(pokestops);
                    }
                    PokemonMapHandler.updateEntities();
                    PokemonGO.LOGGER.info("Detected " + pokestops.size() + " Pokestops, " + gyms.size() + " Gyms, " + catchablePokemon.size() + " Pokemon, and " + nearbyPokemon.size() + " nearby!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clear() {
        synchronized (MAP_LOCK) {
            CATCHABLE_POKEMON.clear();
            GYMS.clear();
            POKESTOPS.clear();
            ENTITIES.clear();
        }
    }

    public static void update() {
        synchronized (MAP_LOCK) {
            for (PokemonEntity entity : ENTITIES) {
                entity.update();
            }
        }
    }
}
