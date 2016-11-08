package net.gegy1000.pokemon.client.util;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.HatchedEgg;
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

import java.util.LinkedList;
import java.util.List;

public class PokemonHandler {
    public static final OkHttpClient CLIENT = new OkHttpClient();

    public static final List<HatchedEgg> HATCHED_EGGS = new LinkedList<>();
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
    }

    public static void authenticate(String username, String password) {
        PokemonHandler.authenticating = true;
        PokemonHandler.username = username;
        PokemonHandler.loginFailed = false;
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                API = new PokemonGo(client);
                API.login(new PtcCredentialProvider(client, username, password));
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                API.setLocation(PokemonGO.GENERATOR.toLat(player.posZ), PokemonGO.GENERATOR.toLong(player.posX), 0);
                API.getInventories().updateInventories();
                List<CatchablePokemon> catchablePokemon = API.getMap().getCatchablePokemon();
                System.out.println(catchablePokemon.size() + " Pokemon!");
                for (CatchablePokemon cp : catchablePokemon) {
                    System.out.println(cp.getPokemonId() + " at " + cp.getLatitude() + " " + cp.getLongitude());
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
        }).start();
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
                                    new Thread(() -> {
                                        try {
                                            API.getPlayerProfile().acceptLevelUpRewards(level);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
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
                            for (HatchedEgg hatchedEgg : PokemonHandler.HATCHED_EGGS) {
                                System.out.println(hatchedEgg.getId());
                                System.out.println(API.getInventories().getPokebank().getPokemonById(hatchedEgg.getId()).getPokemonId().name());
                                System.out.println(hatchedEgg.getCandy());
                                System.out.println(hatchedEgg.getExperience());
                                System.out.println(hatchedEgg.getStardust());
                                //TODO Open GUI
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
                    List<HatchedEgg> hatchedEggs = PokemonHandler.API.getInventories().getHatchery().queryHatchedEggs();
                    synchronized (HATCHED_EGGS) {
                        HATCHED_EGGS.clear();
                        HATCHED_EGGS.addAll(hatchedEggs);
                    }
                    PokemonHandler.lastProfileUpdate = time;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clear() {
        synchronized (HATCHED_EGGS) {
            HATCHED_EGGS.clear();
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
