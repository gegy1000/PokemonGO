package net.gegy1000.pokemon.client.gui;

import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass;
import POGOProtos.Networking.Responses.EncounterResponseOuterClass;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.ItemBag;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.settings.CatchOptions;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonMapHandler;
import net.gegy1000.pokemon.client.util.PokemonUtils;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
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

@SideOnly(Side.CLIENT)
public class CapturePokemonGUI extends PokemonGUI {
    private static final Set<ItemIdOuterClass.ItemId> USABLE_ITEMS = new HashSet<>();

    static {
        USABLE_ITEMS.add(ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY);
        USABLE_ITEMS.add(ItemIdOuterClass.ItemId.ITEM_POKE_BALL);
        USABLE_ITEMS.add(ItemIdOuterClass.ItemId.ITEM_GREAT_BALL);
        USABLE_ITEMS.add(ItemIdOuterClass.ItemId.ITEM_MASTER_BALL);
        USABLE_ITEMS.add(ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL);
    }

    private CatchablePokemon pokemon;
    private String pokemonName;
    private EncounterResult encounterResult;
    private String statusText;
    private boolean usingRazzbery;

    public CapturePokemonGUI(CatchablePokemon pokemon) {
        this.pokemon = pokemon;
        this.pokemonName = PokemonGUIHandler.getName(pokemon.getPokemonId());
        try {
            PokemonHandler.addTask(() -> {
                try {
                    this.encounterResult = this.pokemon.encounterPokemon();
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
                    PokemonHandler.addTask(() -> {
                        PokemonHandler.API.getInventories().updateInventories();
                        return null;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            AdvancedDynamicTexture texture = PokemonGUIHandler.getTexture(this.pokemon.getPokemonId());
            if (texture != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                texture.bind();
                int pokemonSize = captureSizeX / 2;
                this.drawTexturedModalRect(captureX + pokemonSize / 2, (float) (captureY + pokemonSize / 2 + Math.sin((this.mc.thePlayer.ticksExisted + partialTicks) * 0.15F) * 6.0F), 0.0F, 0.0F, 1.0F, 1.0F, pokemonSize, pokemonSize);
            }
            int statsX = 7;
            int statsY = 33;
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
            DecimalFormat shortDecimalFormat = new DecimalFormat("#.##", symbols);
            this.fontRendererObj.drawString(TextFormatting.BOLD + I18n.translateToLocal("gui.statistics.name"), statsX + 2, statsY - 10, LLibrary.CONFIG.getTextColor());
            if (this.encounterResult != null) {
                PokemonDataOuterClass.PokemonData pokemonData = this.encounterResult.getPokemonData();
                this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.cp_string.name", TextFormatting.BLUE + "" + pokemonData.getCp()), statsX + 2, statsY + 5, LLibrary.CONFIG.getTextColor());
                this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.hp_string.name", TextFormatting.GOLD + "" + pokemonData.getStamina(), pokemonData.getStaminaMax()), statsX + 2, statsY + 15, LLibrary.CONFIG.getTextColor());
                this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.weight_string.name", TextFormatting.DARK_BLUE + "" + shortDecimalFormat.format(pokemonData.getWeightKg())), statsX + 2, statsY + 25, LLibrary.CONFIG.getTextColor());
                this.fontRendererObj.drawString(I18n.translateToLocalFormatted("gui.iv.name", TextFormatting.GREEN + "" + (int) PokemonUtils.calculateIV(pokemonData) + "%"), statsX + 2, statsY + 35, LLibrary.CONFIG.getTextColor());
            } else {
                this.fontRendererObj.drawString("Loading...", statsX + 2, statsY + 5, LLibrary.CONFIG.getTextColor());
            }
            if (this.usingRazzbery) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(PokemonGUIHandler.getTexture(ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY));
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
            ItemBag bag = PokemonHandler.API.getInventories().getItemBag();
            int x = 0;
            int y = 0;
            for (Item item : bag.getItems()) {
                if (item.getCount() > 0 && USABLE_ITEMS.contains(item.getItemId())) {
                    int renderX = x * tileSize + tileOffsetX;
                    int renderY = y * tileSize + tileOffsetY;
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.mc.getTextureManager().bindTexture(PokemonGUIHandler.getTexture(item.getItemId()));
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
                if (item.getCount() > 0 && USABLE_ITEMS.contains(item.getItemId())) {
                    float renderX = x * tileSize + tileOffsetX;
                    float renderY = y * tileSize + tileOffsetY;
                    if (mouseX >= renderX && mouseX <= renderX + tileRenderSize && mouseY >= renderY && mouseY <= renderY + tileRenderSize) {
                        List<String> text = new LinkedList<>();
                        ItemIdOuterClass.ItemId itemId = item.getItemId();
                        text.add(TextFormatting.BLUE + I18n.translateToLocal("pokeitem." + itemId.name().replaceAll("ITEM_", "").toLowerCase(Locale.ENGLISH) + ".name"));
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
        if (this.encounterResult != null && this.encounterResult.getStatus() == EncounterResponseOuterClass.EncounterResponse.Status.ENCOUNTER_SUCCESS) {
            try {
                ScaledResolution resolution = new ScaledResolution(this.mc);
                int tileSize = resolution.getScaleFactor() * 21;
                int tileRenderSize = tileSize - 2;
                int tileOffsetX = this.width - (tileSize * 2) - 10;
                int tileOffsetY = 40;
                ItemBag bag = PokemonHandler.API.getInventories().getItemBag();
                int x = 0;
                int y = 0;
                for (Item item : bag.getItems()) {
                    ItemIdOuterClass.ItemId captureItem = item.getItemId();
                    if (item.getCount() > 0 && USABLE_ITEMS.contains(captureItem)) {
                        float renderX = x * tileSize + tileOffsetX;
                        float renderY = y * tileSize + tileOffsetY;
                        if (mouseX >= renderX && mouseX <= renderX + tileRenderSize && mouseY >= renderY && mouseY <= renderY + tileRenderSize) {
                            if (captureItem == ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY && !this.usingRazzbery) {
                                this.usingRazzbery = true;
                            } else {
                                Pokeball pokeball = null;
                                if (captureItem == ItemIdOuterClass.ItemId.ITEM_POKE_BALL) {
                                    pokeball = Pokeball.POKEBALL;
                                } else if (captureItem == ItemIdOuterClass.ItemId.ITEM_GREAT_BALL) {
                                    pokeball = Pokeball.GREATBALL;
                                } else if (captureItem == ItemIdOuterClass.ItemId.ITEM_MASTER_BALL) {
                                    pokeball = Pokeball.MASTERBALL;
                                } else if (captureItem == ItemIdOuterClass.ItemId.ITEM_ULTRA_BALL) {
                                    pokeball = Pokeball.ULTRABALL;
                                }
                                if (pokeball != null) {
                                    final Pokeball selectedPokeball = pokeball;
                                    PokemonHandler.addTask(() -> {
                                        try {
                                            CatchOptions catchOptions = new CatchOptions(PokemonHandler.API);
                                            catchOptions.maxRazzberries(this.usingRazzbery ? 1 : 0);
                                            catchOptions.maxPokeballs(1);
                                            catchOptions.usePokeball(selectedPokeball);
                                            Item capture = bag.getItem(captureItem);
                                            Item razzbery = bag.getItem(ItemIdOuterClass.ItemId.ITEM_RAZZ_BERRY);
                                            capture.setCount(capture.getCount() - 1);
                                            if (this.usingRazzbery) {
                                                razzbery.setCount(razzbery.getCount() - 1);
                                            }
                                            CatchResult result = this.pokemon.catchPokemon(catchOptions);
                                            PokemonHandler.addTask(() -> {
                                                PokemonHandler.API.getInventories().updateInventories();
                                                return null;
                                            });
                                            if (result.getStatus() == CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus.CATCH_ERROR) {
                                                if (this.usingRazzbery) {
                                                    razzbery.setCount(razzbery.getCount() + 1);
                                                }
                                                capture.setCount(capture.getCount() + 1);
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
                                                        PokemonMapHandler.removePokemon(this.pokemon);
                                                    } else {
                                                        this.removeElement(window);
                                                    }
                                                    return true;
                                                }).withParent(window).withColorScheme(THEME_WINDOW);
                                                this.addElement(window);
                                                PokemonHandler.API.getPlayerProfile().updateProfile();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    });
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
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
