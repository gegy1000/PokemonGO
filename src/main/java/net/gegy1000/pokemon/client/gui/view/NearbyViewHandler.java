package net.gegy1000.pokemon.client.gui.view;

import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import com.pokegoapi.util.PokeNames;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonSpriteHandler;
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
import java.util.Locale;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class NearbyViewHandler extends ViewHandler {
    private List<NearbyPokemon> nearbyPokemon = null;
    private boolean loadingNearbyPokemon;
    private boolean nearbyFailed;

    public NearbyViewHandler(PokemonViewGUI gui) {
        super(gui);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        int textColor = LLibrary.CONFIG.getTextColor();
        if (this.nearbyPokemon == null && !this.loadingNearbyPokemon && !this.nearbyFailed) {
            new Thread(() -> {
                try {
                    NearbyViewHandler.this.nearbyPokemon = PokemonHandler.GO.getMap().getNearbyPokemon();
                    NearbyViewHandler.this.loadingNearbyPokemon = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    NearbyViewHandler.this.nearbyFailed = true;
                }
            }).start();
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        ScaledResolution resolution = new ScaledResolution(this.mc);
        int tileSize = 21 * resolution.getScaleFactor();
        int tileRenderSize = tileSize - 2;
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 4; y++) {
                this.drawRectangle(x * tileSize + 5, y * tileSize + 35, tileRenderSize, tileRenderSize, LLibrary.CONFIG.getSecondaryColor());
            }
        }
        if (this.nearbyPokemon != null && !this.loadingNearbyPokemon && !this.nearbyFailed) {
            if (this.nearbyPokemon.size() > 0) {
                this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.spotted.name", this.nearbyPokemon.size()), 5, 23, textColor);
                int x = 0;
                int y = 0;
                Map<PokemonIdOuterClass.PokemonId, Integer> nearbyPokemon = new HashMap<>();
                for (NearbyPokemon pokemon : this.nearbyPokemon) {
                    Integer count = nearbyPokemon.get(pokemon.getPokemonId());
                    if (count == null) {
                        count = 1;
                    } else {
                        count++;
                    }
                    nearbyPokemon.put(pokemon.getPokemonId(), count);
                }
                for (Map.Entry<PokemonIdOuterClass.PokemonId, Integer> pokemon : nearbyPokemon.entrySet()) {
                    PokemonIdOuterClass.PokemonId pokemonId = pokemon.getKey();
                    AdvancedDynamicTexture texture = PokemonSpriteHandler.get(pokemonId);
                    int renderX = x * tileSize + 5;
                    int renderY = y * tileSize + 35;
                    if (texture != null) {
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
                for (Map.Entry<PokemonIdOuterClass.PokemonId, Integer> pokemon : nearbyPokemon.entrySet()) {
                    PokemonIdOuterClass.PokemonId pokemonId = pokemon.getKey();
                    float renderX = x * tileSize + 5;
                    float renderY = y * tileSize + 35;
                    if (mouseX >= renderX && mouseX <= renderX + tileRenderSize && mouseY >= renderY && mouseY <= renderY + tileRenderSize) {
                        List<String> text = new LinkedList<>();
                        text.add(TextFormatting.BLUE + PokeNames.getDisplayName(pokemonId.getNumber(), Locale.ENGLISH));
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
        } else if (this.nearbyFailed) {
            this.fontRenderer.drawString(I18n.translateToLocal("gui.nearby_failed.name"), 5, 23, textColor);
        } else {
            this.fontRenderer.drawString(I18n.translateToLocal("gui.searching_nearby"), 5, 23, textColor);
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
        this.nearbyPokemon = null;
        this.nearbyFailed = false;
        this.loadingNearbyPokemon = false;
    }
}
