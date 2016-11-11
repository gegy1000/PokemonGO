package net.gegy1000.pokemon.client.util;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.EggPokemon;
import com.pokegoapi.api.pokemon.HatchedEgg;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.settings.Settings;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.google.common.geometry.S2LatLng;
import net.gegy1000.pokemon.PokemonGO;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.server.snackbar.Snackbar;
import net.ilexiconn.llibrary.server.snackbar.SnackbarHandler;
import net.ilexiconn.llibrary.server.snackbar.SnackbarPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import okhttp3.OkHttpClient;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;

public class PokemonHandler {
    private static final Queue<FutureTask<?>> TASKS = new LinkedBlockingDeque<>();

    public static final Set<EggIncubator> REQUESTED_HATCHES = new HashSet<>();
    public static long lastProfileUpdate;

    public static PokemonGo API;

    public static boolean authenticating;
    public static boolean loginFailed;
    public static String username;

    public static int level;
    public static long experience;

    public static void onPreInit() {
        PokemonGUIHandler.onPreInit();
        PokemonUtils.onPreInit();
        Thread taskThread = new Thread(() -> {
            while (true) {
                try {
                    if (TASKS.size() > 0) {
                        FutureTask<?> task = TASKS.poll();
                        task.run();
                        task.get();
                    }
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        taskThread.setName("Pokemon Task Runner");
        taskThread.setDaemon(true);
        taskThread.start();
    }

    public static void addTask(Callable<?> task) {
        TASKS.add(new FutureTask<>(task));
    }

    public static void authenticate(String username, String password) {
        PokemonHandler.authenticating = true;
        PokemonHandler.username = username;
        PokemonHandler.loginFailed = false;
        PokemonHandler.addTask(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                API = new PokemonGo(client);
                API.login(new PtcCredentialProvider(client, username, password));
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                API.setLocation(PokemonGO.GENERATOR.toLat(player.posZ), PokemonGO.GENERATOR.toLong(player.posX), 0);
                API.getInventories().updateInventories();
                List<CatchablePokemon> catchablePokemon = API.getMap().getCatchablePokemon();
                PokemonGO.LOGGER.info(catchablePokemon.size() + " Pokemon!");
                for (CatchablePokemon cp : catchablePokemon) {
                    PokemonGO.LOGGER.info(cp.getPokemonId() + " at " + cp.getLatitude() + " " + cp.getLongitude());
                }
                PokemonRefreshHandler.startRefresh();
                PokemonHandler.username = API.getPlayerProfile().getPlayerData().getUsername();
                PokemonHandler.level = API.getPlayerProfile().getStats().getLevel();
                PokemonHandler.experience = API.getPlayerProfile().getStats().getExperience();
                PokemonHandler.authenticating = false;
            } catch (Exception e) {
                API = null;
                System.err.println("Failed to authenticate.");
                e.printStackTrace();
                PokemonHandler.authenticating = false;
                PokemonHandler.loginFailed = true;
            }
            authenticating = false;
            return null;
        });
    }

    public static void update(EntityPlayer player) {
        PokemonGUIHandler.update();
        if (player != null) {
            if (API != null && !authenticating) {
                API.setLatitude(PokemonGO.GENERATOR.toLat(player.posZ));
                API.setLongitude(PokemonGO.GENERATOR.toLong(player.posX));
                if (Minecraft.getMinecraft().currentScreen == null) {
                    try {
                        PlayerProfile playerProfile = API.getPlayerProfile();
                        if (playerProfile != null) {
                            int level = playerProfile.getStats().getLevel();
                            if (PokemonHandler.level != level) {
                                if (level > PokemonHandler.level) {
                                    Snackbar snackbar = Snackbar.create((LLibrary.CONFIG.getColorMode().equals("dark") ? TextFormatting.BLACK : TextFormatting.WHITE) + I18n.translateToLocal("snackbar.level_up.name"));
                                    snackbar.setPosition(SnackbarPosition.UP);
                                    snackbar.setColor(LLibrary.CONFIG.getAccentColor());
                                    SnackbarHandler.INSTANCE.showSnackbar(snackbar);
                                    PokemonHandler.addTask(() -> {
                                        try {
                                            API.getPlayerProfile().acceptLevelUpRewards(level);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    });
                                }
                                PokemonHandler.level = level;
                            }
                            long experience = playerProfile.getStats().getExperience();
                            if (PokemonHandler.experience != experience) {
                                if (experience > PokemonHandler.experience) {
                                    Snackbar snackbar = Snackbar.create((LLibrary.CONFIG.getColorMode().equals("dark") ? TextFormatting.BLACK : TextFormatting.WHITE) + I18n.translateToLocalFormatted("snackbar.experience.name", String.valueOf(experience - PokemonHandler.experience)));
                                    snackbar.setPosition(SnackbarPosition.UP);
                                    snackbar.setColor(LLibrary.CONFIG.getAccentColor());
                                    SnackbarHandler.INSTANCE.showSnackbar(snackbar);
                                }
                                PokemonHandler.experience = experience;
                            }
                            List<EggIncubator> incubators = PokemonHandler.API.getInventories().getIncubators();
                            Set<EggPokemon> eggs = PokemonHandler.API.getInventories().getHatchery().getEggs();
                            boolean queryEggs = false;
                            for (EggIncubator incubator : incubators) {
                                double left = incubator.getKmLeftToWalk();
                                boolean requested = REQUESTED_HATCHES.contains(incubator);
                                if (!requested && left <= 0.0) {
                                    for (EggPokemon egg : eggs) {
                                        if (egg.isIncubate() && egg.getEggIncubatorId().equals(incubator.getId())) {
                                            System.out.println("Request Hatch");
                                            REQUESTED_HATCHES.add(incubator);
                                            PokemonHandler.addTask(() -> {
                                                incubator.hatchEgg(egg);
                                                return null;
                                            });
                                            queryEggs = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (queryEggs) {
                                PokemonHandler.addTask(() -> {
                                    List<HatchedEgg> hatchedEggs = PokemonHandler.API.getInventories().getHatchery().queryHatchedEggs();
                                    System.out.println(hatchedEggs.size() + " eggs are ready to hatch!");
                                    for (HatchedEgg hatchedEgg : hatchedEggs) {
                                        PokeBank pokebank = API.getInventories().getPokebank();
                                        Pokemon pokemon = pokebank.getPokemonById(hatchedEgg.getId());
                                        PokemonGO.LOGGER.info(pokemon.getPokemonId().name());
                                        PokemonGO.LOGGER.info(hatchedEgg.getCandy());
                                        PokemonGO.LOGGER.info(hatchedEgg.getExperience());
                                        PokemonGO.LOGGER.info(hatchedEgg.getStardust());
                                        //TODO Open GUI
                                    }
                                    REQUESTED_HATCHES.clear();
                                    return null;
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void refresh() {
        long time = PokemonHandler.API.currentTimeMillis();
        Settings settings = PokemonHandler.API.getSettings();
        if (settings != null && settings.getMapSettings() != null) {
            try {
                if (time - PokemonHandler.lastProfileUpdate > 10000) {
                    PokemonHandler.API.getPlayerProfile().updateProfile();
                    PokemonHandler.API.getInventories().updateInventories();
                    PokemonHandler.lastProfileUpdate = time;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        S2LatLng first = S2LatLng.fromDegrees(lat1, lon1);
        S2LatLng second = S2LatLng.fromDegrees(lat2, lon2);
        return first.getEarthDistance(second);
    }

    public static boolean isAuthenticating() {
        return PokemonHandler.authenticating;
    }

    public static boolean isLoginFailed() {
        return PokemonHandler.loginFailed;
    }

    public static String getUsername() {
        return PokemonHandler.username;
    }
}
