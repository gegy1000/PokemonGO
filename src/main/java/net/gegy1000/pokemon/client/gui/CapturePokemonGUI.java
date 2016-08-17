package net.gegy1000.pokemon.client.gui;

import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Responses.EncounterResponseOuterClass;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.ItemBag;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.util.PokeNames;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonSpriteHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
import net.ilexiconn.llibrary.client.gui.element.LabelElement;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SideOnly(Side.CLIENT)
public class CapturePokemonGUI extends PokemonGUI {
    private CatchablePokemon pokemon;
    private String pokemonName;
    private EncounterResult encounterResult;
    private String statusText;
    private Set<ItemIdOuterClass.ItemId> useableItems;
    private boolean usingRazzbery;

    public CapturePokemonGUI(CatchablePokemon pokemon) {
        this.pokemon = pokemon;
        this.pokemonName = PokeNames.getDisplayName(pokemon.getPokemonId().getNumber(), Locale.ENGLISH);
        try {
            this.encounterResult = this.pokemon.encounterPokemon();
            new Thread(() -> {
                try {
                    PokemonHandler.GO.getInventories().updateInventories(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        EncounterResponseOuterClass.EncounterResponse.Status status = this.encounterResult.getStatus();
        switch (status) {
            case ENCOUNTER_ALREADY_HAPPENED:
                this.statusText = I18n.translateToLocal("gui.already_encountered.name");
                break;
            case ENCOUNTER_CLOSED:
                this.statusText = I18n.translateToLocal("gui.encounter_closed.name");
                break;
            case ENCOUNTER_ERROR:
                this.statusText = I18n.translateToLocal("gui.unexpected_error.name");
                break;
            case ENCOUNTER_NOT_FOUND:
                this.statusText = I18n.translateToLocal("gui.encounter_not_found.name");
                break;
            case ENCOUNTER_NOT_IN_RANGE:
                this.statusText = I18n.translateToLocal("gui.far.name");
                break;
            case ENCOUNTER_POKEMON_FLED:
                this.statusText = I18n.translateToLocal("gui.pokemon_fled.name");
                break;
            case POKEMON_INVENTORY_FULL:
                this.statusText = I18n.translateToLocal("gui.inventory_full.name");
                break;
            case ENCOUNTER_SUCCESS:
                this.statusText = I18n.translateToLocal("gui.catchable.name");
        }
        this.statusText = "* " + this.statusText + " *";
        this.useableItems = new HashSet<>();
        for (ItemIdOuterClass.ItemId item : this.encounterResult.getCaptureProbability().getPokeballTypeList()) {
            this.useableItems.add(item);
        }
        this.useableItems.add(ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY);
    }

    @Override
    public void initElements() {
    }

    @Override
    public void drawScreen(float mouseX, float mouseY, float partialTicks) {
        try {
            ScaledResolution resolution = new ScaledResolution(this.mc);
            this.drawRectangle(0, this.height - 18.0F, this.width, 18.0F, LLibrary.CONFIG.getPrimaryColor());
            this.drawRectangle(0, 0, this.width, 18.0F, LLibrary.CONFIG.getPrimaryColor());
            String titleString = I18n.translateToLocalFormatted("gui.catching_pokemon.name", this.pokemonName);
            this.fontRendererObj.drawString(titleString, this.width / 2 - this.fontRendererObj.getStringWidth(titleString) / 2, 6, LLibrary.CONFIG.getTextColor(), false);
            int captureSizeX = resolution.getScaleFactor() * 80;
            int captureSizeY = resolution.getScaleFactor() * 90;
            int captureX = this.width / 2 - (captureSizeX / 2);
            int captureY = this.height / 2 - (captureSizeY / 2);
            this.drawRectangle(captureX, captureY, captureSizeX, captureSizeY, LLibrary.CONFIG.getSecondaryColor());
            AdvancedDynamicTexture texture = PokemonSpriteHandler.get(this.pokemon.getPokemonId());
            if (texture != null) {
                texture.bind();
                int pokemonSize = captureSizeX / 2;
                this.drawTexturedModalRect(captureX + pokemonSize / 2, (float) (captureY + pokemonSize / 2 + Math.sin((this.mc.thePlayer.ticksExisted + partialTicks) * 0.15F) * 6.0F), 0.0F, 0.0F, 1.0F, 1.0F, pokemonSize, pokemonSize);
            }
            PokemonDataOuterClass.PokemonData pokemonData = this.encounterResult.getPokemonData();
            int statsX = 7;
            int statsY = 33;
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
            DecimalFormat shortDecimalFormat = new DecimalFormat("#.##", symbols);
            this.fontRendererObj.drawString(TextFormatting.BOLD + I18n.translateToLocal("gui.statistics.name"), statsX + 2, statsY - 10, LLibrary.CONFIG.getTextColor());
            this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.cp_string.name", TextFormatting.BLUE + "" + pokemonData.getCp()), statsX + 2, statsY + 5, LLibrary.CONFIG.getTextColor());
            this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.hp_string.name", TextFormatting.GOLD + "" + pokemonData.getStamina(), pokemonData.getStaminaMax()), statsX + 2, statsY + 15, LLibrary.CONFIG.getTextColor());
            this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.weight_string.name", TextFormatting.DARK_BLUE + "" + shortDecimalFormat.format(pokemonData.getWeightKg())), statsX + 2, statsY + 25, LLibrary.CONFIG.getTextColor());
            long despawn = this.pokemon.getExpirationTimestampMs() - PokemonHandler.GO.currentTimeMillis();
            if (despawn > 0) {
                this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.despawns.name", TimeUnit.MILLISECONDS.toHours(despawn), TimeUnit.MILLISECONDS.toMinutes(despawn) % TimeUnit.HOURS.toMinutes(1), TimeUnit.MILLISECONDS.toSeconds(despawn) % TimeUnit.MINUTES.toSeconds(1)), 3, this.height - 13, LLibrary.CONFIG.getTextColor());
            } else {
                this.statusText = "* " + I18n.translateToLocal("gui.expired.name") + " *";
            }
            if (this.usingRazzbery) {
                this.mc.getTextureManager().bindTexture(PokemonSpriteHandler.get(ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY));
                this.drawTexturedModalRect(0.0F, this.height - 50.0F, 0.0F, 0.0F, 1.0F, 1.0F, 32.0F, 32.0F);
            }
            this.fontRendererObj.drawString(this.statusText, this.width / 2 - this.fontRendererObj.getStringWidth(this.statusText) / 2, this.height - 12, LLibrary.CONFIG.getTextColor(), false);
            int tileSize = resolution.getScaleFactor() * 21;
            int tileRenderSize = tileSize - 2;
            int tileOffsetX = this.width - (tileSize * 2) - 10;
            int tileOffsetY = 40;
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 3; y++) {
                    this.drawRectangle(x * tileSize + tileOffsetX, y * tileSize + tileOffsetY, tileRenderSize, tileRenderSize, LLibrary.CONFIG.getSecondaryColor());
                }
            }
            this.fontRendererObj.drawString(I18n.translateToLocal("gui.useable_items.name"), tileOffsetX, tileOffsetY - 10, LLibrary.CONFIG.getTextColor());
            ItemBag bag = PokemonHandler.GO.getInventories().getItemBag();
            int x = 0;
            int y = 0;
            for (Item item : bag.getItems()) {
                if (this.useableItems.contains(item.getItemId())) {
                    int renderX = x * tileSize + tileOffsetX;
                    int renderY = y * tileSize + tileOffsetY;
                    this.mc.getTextureManager().bindTexture(PokemonSpriteHandler.get(item.getItemId()));
                    this.drawTexturedModalRect(renderX + 3, renderY + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
                    this.fontRendererObj.drawString("x" + item.getCount(), renderX + 2, renderY + tileRenderSize - 9, LLibrary.CONFIG.getTextColor(), false);
                    x++;
                    if (x >= 2) {
                        x = 0;
                        y++;
                    }
                    if (y >= 3) {
                        break;
                    }
                }
            }
            x = 0;
            y = 0;
            for (Item item : bag.getItems()) {
                if (this.useableItems.contains(item.getItemId())) {
                    float renderX = x * tileSize + tileOffsetX;
                    float renderY = y * tileSize + tileOffsetY;
                    if (mouseX >= renderX && mouseX <= renderX + tileRenderSize && mouseY >= renderY && mouseY <= renderY + tileRenderSize) {
                        List<String> text = new LinkedList<>();
                        ItemIdOuterClass.ItemId itemId = item.getItemId();
                        text.add(TextFormatting.BLUE + I18n.translateToLocal("item." + itemId.name().replaceAll("ITEM_", "").toLowerCase(Locale.ENGLISH) + ".name"));
                        text.add(TextFormatting.GREEN + "x" + item.getCount());
                        this.drawHoveringText(text, (int) mouseX, (int) mouseY);
                        break;
                    }
                    x++;
                    if (x >= 2) {
                        x = 0;
                        y++;
                    }
                    if (y >= 3) {
                        break;
                    }
                }
            }
            GlStateManager.disableLighting();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        try {
            ScaledResolution resolution = new ScaledResolution(this.mc);
            int tileSize = resolution.getScaleFactor() * 21;
            int tileRenderSize = tileSize - 2;
            int tileOffsetX = this.width - (tileSize * 2) - 10;
            int tileOffsetY = 40;
            ItemBag bag = PokemonHandler.GO.getInventories().getItemBag();
            int x = 0;
            int y = 0;
            for (Item item : bag.getItems()) {
                ItemIdOuterClass.ItemId itemId = item.getItemId();
                if (this.useableItems.contains(itemId)) {
                    float renderX = x * tileSize + tileOffsetX;
                    float renderY = y * tileSize + tileOffsetY;
                    if (mouseX >= renderX && mouseX <= renderX + tileRenderSize && mouseY >= renderY && mouseY <= renderY + tileRenderSize) {
                        if (itemId == ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY && !this.usingRazzbery) {
                            this.usingRazzbery = true;
                        } else {
                            Pokeball pokeball = null;
                            if (itemId == ItemIdOuterClass.ItemId.ITEM_POKE_BALL) {
                                pokeball = Pokeball.POKEBALL;
                            } else if (itemId == ItemIdOuterClass.ItemId.ITEM_GREAT_BALL) {
                                pokeball = Pokeball.GREATBALL;
                            } else if (itemId == ItemIdOuterClass.ItemId.ITEM_MASTER_BALL) {
                                pokeball = Pokeball.MASTERBALL;
                            } else if (itemId == ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL) {
                                pokeball = Pokeball.ULTRABALL;
                            }
                            if (pokeball != null) {
                                final Pokeball finalPokeball = pokeball;
                                new Thread(() -> {
                                    try {
                                        CatchResult result = this.pokemon.catchPokemon(finalPokeball, 1, this.usingRazzbery ? 1 : 0);
                                        if (!result.isFailed()) {
                                            Item ball = bag.getItem(itemId);
                                            ball.setCount(ball.getCount() - 1);
                                            if (this.usingRazzbery) {
                                                Item razzbery = bag.getItem(ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY);
                                                razzbery.setCount(razzbery.getCount() - 1);
                                            }
                                            PokemonHandler.GO.getInventories().updateInventories(false);
                                        }
                                        this.usingRazzbery = false;
                                        boolean close = false;
                                        String statusWindowTitle = null;
                                        String statusWindowMessage = null;
                                        switch (result.getStatus()) {
                                            case CATCH_ERROR:
                                                statusWindowTitle = I18n.translateToLocal("gui.failure.name");
                                                statusWindowMessage = I18n.translateToLocal("gui.unexpected_error.name");
                                                close = true;
                                                break;
                                            case CATCH_ESCAPE:
                                                statusWindowTitle = I18n.translateToLocal("gui.failure.name");
                                                statusWindowMessage = I18n.translateToLocal("gui.escaped.name");
                                                close = true;
                                                break;
                                            case CATCH_FLEE:
                                                statusWindowTitle = I18n.translateToLocal("gui.failure.name");
                                                statusWindowMessage = I18n.translateToLocal("gui.fled.name");
                                                close = true;
                                                break;
                                            case CATCH_MISSED:
                                                statusWindowTitle = I18n.translateToLocal("gui.failure.name");
                                                statusWindowMessage = I18n.translateToLocal("gui.missed.name");
                                                break;
                                            case CATCH_SUCCESS:
                                                statusWindowTitle = I18n.translateToLocal("gui.success.name");
                                                statusWindowMessage = I18n.translateToLocalFormatted("gui.caught_pokemon.name", this.pokemonName);
                                                close = true;
                                        }
                                        if (statusWindowMessage != null) {
                                            int windowWidth = this.fontRendererObj.getStringWidth(statusWindowMessage) + 4;
                                            WindowElement<CapturePokemonGUI> window = new WindowElement<>(this, statusWindowTitle, windowWidth, 45, false);
                                            new LabelElement<>(this, statusWindowMessage, 2, 18).withParent(window);
                                            boolean finalClose = close;
                                            new ButtonElement<>(this, I18n.translateToLocal("gui.okay.name"), 1, 29, windowWidth - 2, 15, (button) -> {
                                                if (finalClose) {
                                                    this.mc.displayGuiScreen(null);
                                                } else {
                                                    ElementHandler.INSTANCE.removeElement(this, window);
                                                }
                                                return true;
                                            }).withParent(window).withColorScheme(THEME_WINDOW);
                                            ElementHandler.INSTANCE.addElement(this, window);
                                            PokemonHandler.GO.getPlayerProfile().updateProfile();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                            }
                        }
                        break;
                    }
                    x++;
                    if (x >= 2) {
                        x = 0;
                        y++;
                    }
                    if (y >= 3) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
