package net.gegy1000.pokemon.client.util;

import com.pokegoapi.api.PokemonGo;

public class PokemonRefreshHandler {
    public static void startRefresh() {
        PokemonMapHandler.refresh();
        final PokemonGo api = PokemonHandler.API;
        Thread refreshThread = new Thread(() -> {
            while (PokemonHandler.API == api) {
                try {
                    Thread.sleep(100);
                    PokemonMapHandler.refresh();
                    PokemonHandler.refresh();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            PokemonMapHandler.clear();
        });
        refreshThread.setName("Pokemon Refresh Thread");
        refreshThread.start();
    }
}
