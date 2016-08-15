package net.gegy1000.pokemon.pokemon;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.device.DeviceInfo;
import com.pokegoapi.api.device.SensorInfo;
import com.pokegoapi.auth.PtcCredentialProvider;
import net.gegy1000.pokemon.PokemonGO;
import net.minecraft.entity.player.EntityPlayer;
import okhttp3.OkHttpClient;

public class PokemonHandler {
    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static PokemonGo GO;
    public static boolean loggingIn;
    public static boolean loginFailed;
    public static String username;

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
            if (GO != null) {
                GO.setLocation(PokemonGO.GENERATOR.toLat(player.posZ), PokemonGO.GENERATOR.toLong(player.posX), (player.posY - 22) * 34.5625);
            }
        }
        PokemonSpriteHandler.update();
    }
}
