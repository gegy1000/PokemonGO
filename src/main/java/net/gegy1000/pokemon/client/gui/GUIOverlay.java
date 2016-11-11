package net.gegy1000.pokemon.client.gui;

import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.inventory.Pokedex;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import com.pokegoapi.api.pokemon.EggPokemon;
import net.gegy1000.earth.Earth;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonMapHandler;
import net.gegy1000.pokemon.server.world.gen.WorldTypePokemonEarth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GUIOverlay extends Gui {
    private static final Minecraft MC = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onGuiRender(RenderGameOverlayEvent event) {
        EntityPlayerSP player = MC.thePlayer;
        if (PokemonHandler.API != null && MC.theWorld.getWorldType() instanceof WorldTypePokemonEarth) {
            Inventories inventories = PokemonHandler.API.getInventories();
            try {
                if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
                    this.renderSpeed(player);
                    this.renderEggs(inventories);
                } else if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
                    if (inventories != null) {
                        this.renderNearby(inventories);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void renderEggs(Inventories inventories) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        DecimalFormat shortDecimalFormat = new DecimalFormat("#.##", symbols);
        int eggY = 15;
        if (inventories != null && inventories.getHatchery() != null) {
            for (EggPokemon egg : inventories.getHatchery().getEggs()) {
                if (egg.isIncubate()) {
                    MC.fontRendererObj.drawString(shortDecimalFormat.format(egg.getEggKmWalked()) + "/" + egg.getEggKmWalkedTarget() + "km", 22, eggY + 2, 0xFFFFFF);
                    MC.getTextureManager().bindTexture(PokemonGUIHandler.getEggTexture(egg.getEggKmWalkedTarget()));
                    Gui.drawScaledCustomSizeModalRect(2, eggY, 0, 0, 1, 1, 18, 18, 1, 1);
                    GlStateManager.disableTexture2D();
                    Gui.drawRect(22, eggY + 11, 102, eggY + 16, 0xFF606060);
                    Gui.drawRect(23, eggY + 12, (int) (23 + ((Math.min(egg.getEggKmWalked(), egg.getEggKmWalkedTarget()) * 78.0) / egg.getEggKmWalkedTarget())), eggY + 15, 0xFF00FF60);
                    eggY += 20;
                }
            }
        }
    }

    private void renderSpeed(EntityPlayerSP player) {
        double move = PokemonHandler.getDistance(Earth.GENERATOR.toLat(player.posZ), Earth.GENERATOR.toLong(player.posX), Earth.GENERATOR.toLat(player.lastTickPosZ), Earth.GENERATOR.toLong(player.lastTickPosX));
        int speed = (int) ((move * 60.0) * 50.0 / 1000.0);
        TextFormatting textFormatting = TextFormatting.GREEN;
        if (speed > 24) {
            textFormatting = TextFormatting.YELLOW;
            if (speed > 120) {
                textFormatting = TextFormatting.RED;
            }
        }
        MC.fontRendererObj.drawString(textFormatting + I18n.translateToLocalFormatted("gui.speed.name", String.valueOf(speed)), 2, 2, 0xFFFFFF);
    }

    private void renderNearby(Inventories inventories) {
        boolean extra = false;
        GlStateManager.enableAlpha();
        Pokedex pokedex = inventories.getPokedex();
        List<NearbyPokemon> nearbyPokemon = PokemonMapHandler.getNearbyPokemon();
        Map<PokemonIdOuterClass.PokemonId, Integer> sortedNearbyPokemon = new HashMap<>();
        int displayCount = 0;
        for (NearbyPokemon pokemon : nearbyPokemon) {
            Integer count = sortedNearbyPokemon.get(pokemon.getPokemonId());
            if (count == null) {
                count = 1;
                if (sortedNearbyPokemon.size() >= 6) {
                    extra = true;
                }
                else
                {
                    displayCount++;
                }
            } else {
                count++;
            }
            sortedNearbyPokemon.put(pokemon.getPokemonId(), count);
        }
        GlStateManager.enableBlend();
        ScaledResolution resolution = new ScaledResolution(MC);
        int scale = resolution.getScaleFactor();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0);
        int index = 0;
        int renderX = resolution.getScaledWidth() / scale / 2 - (displayCount * 13) / 2;
        int renderY = -3;
        for (Map.Entry<PokemonIdOuterClass.PokemonId, Integer> pokemon : sortedNearbyPokemon.entrySet()) {
            PokemonIdOuterClass.PokemonId pokemonId = pokemon.getKey();
            AdvancedDynamicTexture texture = PokemonGUIHandler.getTexture(pokemonId);
            if (texture != null) {
                if (pokedex.getPokedexEntry(pokemonId) != null) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                } else {
                    GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
                }
                texture.bind();
                Gui.drawScaledCustomSizeModalRect(renderX, renderY, 0, 0, 1, 1, 20, 20, 1, 1);
            }
            if (pokemon.getValue() > 1) {
                GlStateManager.pushMatrix();
                float textScale = 0.25F;
                GlStateManager.scale(textScale, textScale, 1.0F);
                MC.fontRendererObj.drawString("x" + pokemon.getValue(), (renderX + 8) / textScale, (renderY + 15) / textScale, 0, false);
                GlStateManager.popMatrix();
            }
            renderX += 13;
            if (index++ >= 5) {
                break;
            }
        }
        if (extra) {
            float textScale = 0.5F;
            GlStateManager.pushMatrix();
            GlStateManager.scale(textScale, textScale, 1.0F);
            MC.fontRendererObj.drawString("...", ((resolution.getScaledWidth() - MC.fontRendererObj.getStringWidth("...")) / scale / 2) / textScale, 12 / textScale, 0xFFFFFF, true);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
