package net.gegy1000.pokemon.client.gui.view.inventory;

import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Networking.Responses.NicknamePokemonResponseOuterClass;
import com.pokegoapi.api.inventory.CandyJar;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.map.pokemon.EvolutionResult;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.pokemon.PokemonMoveMetaRegistry;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.gui.PokemonGUI;
import net.gegy1000.pokemon.client.gui.element.DrawableElement;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonUtils;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.ClientProxy;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.Element;
import net.ilexiconn.llibrary.client.gui.element.InputElement;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PokebankHandler extends InventoryHandler {
    @Override
    public void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) throws Exception {
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.pokebank.name", inventories.getPokebank().getPokemons().size(), PokemonHandler.API.getPlayerProfile().getPlayerData().getMaxPokemonStorage()), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        PokeBank pokebank = inventories.getPokebank();
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        List<Pokemon> pokemons = new ArrayList<>(pokebank.getPokemons());
        pokemons.sort((pokemon1, pokemon2) -> {
            int pokemonId1 = pokemon1.getPokemonId().getNumber();
            int pokemonId2 = pokemon2.getPokemonId().getNumber();
            if (pokemonId1 == pokemonId2) {
                return Integer.compare(pokemon2.getCp(), pokemon1.getCp());
            } else {
                return Integer.compare(pokemonId1, pokemonId2);
            }
        });
        slotHandler.draw((slot) -> {
            Pokemon pokemon = pokemons.get(slot.getIndex());
            AdvancedDynamicTexture texture = PokemonGUIHandler.getTexture(pokemon.getPokemonId());
            if (texture != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                texture.bind();
                this.drawTexturedModalRect(slot.getX(), slot.getY(), 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize, tileRenderSize);
            }
            this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()), (int) slot.getX() + 1, (int) slot.getY() + tileRenderSize - 8, LLibrary.CONFIG.getTextColor());
            GlStateManager.disableTexture2D();
            this.drawRectangle(slot.getX() + 1, slot.getY() + 1, tileRenderSize - 2, 4, LLibrary.CONFIG.getPrimaryColor());
            this.drawRectangle(slot.getX() + 2, slot.getY() + 2, pokemon.getStamina() * (tileRenderSize - 4) / pokemon.getMaxStamina(), 2, 0xFF4AD33C);
            return null;
        }, (slot) -> {
            List<String> text = new ArrayList<>();
            Pokemon pokemon = pokemons.get(slot.getIndex());
            text.add(TextFormatting.BLUE + (pokemon.getNickname() != null && pokemon.getNickname().length() > 0 ? pokemon.getNickname() : PokemonGUIHandler.getName(pokemon.getPokemonId())));
            text.add(TextFormatting.GREEN + I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()));
            return text;
        }, pokemons.size());
    }

    @Override
    public void click(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        PokeBank pokebank = inventories.getPokebank();
        List<Pokemon> pokemons = new ArrayList<>(pokebank.getPokemons());
        pokemons.sort((pokemon1, pokemon2) -> {
            int pokemonId1 = pokemon1.getPokemonId().getNumber();
            int pokemonId2 = pokemon2.getPokemonId().getNumber();
            if (pokemonId1 == pokemonId2) {
                return Integer.compare(pokemon2.getCp(), pokemon1.getCp());
            } else {
                return Integer.compare(pokemonId1, pokemonId2);
            }
        });
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        slotHandler.click(slot -> {
            this.openPokemonView(resolution, pokemons.get(slot.getIndex()));
            return true;
        }, pokemons.size());
    }

    private void openPokemonView(ScaledResolution resolution, Pokemon pokemon) {
        try {
            String displayName = PokemonGUIHandler.getName(pokemon.getPokemonId());
            int scaleFactor = resolution.getScaleFactor();
            WindowElement<PokemonViewGUI> window = new WindowElement<>(this.getGUI(), I18n.translateToLocalFormatted("gui.pokemon_info.name", displayName), scaleFactor * 100, scaleFactor * 100, true);
            int centerX = scaleFactor * 50;
            int iconSize = scaleFactor * 35;
            int nameWidth = scaleFactor * 40;
            String nickname = pokemon.getNickname();
            int buttonHeight = scaleFactor * 6;
            int buttonOffset = scaleFactor * 20;
            int buttonY = scaleFactor * (100 - 33);
            CandyJar candyJar = PokemonHandler.API.getInventories().getCandyjar();
            boolean canEvolve = candyJar.getCandies(pokemon.getPokemonFamily()) >= pokemon.getCandiesToEvolve() && pokemon.getCandiesToEvolve() > 0;
            boolean canPowerUpCandy = candyJar.getCandies(pokemon.getPokemonFamily()) >= pokemon.getCandyCostsForPowerup();
            boolean canPowerUpStardust = PokemonHandler.API.getPlayerProfile().getCurrencies().get(PlayerProfile.Currency.STARDUST) >= pokemon.getStardustCostsForPowerup();
            boolean canPowerUp = canPowerUpCandy && canPowerUpStardust;
            int textColor = LLibrary.CONFIG.getTextColor();
            int fontHeight = (int) (this.fontRenderer.FONT_HEIGHT / 2.5F * scaleFactor);
            new InputElement<>(this.getGUI(), centerX - nameWidth / 2, iconSize + 26, nameWidth, nickname != null && nickname.length() > 0 ? nickname : displayName, (input) -> {
                PokemonHandler.addTask(() -> {
                    try {
                        if (!input.getText().equals(nickname)) {
                            String newNickname = input.getText().equals(displayName) ? "" : input.getText();
                            if (pokemon.renamePokemon(newNickname) == NicknamePokemonResponseOuterClass.NicknamePokemonResponse.Result.SUCCESS) {
                                pokemon.setProto(pokemon.getProto().toBuilder().mergeFrom(PokemonDataOuterClass.PokemonData.newBuilder().setNickname(newNickname).build()).build());
                            } else {
                                input.clearText();
                                String newName = pokemon.getNickname() == null || pokemon.getNickname().length() == 0 ? displayName : pokemon.getNickname();
                                input.writeText(newName);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                });
            }).withParent(window);
            new DrawableElement<>(this.getGUI(), 0.0F, 14.0F, scaleFactor * 100, scaleFactor * 100 - 14, (mousePosition) -> {
                try {
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
                    symbols.setDecimalSeparator('.');
                    symbols.setGroupingSeparator(',');
                    DecimalFormat shortDecimalFormat = new DecimalFormat("#.##", symbols);
                    int x = centerX - iconSize / 2;
                    int y = 10;
                    this.drawRectangle(x, y, iconSize, iconSize, LLibrary.CONFIG.getSecondaryColor());
                    AdvancedDynamicTexture texture = PokemonGUIHandler.getTexture(pokemon.getPokemonId());
                    if (texture != null) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        texture.bind();
                        this.drawTexturedModalRect(x, y, 0.0F, 0.0F, 1.0F, 1.0F, iconSize, iconSize);
                    }
                    String iv = I18n.translateToLocalFormatted("gui.iv.name", (int) PokemonUtils.calculateIV(pokemon) + "%");
                    this.fontRenderer.drawString(iv, centerX - this.fontRenderer.getStringWidth(iv) / 2, y + iconSize - 10, textColor);
                    GlStateManager.disableTexture2D();
                    this.drawRectangle(buttonOffset / 2, buttonY, scaleFactor * 100 - buttonOffset, buttonHeight, LLibrary.CONFIG.getSecondaryColor());
                    this.drawRectangle(buttonOffset / 2, buttonY + buttonHeight + 4, scaleFactor * 100 - buttonOffset, buttonHeight, LLibrary.CONFIG.getSecondaryColor());
                    int healthWidth = scaleFactor * 30;
                    this.drawRectangle(centerX - healthWidth / 2 - 1, y + iconSize + 16, healthWidth + 2, 11, LLibrary.CONFIG.getSecondaryColor());
                    this.drawRectangle(centerX - healthWidth / 2, y + iconSize + 17, pokemon.getMaxStamina() <= 0 ? 0 : (pokemon.getStamina() * healthWidth) / pokemon.getMaxStamina(), 9, 0xFF4AD33C);
                    String hp = I18n.translateToLocalFormatted("gui.hp.name", pokemon.getStamina(), pokemon.getMaxStamina());
                    this.fontRenderer.drawString(hp, centerX - this.fontRenderer.getStringWidth(hp) / 2, y + iconSize + 18, textColor);
                    String cp = I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp());
                    this.fontRenderer.drawString(cp, centerX - this.fontRenderer.getStringWidth(cp) / 2, y + iconSize + 29, textColor);
                    String weight = I18n.translateToLocalFormatted("gui.weight.name", shortDecimalFormat.format(pokemon.getWeightKg()));
                    this.fontRenderer.drawString(weight, centerX - this.fontRenderer.getStringWidth(weight) / 2, y + iconSize + 39, textColor);
                    String primaryMove = I18n.translateToLocal("move." + pokemon.getMove1().name().toLowerCase(Locale.ENGLISH) + ".name");
                    this.fontRenderer.drawString(TextFormatting.ITALIC + primaryMove, (int) ((scaleFactor * 16.5) - this.fontRenderer.getStringWidth(primaryMove) / 2), 10, textColor, false);
                    String primaryPower = "" + PokemonMoveMetaRegistry.getMeta(pokemon.getMove1()).getPower();
                    this.fontRenderer.drawString(primaryPower, (int) ((scaleFactor * 16.5) - this.fontRenderer.getStringWidth(primaryPower) / 2), 20, textColor, false);
                    String secondaryMove = I18n.translateToLocal("move." + pokemon.getMove2().name().toLowerCase(Locale.ENGLISH) + ".name");
                    this.fontRenderer.drawString(TextFormatting.ITALIC + secondaryMove, (scaleFactor * 84) - this.fontRenderer.getStringWidth(secondaryMove) / 2, 10, textColor, false);
                    String secondaryPower = "" + PokemonMoveMetaRegistry.getMeta(pokemon.getMove2()).getPower();
                    this.fontRenderer.drawString(secondaryPower, (scaleFactor * 84) - this.fontRenderer.getStringWidth(secondaryPower) / 2, 20, textColor, false);
                    this.fontRenderer.drawString(String.valueOf(canPowerUpCandy ? "" : TextFormatting.RED) + pokemon.getCandyCostsForPowerup(), buttonOffset / 2 + scaleFactor * 50, buttonY + buttonHeight / 2 - fontHeight / 2, textColor);
                    this.fontRenderer.drawString(String.valueOf(canPowerUpStardust ? "" : TextFormatting.RED) + pokemon.getStardustCostsForPowerup(), (int) (buttonOffset * 1.3) + scaleFactor * 50, buttonY + buttonHeight / 2 - fontHeight / 2, textColor);
                    if (pokemon.getCandiesToEvolve() != 0) {
                        this.fontRenderer.drawString(String.valueOf(canEvolve ? "" : TextFormatting.RED) + pokemon.getCandiesToEvolve(), buttonOffset + scaleFactor * 50, buttonY + resolution.getScaleFactor() * 8 + buttonHeight / 2 - fontHeight / 2, textColor);
                    }
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    ClientProxy.MINECRAFT.getTextureManager().bindTexture(PokemonGUIHandler.getTexture(pokemon.getPokemonFamily()));
                    int costIconSize = (int) (scaleFactor * 5.5);
                    if (pokemon.getCandiesToEvolve() != 0) {
                        this.drawTexturedModalRect(buttonOffset + scaleFactor * 43, buttonY + resolution.getScaleFactor() * 8 + 1, 0.0F, 0.0F, 1.0F, 1.0F, costIconSize, costIconSize);
                    }
                    this.drawTexturedModalRect(buttonOffset / 2 + scaleFactor * 43, buttonY + 1, 0.0F, 0.0F, 1.0F, 1.0F, costIconSize, costIconSize);
                    ClientProxy.MINECRAFT.getTextureManager().bindTexture(PokemonGUI.STARDUST_TEXTURE);
                    this.drawTexturedModalRect((int) (buttonOffset * 1.3) + scaleFactor * 45, buttonY + 1, 0.0F, 0.0F, 1.0F, 1.0F, costIconSize, costIconSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).withParent(window);
            new ButtonElement<>(this.getGUI(), I18n.translateToLocal("gui.release.name"), 1.0F, scaleFactor * 100 - 21, scaleFactor * 100 - 2, 20, (button) -> {
                PokemonHandler.addTask(() -> {
                    try {
                        this.getGUI().removeElement(window);
                        pokemon.transferPokemon();
                        PokemonHandler.API.getPlayerProfile().updateProfile();
                        PokemonHandler.API.getInventories().updateInventories();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                });
                return true;
            }).withParent(window);
            new ButtonElement<>(this.getGUI(), I18n.translateToLocal("gui.power_up.name"), buttonOffset / 2 + 1, buttonY + 15, scaleFactor * 50 - buttonOffset / 2, buttonHeight - 2, (button) -> {
                if (canPowerUp) {
                    PokemonHandler.addTask(() -> {
                        try {
                            pokemon.powerUp();
                            PokemonHandler.API.getPlayerProfile().updateProfile();
                            PokemonHandler.API.getInventories().updateInventories();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    });
                    return true;
                }
                return false;
            }).withParent(window).withColorScheme(canPowerUp ? Element.DEFAULT : PokemonGUI.THEME_DISABLED).setEnabled(canPowerUp);
            new ButtonElement<>(this.getGUI(), I18n.translateToLocal("gui.evolve.name"), buttonOffset / 2 + 1, buttonY + buttonHeight + 19, scaleFactor * 50 - buttonOffset / 2, buttonHeight - 2, (button) -> {
                if (canEvolve) {
                    PokemonHandler.addTask(() -> {
                        try {
                            EvolutionResult result = pokemon.evolve();
                            if (result.isSuccessful()) {
                                this.openPokemonView(resolution, result.getEvolvedPokemon());
                                PokemonHandler.API.getPlayerProfile().updateProfile();
                                PokemonHandler.API.getInventories().updateInventories();
                            }
                            this.getGUI().removeElement(window);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    });
                    return true;
                }
                return false;
            }).withParent(window).withColorScheme(canEvolve ? Element.DEFAULT : PokemonGUI.THEME_DISABLED).setEnabled(canEvolve);
            this.getGUI().addElement(window);
        } catch (Exception e) {
        }
    }
}
