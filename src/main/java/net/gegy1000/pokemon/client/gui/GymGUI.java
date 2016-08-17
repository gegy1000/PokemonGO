package net.gegy1000.pokemon.client.gui;

import POGOProtos.Data.PokemonDataOuterClass;
import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.util.PokeNames;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonSpriteHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class GymGUI extends PokemonGUI {
    private Gym gym;
    private String name;
    private String description;

    public GymGUI(Gym gym) {
        try {
            this.gym = gym;
            this.name = gym.getName();
            this.description = gym.getDescription();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initElements() {
        ScaledResolution resolution = new ScaledResolution(this.mc);
        ElementHandler.INSTANCE.addElement(this, new InventoryGridElement<>(this, this.width / 8.0F - (resolution.getScaleFactor() * 12), 60.0F, resolution.getScaleFactor() * 50, resolution.getScaleFactor() * 72, 2, resolution.getScaleFactor() * 24, (slotRenderer) -> {
            try {
                List<PokemonDataOuterClass.PokemonData> defendingPokemon = this.gym.getDefendingPokemon();
                int tileRenderSize = slotRenderer.getGrid().getRenderTileSize();
                slotRenderer.draw((slot) -> {
                    try {
                        if (slot.getIndex() < defendingPokemon.size()) {
                            PokemonDataOuterClass.PokemonData pokemon = defendingPokemon.get(slot.getIndex());
                            AdvancedDynamicTexture texture = PokemonSpriteHandler.get(pokemon.getPokemonId());
                            if (texture != null) {
                                texture.bind();
                                this.drawTexturedModalRect(slot.getX(), slot.getY(), 0, 0, tileRenderSize, tileRenderSize, tileRenderSize, tileRenderSize, 1.0, 1.0);
                            }
                            this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()), (int) slot.getX() + 1, (int) slot.getY() + tileRenderSize - 8, LLibrary.CONFIG.getTextColor());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }, (slot) -> {
                    List<String> tooltip = new LinkedList<>();
                    try {
                        if (slot.getIndex() < defendingPokemon.size()) {
                            List<String> text = new ArrayList<>();
                            PokemonDataOuterClass.PokemonData pokemon = defendingPokemon.get(slot.getIndex());
                            text.add(TextFormatting.BLUE + PokeNames.getDisplayName(pokemon.getPokemonId().getNumber(), Locale.ENGLISH));
                            text.add(TextFormatting.GREEN + I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()));
                            return text;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return tooltip;
                }, Math.max(6, defendingPokemon.size()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }));

        ElementHandler.INSTANCE.addElement(this, new InventoryGridElement<>(this, this.width - this.width / 8.0F - (resolution.getScaleFactor() * 32), 60.0F, resolution.getScaleFactor() * 50, resolution.getScaleFactor() * 72, 2, resolution.getScaleFactor() * 24, (slotRenderer) -> {
            try {
                PokeBank pokebank = PokemonHandler.GO.getInventories().getPokebank();
                List<Pokemon> pokemons = pokebank.getPokemons();
                int tileRenderSize = slotRenderer.getGrid().getRenderTileSize();
                slotRenderer.draw((slot) -> {
                    try {
                        if (slot.getIndex() < pokemons.size()) {
                            Pokemon pokemon = pokemons.get(slot.getIndex());
                            AdvancedDynamicTexture texture = PokemonSpriteHandler.get(pokemon.getPokemonId());
                            if (texture != null) {
                                texture.bind();
                                this.drawTexturedModalRect(slot.getX(), slot.getY(), 0, 0, tileRenderSize, tileRenderSize, tileRenderSize, tileRenderSize, 1.0, 1.0);
                            }
                            this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()), (int) slot.getX() + 1, (int) slot.getY() + tileRenderSize - 8, LLibrary.CONFIG.getTextColor());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }, (slot) -> {
                    List<String> tooltip = new LinkedList<>();
                    try {
                        if (slot.getIndex() < pokemons.size()) {
                            List<String> text = new ArrayList<>();
                            Pokemon pokemon = pokemons.get(slot.getIndex());
                            text.add(TextFormatting.BLUE + PokeNames.getDisplayName(pokemon.getPokemonId().getNumber(), Locale.ENGLISH));
                            text.add(TextFormatting.GREEN + I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()));
                            return text;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return tooltip;
                }, Math.max(6, pokemons.size()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }));
    }

    @Override
    public void drawScreen(float mouseX, float mouseY, float partialTicks) {
        try {
            if (PokemonHandler.GO != null) {
                int textColor = LLibrary.CONFIG.getTextColor();

                this.drawRectangle(0, this.height - 18.0F, this.width, 18.0F, LLibrary.CONFIG.getPrimaryColor());
                this.drawRectangle(0, 0, this.width, 34.0F, LLibrary.CONFIG.getPrimaryColor());
                String titleString = I18n.translateToLocal("pokemon.gym.name") + " - " + this.name;
                this.fontRendererObj.drawString(titleString, this.width / 2 - this.fontRendererObj.getStringWidth(titleString) / 2, 6, textColor, false);
                String status = I18n.translateToLocal("gui.attackable.name");
                if (!this.gym.isAttackable()) {
                    if (!this.gym.inRange()) {
                        status = I18n.translateToLocal("gui.far.name");
                    } else {
                        status = I18n.translateToLocal("gui.not_attackable.name");
                    }
                }
                if (this.gym.getIsInBattle()) {
                    status = I18n.translateToLocal("gui.in_battle.name");
                }
                status = "* " + status + " *";
                this.fontRendererObj.drawString(status, this.width / 2 - this.fontRendererObj.getStringWidth(status) / 2, this.height - 12, textColor, false);
                String description = "\"" + (this.description.length() == 0 ? I18n.translateToLocal("gui.no_description.name") : this.description) + "\"";
                this.fontRendererObj.drawString(description, this.width / 2 - this.fontRendererObj.getStringWidth(description) / 2, 24.0F, textColor, false);
                Team gymTeam = new Team(this.gym.getOwnedByTeam());
                this.mc.getTextureManager().bindTexture(gymTeam.getTeamTexture());
                this.drawTexturedModalRect(2, 2, 0, 0, 32, 32, 32, 32, 1.0, 1.0);
                this.fontRendererObj.drawString(gymTeam.getTeamName(), 40, 15, textColor);

                Team team = new Team(PokemonHandler.GO.getPlayerProfile().getPlayerData().getTeam());
                this.mc.getTextureManager().bindTexture(team.getTeamTexture());
                this.drawTexturedModalRect(this.width - 34, 2, 0, 0, 32, 32, 32, 32, 1.0, 1.0);
                this.fontRendererObj.drawString(team.getTeamName(), this.width - (this.fontRendererObj.getStringWidth(team.getTeamName()) + 40), 15, textColor);

                this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.points.name", this.gym.getPoints()), 5, this.height - 12, textColor);

                this.fontRendererObj.drawString(I18n.translateToLocal("gui.defense.name"), this.width / 8, 40, textColor);
                String me = I18n.translateToLocal("gui.me.name");
                this.fontRendererObj.drawString(me, this.width - this.width / 8 - this.fontRendererObj.getStringWidth(me) - 15, 40, textColor);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
