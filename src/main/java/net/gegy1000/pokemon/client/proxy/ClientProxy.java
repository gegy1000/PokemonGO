package net.gegy1000.pokemon.client.proxy;

import net.gegy1000.pokemon.client.event.ClientEventHandler;
import net.gegy1000.pokemon.client.gui.GUIOverlay;
import net.gegy1000.pokemon.client.key.PokemonKeyBinds;
import net.gegy1000.pokemon.client.renderer.RenderHandler;
import net.gegy1000.pokemon.server.proxy.ServerProxy;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends ServerProxy {
    @Override
    public void preInit() {
        super.preInit();

        PokemonKeyBinds.onPreInit();
        RenderHandler.onPreInit();

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new GUIOverlay());
    }

    @Override
    public void postInit() {
        super.postInit();
    }
}
