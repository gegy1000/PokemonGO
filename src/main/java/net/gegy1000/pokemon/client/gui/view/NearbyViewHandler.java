package net.gegy1000.pokemon.client.gui.view;

import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.inventory.Pokedex;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class NearbyViewHandler extends ViewHandler {
    public NearbyViewHandler(PokemonViewGUI gui) {
        super(gui);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        int textColor = LLibrary.CONFIG.getTextColor();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        ScaledResolution resolution = new ScaledResolution(this.mc);
        int tileSize = 21 * resolution.getScaleFactor();
        int tileRenderSize = tileSize - 2;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 4; y++) {
                this.drawRectangle(x * tileSize + 5, y * tileSize + 35, tileRenderSize, tileRenderSize, LLibrary.CONFIG.getSecondaryColor());
            }
        }
        Pokedex pokedex = PokemonHandler.API.getInventories().getPokedex();
        List<NearbyPokemon> nearbyPokemon = PokemonHandler.getNearbyPokemon();
        if (nearbyPokemon.size() > 0) {
            this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.spotted.name", nearbyPokemon.size()), 5, 23, textColor);
            int x = 0;
            int y = 0;
            Map<PokemonIdOuterClass.PokemonId, Integer> sortedNearbyPokemon = new HashMap<>();
            for (NearbyPokemon pokemon : nearbyPokemon) {
                Integer count = sortedNearbyPokemon.get(pokemon.getPokemonId());
                if (count == null) {
                    count = 1;
                } else {
                    count++;
                }
                sortedNearbyPokemon.put(pokemon.getPokemonId(), count);
            }
            GlStateManager.enableBlend();
            for (Map.Entry<PokemonIdOuterClass.PokemonId, Integer> pokemon : sortedNearbyPokemon.entrySet()) {
                PokemonIdOuterClass.PokemonId pokemonId = pokemon.getKey();
                AdvancedDynamicTexture texture = PokemonHandler.getTexture(pokemonId);
                int renderX = x * tileSize + 5;
                int renderY = y * tileSize + 35;
                if (texture != null) {
                    if (pokedex.getPokedexEntry(pokemonId) != null) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    } else {
                        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
                    }
                    texture.bind();
                    this.drawTexturedModalRect(renderX, renderY, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize, tileRenderSize);
                }
                if (pokemon.getValue() > 1) {
                    this.fontRenderer.drawString("x" + pokemon.getValue(), renderX + 2, renderY + tileRenderSize - 9, LLibrary.CONFIG.getTextColor(), false);
                }
                x++;
                if (x > 10) {
                    x = 0;
                    y++;
                }
                if (y > 4) {
                    break;
                }
            }
            x = 0;
            y = 0;
            for (Map.Entry<PokemonIdOuterClass.PokemonId, Integer> pokemon : sortedNearbyPokemon.entrySet()) {
                PokemonIdOuterClass.PokemonId pokemonId = pokemon.getKey();
                float renderX = x * tileSize + 5;
                float renderY = y * tileSize + 35;
                if (mouseX >= renderX && mouseX <= renderX + tileRenderSize && mouseY >= renderY && mouseY <= renderY + tileRenderSize) {
                    List<String> text = new LinkedList<>();
                    if (pokedex.getPokedexEntry(pokemonId) != null) {
                        text.add(TextFormatting.BLUE + PokemonHandler.getName(pokemonId));
                    }
                    this.drawHoveringText(text, (int) mouseX, (int) mouseY);
                }
                x++;
                if (x > 10) {
                    x = 0;
                    y++;
                }
                if (y > 4) {
                    break;
                }
            }
            GlStateManager.disableLighting();
        } else {
            this.fontRenderer.drawString(I18n.translateToLocal("gui.no_nearby.name"), 5, 23, textColor);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY) {

    }

    @Override
    public void initView() {

    }

    @Override
    public void cleanupView() {
    }
}
