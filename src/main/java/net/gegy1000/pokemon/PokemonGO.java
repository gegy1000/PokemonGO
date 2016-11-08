package net.gegy1000.pokemon;

import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.server.proxy.ServerProxy;
import net.gegy1000.pokemon.server.world.gen.PokemonEarthGenerator;
import net.gegy1000.pokemon.server.world.gen.WorldTypePokemonEarth;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(modid = PokemonGO.MODID, name = "PokemonGO", version = PokemonGO.VERSION, dependencies = "required-after:llibrary@[" + PokemonGO.LLIBRARY_VERSION + ",);required-after:earth@[1.1.0,]")
public class PokemonGO {
    public static final PokemonEarthGenerator GENERATOR = new PokemonEarthGenerator();

    @SidedProxy(clientSide = "net.gegy1000.pokemon.client.proxy.ClientProxy", serverSide = "net.gegy1000.pokemon.server.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static final String MODID = "pokemongo";
    public static final String VERSION = "1.0.0";
    public static final String LLIBRARY_VERSION = "1.7.1";

    public static final Logger LOGGER = LogManager.getLogger("PokemonGO");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ProgressManager.ProgressBar bar = ProgressManager.push("Loading Earth (Pokemon) Maps", 2);
        try {
            GENERATOR.load(bar);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ProgressManager.pop(bar);

        new WorldTypePokemonEarth(GENERATOR);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PokemonHandler.onPreInit();
        proxy.postInit();
    }
}
