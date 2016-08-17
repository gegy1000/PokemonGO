package net.gegy1000.pokemon.client.util;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.device.DeviceInfo;
import com.pokegoapi.api.device.SensorInfo;
import com.pokegoapi.auth.PtcCredentialProvider;
import net.gegy1000.pokemon.PokemonGO;
import net.ilexiconn.llibrary.server.snackbar.Snackbar;
import net.minecraft.entity.player.EntityPlayer;
import okhttp3.OkHttpClient;

public class PokemonHandler {
    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static PokemonGo GO;
    public static boolean loggingIn;
    public static boolean loginFailed;
    public static String username;
    public static int level;

    public static void authenticate(String username, String password) {
        PokemonHandler.loggingIn = true;
        PokemonHandler.username = username;
        PokemonHandler.loginFailed = false;
        new Thread(() -> {
            try {
                GO = new PokemonGo(new PtcCredentialProvider(CLIENT, username, password), CLIENT);
                GO.setDeviceInfo(DeviceInfo.DEFAULT);
                SensorInfo sensorInfo = new SensorInfo();
                sensorInfo.setAngleNormalizedY(0);
                GO.setSensorInfo(sensorInfo);
                String pokemonUsername = GO.getPlayerProfile().getPlayerData().getUsername();
                PokemonHandler.username = pokemonUsername != null && pokemonUsername.length() > 0 ? pokemonUsername : PokemonHandler.username;
                PokemonHandler.level = GO.getPlayerProfile().getStats().getLevel();
                PokemonHandler.loggingIn = false;
            } catch (Exception e) {
                System.err.println("Failed to authenticate.");
                e.printStackTrace();
                PokemonHandler.loggingIn = false;
                PokemonHandler.loginFailed = true;
            }
        }).start();
    }

    public static void update(EntityPlayer player) {
        if (player != null && player.ticksExisted % 4 == 0) {
            if (GO != null && !loggingIn) {
                GO.setLocation(PokemonGO.GENERATOR.toLat(player.posZ), PokemonGO.GENERATOR.toLong(player.posX), (player.posY - 22) * 34.5625);
                try {
                    if (player.ticksExisted % 6000 == 0) {
                        GO.getPlayerProfile().updateProfile();
                    }
                    int level = GO.getPlayerProfile().getStats().getLevel();
                    if (PokemonHandler.level != level) {
                        if (level > PokemonHandler.level) {
                            Snackbar.create("Level up! Rewards added to inventory.");
                            new Thread(() -> {
                                try {
                                    GO.getPlayerProfile().acceptLevelUpRewards(level);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        PokemonHandler.level = level;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        PokemonSpriteHandler.update();
    }
}
