package net.gegy1000.pokemon.client.gui;

import POGOProtos.Data.PokemonDataOuterClass;
import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.pokemon.Pokemon;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GymGUI extends PokemonGUI {
    public static final long[] LEVEL_POINTS = new long[] { 2000, 4000, 8000, 12000, 16000, 20000, 30000, 40000, 50000 };

    private Gym gym;
    private String name;
    private String description;

    private ButtonElement<GymGUI> attackButton;

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

        this.addElement(this.attackButton = new ButtonElement<>(this, I18n.translateToLocal("gui.attack.name"), this.width / 2.0F - (resolution.getScaleFactor() * 32), this.height / 2.0F - 15, resolution.getScaleFactor() * 64, 30, (button) -> {
            try {
                if (this.gym.isAttackable() && !this.gym.getIsInBattle()) {
                    PokeBank pokebank = PokemonHandler.API.getInventories().getPokebank();
                    List<Pokemon> pokemons = new ArrayList<>(pokebank.getPokemons());
                    pokemons.sort((pokemon1, pokemon2) -> Integer.compare(pokemon2.getCp(), pokemon1.getCp()));
                    int count = Math.min(pokemons.size(), 6);
                    Pokemon[] team = new Pokemon[count];
                    int teamIndex = 0;
                    for (int i = 0; i < count; i++) {
                        Pokemon pokemon = pokemons.get(i);
                        if (pokemon.getStamina() >= pokemon.getMaxStamina()) {
                            team[teamIndex] = pokemon;
                            teamIndex++;
                        }
                    }
                    this.mc.displayGuiScreen(new GymAttackGUI(this.gym, team));
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }));

        this.addElement(new InventoryGridElement<>(this, this.width / 8.0F - (resolution.getScaleFactor() * 12), 60.0F, resolution.getScaleFactor() * 50, resolution.getScaleFactor() * 72, 2, resolution.getScaleFactor() * 24, (slotRenderer) -> {
            try {
                List<PokemonDataOuterClass.PokemonData> defendingPokemon = this.gym.getDefendingPokemon();
                int tileRenderSize = slotRenderer.getGrid().getRenderTileSize();
                slotRenderer.draw((slot) -> {
                    try {
                        if (slot.getIndex() < defendingPokemon.size()) {
                            PokemonDataOuterClass.PokemonData pokemon = defendingPokemon.get(slot.getIndex());
                            AdvancedDynamicTexture texture = PokemonGUIHandler.getTexture(pokemon.getPokemonId());
                            if (texture != null) {
                                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
                            text.add(TextFormatting.BLUE + PokemonGUIHandler.getName(pokemon.getPokemonId()));
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

        this.addElement(new InventoryGridElement<>(this, this.width - this.width / 8.0F - (resolution.getScaleFactor() * 35), 60.0F, resolution.getScaleFactor() * 50, resolution.getScaleFactor() * 72, 2, resolution.getScaleFactor() * 24, (slotRenderer) -> {
            try {
                PokeBank pokebank = PokemonHandler.API.getInventories().getPokebank();
                List<Pokemon> pokemons = new ArrayList<>(pokebank.getPokemons());
                pokemons.sort((pokemon1, pokemon2) -> Integer.compare(pokemon2.getCp(), pokemon1.getCp()));
                int tileRenderSize = slotRenderer.getGrid().getRenderTileSize();
                slotRenderer.draw((slot) -> {
                    try {
                        if (slot.getIndex() < pokemons.size()) {
                            Pokemon pokemon = pokemons.get(slot.getIndex());
                            AdvancedDynamicTexture texture = PokemonGUIHandler.getTexture(pokemon.getPokemonId());
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                            if (texture != null) {
                                texture.bind();
                                this.drawTexturedModalRect(slot.getX(), slot.getY(), 0, 0, tileRenderSize, tileRenderSize, tileRenderSize, tileRenderSize, 1.0, 1.0);
                            }
                            this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()), (int) slot.getX() + 1, (int) slot.getY() + tileRenderSize - 8, LLibrary.CONFIG.getTextColor());
                            GlStateManager.disableTexture2D();
                            this.drawRectangle(slot.getX() + 1, slot.getY() + 1, tileRenderSize - 2, 4, LLibrary.CONFIG.getPrimaryColor());
                            this.drawRectangle(slot.getX() + 2, slot.getY() + 2, pokemon.getStamina() * (tileRenderSize - 4) / pokemon.getMaxStamina(), 2, 0xFF4AD33C);
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
                            text.add(TextFormatting.BLUE + PokemonGUIHandler.getName(pokemon.getPokemonId()));
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
            if (PokemonHandler.API != null) {
                int textColor = LLibrary.CONFIG.getTextColor();

                this.drawRectangle(0, this.height - 18.0F, this.width, 18.0F, LLibrary.CONFIG.getPrimaryColor());
                this.drawRectangle(0, 0, this.width, 34.0F, LLibrary.CONFIG.getPrimaryColor());
                String titleString = I18n.translateToLocal("pokemon.gym.name") + " - " + this.name;
                this.fontRendererObj.drawString(titleString, this.width / 2 - this.fontRendererObj.getStringWidth(titleString) / 2, 6, textColor, false);
                String status = I18n.translateToLocal("gui.attackable.name");
                boolean attackable = true;
                if (!this.gym.isAttackable()) {
                    if (!this.gym.inRange()) {
                        status = I18n.translateToLocal("gui.far.name");
                    } else {
                        status = I18n.translateToLocal("gui.not_attackable.name");
                    }
                    attackable = false;
                }
                if (this.gym.getIsInBattle()) {
                    status = I18n.translateToLocal("gui.in_battle.name");
                    attackable = false;
                }
                this.attackButton.setEnabled(attackable);
                status = "* " + status + " *";
                this.fontRendererObj.drawString(status, this.width / 2 - this.fontRendererObj.getStringWidth(status) / 2, this.height - 12, textColor, false);
                String description = "\"" + (this.description.length() == 0 ? I18n.translateToLocal("gui.no_description.name") : this.description) + "\"";
                this.fontRendererObj.drawString(description, this.width / 2 - this.fontRendererObj.getStringWidth(description) / 2, 24.0F, textColor, false);

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                Team gymTeam = new Team(this.gym.getOwnedByTeam());
                this.mc.getTextureManager().bindTexture(gymTeam.getTeamTexture());
                this.drawTexturedModalRect(2, 2, 0, 0, 32, 32, 32, 32, 1.0, 1.0);

                Team team = new Team(PokemonHandler.API.getPlayerProfile().getPlayerData().getTeam());
                this.mc.getTextureManager().bindTexture(team.getTeamTexture());
                this.drawTexturedModalRect(this.width - 34, 2, 0, 0, 32, 32, 32, 32, 1.0, 1.0);

                this.fontRendererObj.drawString(gymTeam.getTeamName(), 40, 15, textColor);
                this.fontRendererObj.drawString(team.getTeamName(), this.width - (this.fontRendererObj.getStringWidth(team.getTeamName()) + 40), 15, textColor);

                int level = 1;
                long points = this.gym.getPoints();
                for (long levelPoints : LEVEL_POINTS) {
                    if (points >= levelPoints) {
                        level++;
                    }
                }
                this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.level.name", level), 5, this.height - 12, textColor);

                this.fontRendererObj.drawString(I18n.translateToLocal("gui.defense.name"), this.width / 8, 40, textColor);
                String me = I18n.translateToLocal("gui.me.name");
                this.fontRendererObj.drawString(me, this.width - this.width / 8 - this.fontRendererObj.getStringWidth(me) - 15, 40, textColor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
