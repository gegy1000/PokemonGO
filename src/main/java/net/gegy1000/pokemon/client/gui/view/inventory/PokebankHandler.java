package net.gegy1000.pokemon.client.gui.view.inventory;

import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Networking.Requests.Messages.NicknamePokemonMessageOuterClass;
import POGOProtos.Networking.Requests.Messages.ReleasePokemonMessageOuterClass;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import POGOProtos.Networking.Responses.NicknamePokemonResponseOuterClass;
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;
import POGOProtos.Networking.Responses.UpgradePokemonResponseOuterClass;
import com.google.protobuf.ByteString;
import com.pokegoapi.api.inventory.CandyJar;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.main.AsyncServerRequest;
import com.pokegoapi.util.AsyncHelper;
import com.pokegoapi.util.PokeNames;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.PokemonGUI;
import net.gegy1000.pokemon.client.gui.element.DrawableElement;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonSpriteHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.Element;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
import net.ilexiconn.llibrary.client.gui.element.InputElement;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
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
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.pokebank.name", inventories.getPokebank().getPokemons().size(), PokemonHandler.GO.getPlayerProfile().getPlayerData().getMaxPokemonStorage()), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        PokeBank pokebank = inventories.getPokebank();
        List<Pokemon> pokemons = pokebank.getPokemons();
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        slotHandler.draw((slot) -> {
            Pokemon pokemon = pokemons.get(slot.getIndex());
            AdvancedDynamicTexture texture = PokemonSpriteHandler.get(pokemon.getPokemonId());
            if (texture != null) {
                texture.bind();
                this.drawTexturedModalRect(slot.getX(), slot.getY(), 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize, tileRenderSize);
            }
            this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()), (int) slot.getX() + 1, (int) slot.getY() + tileRenderSize - 9, LLibrary.CONFIG.getTextColor());
            return null;
        }, (slot) -> {
            List<String> text = new ArrayList<>();
            Pokemon pokemon = pokemons.get(slot.getIndex());
            text.add(TextFormatting.BLUE + (pokemon.getNickname() != null && pokemon.getNickname().length() > 0 ? pokemon.getNickname() : PokeNames.getDisplayName(pokemon.getPokemonId().getNumber(), Locale.ENGLISH)));
            text.add(TextFormatting.GREEN + I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()));
            return text;
        }, pokemons.size());
    }

    @Override
    public void click(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        PokeBank pokebank = inventories.getPokebank();
        List<Pokemon> pokemons = pokebank.getPokemons();
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        slotHandler.click(slot -> {
            try {
                Pokemon pokemon = pokemons.get(slot.getIndex());
                String displayName = PokeNames.getDisplayName(pokemon.getPokemonId().getNumber(), Locale.ENGLISH);
                int scaleFactor = resolution.getScaleFactor();
                WindowElement<PokemonViewGUI> window = new WindowElement<>(this.getGUI(), I18n.translateToLocalFormatted("gui.pokemon_info.name", displayName), scaleFactor * 100, scaleFactor * 100, true);
                int centerX = scaleFactor * 50;
                int iconSize = scaleFactor * 35;
                int nameWidth = scaleFactor * 40;
                String nickname = pokemon.getNickname();
                int buttonHeight = scaleFactor * 6;
                int buttonOffset = scaleFactor * 20;
                int buttonY = scaleFactor * (100 - 33);
                CandyJar candyJar = PokemonHandler.GO.getInventories().getCandyjar();
                boolean canEvolve = candyJar.getCandies(pokemon.getPokemonFamily()) >= pokemon.getCandiesToEvolve() && pokemon.getCandiesToEvolve() > 0;
                boolean canPowerUpCandy = candyJar.getCandies(pokemon.getPokemonFamily()) >= pokemon.getCandyCostsForPowerup();
                boolean canPowerUpStardust = PokemonHandler.GO.getPlayerProfile().getCurrencies().get(PlayerProfile.Currency.STARDUST) >= pokemon.getStardustCostsForPowerup();
                boolean canPowerUp = canPowerUpCandy && canPowerUpStardust;
                int textColor = LLibrary.CONFIG.getTextColor();
                int fontHeight = (int) (this.fontRenderer.FONT_HEIGHT / 2.5F * scaleFactor);
                new InputElement<>(this.getGUI(), nickname != null && nickname.length() > 0 ? nickname : displayName, centerX - nameWidth / 2, iconSize + 26, nameWidth, (input) -> new Thread(() -> {
                    try {
                        if (!input.getText().equals(nickname)) {
                            String newNickname = input.getText().equals(displayName) ? "" : input.getText();
                            NicknamePokemonMessageOuterClass.NicknamePokemonMessage message = NicknamePokemonMessageOuterClass.NicknamePokemonMessage.newBuilder().setPokemonId(pokemon.getId()).setNickname(newNickname).build();
                            AsyncServerRequest request = new AsyncServerRequest(RequestTypeOuterClass.RequestType.NICKNAME_POKEMON, message);
                            ByteString data = AsyncHelper.toBlocking(PokemonHandler.GO.getRequestHandler().sendAsyncServerRequests(request));
                            NicknamePokemonResponseOuterClass.NicknamePokemonResponse response = NicknamePokemonResponseOuterClass.NicknamePokemonResponse.parseFrom(data);
                            if (response.getResult() == NicknamePokemonResponseOuterClass.NicknamePokemonResponse.Result.SUCCESS) {
                                pokemon.setProto(pokemon.getProto().toBuilder().mergeFrom(PokemonDataOuterClass.PokemonData.newBuilder().setNickname(newNickname).build()).build());
                            } else {
                                input.clearText();
                                input.writeText(pokemon.getNickname() == null || pokemon.getNickname().length() == 0 ? displayName : pokemon.getNickname());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start()).withParent(window);
                new DrawableElement<>(this.getGUI(), 0.0F, 14.0F, scaleFactor * 100, scaleFactor * 100 - 14, (mousePosition) -> {
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
                    symbols.setDecimalSeparator('.');
                    symbols.setGroupingSeparator(',');
                    DecimalFormat shortDecimalFormat = new DecimalFormat("#.##", symbols);
                    int x = centerX - iconSize / 2;
                    int y = 10;
                    this.drawRectangle(x, y, iconSize, iconSize, LLibrary.CONFIG.getSecondaryColor());
                    AdvancedDynamicTexture texture = PokemonSpriteHandler.get(pokemon.getPokemonId());
                    if (texture != null) {
                        texture.bind();
                        this.drawTexturedModalRect(x, y, 0.0F, 0.0F, 1.0F, 1.0F, iconSize, iconSize);
                    }
                    GlStateManager.disableTexture2D();
                    this.drawRectangle(buttonOffset / 2, buttonY, scaleFactor * 100 - buttonOffset, buttonHeight, LLibrary.CONFIG.getSecondaryColor());
                    this.drawRectangle(buttonOffset / 2, buttonY + buttonHeight + 4, scaleFactor * 100 - buttonOffset, buttonHeight, LLibrary.CONFIG.getSecondaryColor());
                    int healthWidth = scaleFactor * 30;
                    this.drawRectangle(centerX - healthWidth / 2 - 1, y + iconSize + 16, healthWidth + 2, 11, LLibrary.CONFIG.getSecondaryColor());
                    this.drawRectangle(centerX - healthWidth / 2, y + iconSize + 17, (pokemon.getStamina() * healthWidth) / pokemon.getMaxStamina(), 9, 0xFF4AD33C);
                    String hp = I18n.translateToLocalFormatted("gui.hp.name", pokemon.getStamina(), pokemon.getMaxStamina());
                    this.fontRenderer.drawString(hp, centerX - this.fontRenderer.getStringWidth(hp) / 2, y + iconSize + 18, textColor);
                    String cp = I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp());
                    this.fontRenderer.drawString(cp, centerX - this.fontRenderer.getStringWidth(cp) / 2, y + iconSize + 29, textColor);
                    String weight = I18n.translateToLocalFormatted("gui.weight.name", shortDecimalFormat.format(pokemon.getWeightKg()));
                    this.fontRenderer.drawString(weight, centerX - this.fontRenderer.getStringWidth(weight) / 2, y + iconSize + 39, textColor);
                    this.fontRenderer.drawString(String.valueOf(canPowerUpCandy ? "" : TextFormatting.RED) + pokemon.getCandyCostsForPowerup(), buttonOffset / 2 + scaleFactor * 50, buttonY + buttonHeight / 2 - fontHeight / 2, textColor);
                    this.fontRenderer.drawString(String.valueOf(canPowerUpStardust ? "" : TextFormatting.RED) + pokemon.getStardustCostsForPowerup(), (int) (buttonOffset * 1.3) + scaleFactor * 50, buttonY + buttonHeight / 2 - fontHeight / 2, textColor);
                    if (pokemon.getCandiesToEvolve() != 0) {
                        this.fontRenderer.drawString(String.valueOf(canEvolve ? "" : TextFormatting.RED) + pokemon.getCandiesToEvolve(), buttonOffset + scaleFactor * 50, buttonY + resolution.getScaleFactor() * 8 + buttonHeight / 2 - fontHeight / 2, textColor);
                    }
                    this.getGUI().mc.getTextureManager().bindTexture(PokemonSpriteHandler.get(pokemon.getPokemonFamily()));
                    int costIconSize = (int) (scaleFactor * 5.5);
                    if (pokemon.getCandiesToEvolve() != 0) {
                        this.drawTexturedModalRect(buttonOffset + scaleFactor * 43, buttonY + resolution.getScaleFactor() * 8 + 1, 0.0F, 0.0F, 1.0F, 1.0F, costIconSize, costIconSize);
                    }
                    this.drawTexturedModalRect(buttonOffset / 2 + scaleFactor * 43, buttonY + 1, 0.0F, 0.0F, 1.0F, 1.0F, costIconSize, costIconSize);
                    this.getGUI().mc.getTextureManager().bindTexture(PokemonGUI.STARDUST_TEXTURE);
                    this.drawTexturedModalRect((int) (buttonOffset * 1.3) + scaleFactor * 45, buttonY + 1, 0.0F, 0.0F, 1.0F, 1.0F, costIconSize, costIconSize);
                    return null;
                }).withParent(window);
                new ButtonElement<>(this.getGUI(), I18n.translateToLocal("gui.release.name"), 1.0F, scaleFactor * 100 - 21, scaleFactor * 100 - 2, 20, (button) -> {
                    new Thread(() -> {
                        try {
                            ReleasePokemonMessageOuterClass.ReleasePokemonMessage message = ReleasePokemonMessageOuterClass.ReleasePokemonMessage.newBuilder().setPokemonId(pokemon.getId()).build();
                            AsyncServerRequest request = new AsyncServerRequest(RequestTypeOuterClass.RequestType.RELEASE_POKEMON, message);
                            ByteString byteString = AsyncHelper.toBlocking(PokemonHandler.GO.getRequestHandler().sendAsyncServerRequests(request));
                            ReleasePokemonResponseOuterClass.ReleasePokemonResponse response = ReleasePokemonResponseOuterClass.ReleasePokemonResponse.parseFrom(byteString);
                            if (response.getResult() == ReleasePokemonResponseOuterClass.ReleasePokemonResponse.Result.SUCCESS) {
                                pokebank.removePokemon(pokemon);
                                ElementHandler.INSTANCE.removeElement(this.getGUI(), window);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    return true;
                }).withParent(window);
                new ButtonElement<>(this.getGUI(), I18n.translateToLocal("gui.power_up.name"), buttonOffset / 2 + 1, buttonY + 15, scaleFactor * 50 - buttonOffset / 2, buttonHeight - 2, (button) -> {
                    if (canPowerUp) {
                        new Thread(() -> {
                            try {
                                pokemon.powerUp();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                        return true;
                    }
                    return false;
                }).withParent(window).withColorScheme(canPowerUp ? Element.DEFAULT : PokemonGUI.THEME_DISABLED).setEnabled(canPowerUp);
                new ButtonElement<>(this.getGUI(), I18n.translateToLocal("gui.evolve.name"), buttonOffset / 2 + 1, buttonY + buttonHeight + 19, scaleFactor * 50 - buttonOffset / 2, buttonHeight - 2, (button) -> {
                    if (canEvolve) {
                        new Thread(() -> {
                            try {
                                pokemon.evolve();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                        return true;
                    }
                    return false;
                }).withParent(window).withColorScheme(canEvolve ? Element.DEFAULT : PokemonGUI.THEME_DISABLED).setEnabled(canEvolve);
                ElementHandler.INSTANCE.addElement(this.getGUI(), window);
            } catch (Exception e) {
            }
            return true;
        }, pokemons.size());
    }
}
