package net.gegy1000.pokemon.client.gui.view;

import POGOProtos.Data.PokedexEntryOuterClass;
import POGOProtos.Enums.PokemonFamilyIdOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.inventory.CandyJar;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.ItemBag;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.inventory.Pokedex;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.util.PokeNames;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonSpriteHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
import net.ilexiconn.llibrary.client.gui.element.ListElement;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class InventoryViewHandler extends ViewHandler {
    private ListElement<PokemonViewGUI> inventoriesList;
    private InventoryGridElement<PokemonViewGUI> inventoryGrid;
    private InventoryType selectedInventoryType = InventoryType.POKEBANK;

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        try {
            Inventories inventories = PokemonHandler.GO.getInventories();
            if (this.selectedInventoryType == InventoryType.POKEDEX) {
                Pokedex pokedex = inventories.getPokedex();
                List<PokedexEntryOuterClass.PokedexEntry> caughtPokemon = this.getPokedexEntries(pokedex);
                this.fontRendererObj.drawString("You have " + caughtPokemon.size() + " Pokémon in your Pokédex.", 85, 23, LLibrary.CONFIG.getTextColor());
            } else if (this.selectedInventoryType == InventoryType.BACKPACK) {
                ItemBag bag = inventories.getItemBag();
                int count = bag.getItemsCount();
                this.fontRendererObj.drawString("You have " + count + "/" + PokemonHandler.GO.getPlayerProfile().getPlayerData().getMaxItemStorage() + " items in your Backpack.", 85, 23, LLibrary.CONFIG.getTextColor());
            } else if (this.selectedInventoryType == InventoryType.POKEBANK) {
                PokeBank pokebank = inventories.getPokebank();
                this.fontRendererObj.drawString("You have " + pokebank.getPokemons().size() + "/" + PokemonHandler.GO.getPlayerProfile().getPlayerData().getMaxPokemonStorage() + " Pokémon in your Pokébank.", 85, 23, LLibrary.CONFIG.getTextColor());
            } else if (this.selectedInventoryType == InventoryType.CANDY_JAR) {
                CandyJar candyJar = inventories.getCandyjar();
                Map<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> candies = this.getCandies(candyJar);
                this.fontRendererObj.drawString("You have " + candies.size() + "/" + PokemonFamilyIdOuterClass.PokemonFamilyId.values().length + " candies in your Candy Jar.", 85, 23, LLibrary.CONFIG.getTextColor());
            }
        } catch (Exception e) {
        }
    }

    private List<PokedexEntryOuterClass.PokedexEntry> getPokedexEntries(Pokedex pokedex) {
        List<PokedexEntryOuterClass.PokedexEntry> caughtPokemon = new LinkedList<>();
        for (PokemonIdOuterClass.PokemonId pokemon : PokemonIdOuterClass.PokemonId.values()) {
            PokedexEntryOuterClass.PokedexEntry entry = pokedex.getPokedexEntry(pokemon);
            if (entry != null) {
                caughtPokemon.add(entry);
            }
        }
        return caughtPokemon;
    }

    private Map<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> getCandies(CandyJar candyJar) {
        Map<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> candies = new HashMap<>();
        for (PokemonFamilyIdOuterClass.PokemonFamilyId family : PokemonFamilyIdOuterClass.PokemonFamilyId.values()) {
            int amount = candyJar.getCandies(family);
            if (amount > 0) {
                candies.put(family, amount);
            }
        }
        return candies;
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY) {

    }

    @Override
    public void initView() {
        if (this.inventoriesList == null || this.inventoryGrid == null) {
            new Thread(() -> {
                try {
                    PokemonHandler.GO.getInventories().updateInventories(true);
                } catch (Exception e) {
                }
            }).start();
            List<String> inventoryTypes = new ArrayList<>();
            for (InventoryType type : InventoryType.values()) {
                inventoryTypes.add(type.getName());
            }
            if (this.inventoriesList == null) {
                ElementHandler.INSTANCE.addElement(this.getGUI(), this.inventoriesList = new ListElement<>(this.getGUI(), 0.0F, 18.0F, 80, this.getGUI().height - 52, inventoryTypes, 20, list -> {
                    this.selectedInventoryType = InventoryType.values()[list.getSelectedIndex()];
                    return true;
                }));
                this.inventoriesList.init();
                this.inventoriesList.setSelectedIndex(0);
            }
            if (this.inventoryGrid == null) {
                ScaledResolution resolution = new ScaledResolution(this.mc);
                int tileSize = resolution.getScaleFactor() * 21;
                int tilesX = 8;
                int tilesY = 4;
                ElementHandler.INSTANCE.addElement(this.getGUI(), this.inventoryGrid = new InventoryGridElement<>(this.getGUI(), (this.getGUI().width - 80 - tileSize * 8) / 2 + 80, 35, tilesX * tileSize, tilesY * tileSize, tilesX, tileSize, (slotRenderer) -> {
                    try {
                        Inventories inventories = PokemonHandler.GO.getInventories();
                        int amount = 0;
                        int tileRenderSize = slotRenderer.getGrid().getRenderTileSize();
                        Function<InventoryGridElement.Slot, Void> renderFunction = null;
                        Function<InventoryGridElement.Slot, List<String>> hoverFunction = null;
                        if (this.selectedInventoryType == InventoryType.POKEDEX) {
                            Pokedex pokedex = inventories.getPokedex();
                            final List<PokedexEntryOuterClass.PokedexEntry> entries = this.getPokedexEntries(pokedex);
                            amount = entries.size();
                            renderFunction = (slot) -> {
                                PokedexEntryOuterClass.PokedexEntry pokemon = entries.get(slot.getIndex());
                                AdvancedDynamicTexture texture = PokemonSpriteHandler.get(pokemon.getPokemonId());
                                if (texture != null) {
                                    texture.bind();
                                    this.drawTexturedModalRect(slot.getX(), slot.getY(), 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize, tileRenderSize);
                                }
                                return null;
                            };
                            hoverFunction = (slot) -> {
                                List<String> text = new ArrayList<>();
                                text.add(TextFormatting.BLUE + PokeNames.getDisplayName(entries.get(slot.getIndex()).getPokemonId().getNumber(), Locale.ENGLISH));
                                return text;
                            };
                        } else if (this.selectedInventoryType == InventoryType.BACKPACK) {
                            ItemBag bag = inventories.getItemBag();
                            final Collection<Item> items = bag.getItems();
                            amount = items.size();
                            renderFunction = (slot) -> {
                                int index = 0;
                                for (Item item : items) {
                                    if (index == slot.getIndex()) {
                                        this.mc.getTextureManager().bindTexture(PokemonSpriteHandler.get(item.getItemId()));
                                        this.drawTexturedModalRect(slot.getX() + 3, slot.getY() + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
                                        this.fontRendererObj.drawString("x" + item.getCount(), (int) slot.getX() + 1, (int) slot.getY() + 3 + tileRenderSize - 12, LLibrary.CONFIG.getTextColor(), false);
                                        return null;
                                    }
                                    index++;
                                }
                                return null;
                            };
                            hoverFunction = (slot) -> {
                                List<String> text = new ArrayList<>();
                                int index = 0;
                                for (Item item : items) {
                                    if (index == slot.getIndex()) {
                                        text.add(TextFormatting.BLUE + I18n.translateToLocal("item." + item.getItemId().name().replaceAll("ITEM_", "").toLowerCase(Locale.ENGLISH) + ".name"));
                                        text.add(TextFormatting.GREEN + "x" + item.getCount());
                                        return text;
                                    }
                                    index++;
                                }
                                return text;
                            };
                        } else if (this.selectedInventoryType == InventoryType.POKEBANK) {
                            PokeBank pokebank = inventories.getPokebank();
                            List<Pokemon> pokemons = pokebank.getPokemons();
                            amount = pokemons.size();
                            renderFunction = (slot) -> {
                                Pokemon pokemon = pokemons.get(slot.getIndex());
                                AdvancedDynamicTexture texture = PokemonSpriteHandler.get(pokemon.getPokemonId());
                                if (texture != null) {
                                    texture.bind();
                                    this.drawTexturedModalRect(slot.getX(), slot.getY(), 0, 0, tileRenderSize, tileRenderSize, tileRenderSize, tileRenderSize, 1.0, 1.0);
                                }
                                this.fontRendererObj.drawString("CP: " + pokemon.getCp(), (int) slot.getX() + 1, (int) slot.getY() + tileRenderSize - 9, LLibrary.CONFIG.getTextColor());
                                return null;
                            };
                            hoverFunction = (slot) -> {
                                List<String> text = new ArrayList<>();
                                Pokemon pokemon = pokemons.get(slot.getIndex());
                                text.add(TextFormatting.BLUE + PokeNames.getDisplayName(pokemon.getPokemonId().getNumber(), Locale.ENGLISH));
                                text.add(TextFormatting.GREEN + "CP: " + pokemon.getCp());
                                return text;
                            };
                        } else if (this.selectedInventoryType == InventoryType.CANDY_JAR) {
                            CandyJar candyJar = inventories.getCandyjar();
                            Map<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> candies = this.getCandies(candyJar);
                            amount = candies.size();
                            renderFunction = (slot) -> {
                                int index = 0;
                                for (Map.Entry<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> candy : candies.entrySet()) {
                                    if (index == slot.getIndex()) {
                                        this.mc.getTextureManager().bindTexture(PokemonSpriteHandler.get(candy.getKey()));
                                        this.drawTexturedModalRect(slot.getX() + 3, slot.getY() + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
                                        this.fontRendererObj.drawString("x" + candy.getValue(), (int) slot.getX() + 1, (int) slot.getY() + 3 + tileRenderSize - 12, LLibrary.CONFIG.getTextColor(), false);
                                        return null;
                                    }
                                    index++;
                                }
                                return null;
                            };
                            hoverFunction = (slot) -> {
                                List<String> text = new ArrayList<>();
                                int index = 0;
                                for (Map.Entry<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> candy : candies.entrySet()) {
                                    if (index == slot.getIndex()) {
                                        text.add(TextFormatting.BLUE + I18n.translateToLocalFormatted("item.candy.name", PokeNames.getDisplayName(candy.getKey().getNumber(), Locale.ENGLISH)));
                                        text.add(TextFormatting.GREEN + "x" + candy.getValue());
                                        return text;
                                    }
                                    index++;
                                }
                                return text;
                            };
                        }
                        if (renderFunction != null) {
                            slotRenderer.draw(renderFunction, hoverFunction, amount);
                        }
                    } catch (Exception e) {
                    }
                    return null;
                }));
                this.inventoryGrid.init();
            }
        }
    }

    @Override
    public void cleanupView() {
        if (this.inventoriesList != null) {
            ElementHandler.INSTANCE.removeElement(this.getGUI(), this.inventoriesList);
            this.inventoriesList = null;
        }
        if (this.inventoryGrid != null) {
            ElementHandler.INSTANCE.removeElement(this.getGUI(), this.inventoryGrid);
            this.inventoryGrid = null;
        }
        this.selectedInventoryType = InventoryType.POKEBANK;
    }

    private enum InventoryType {
        POKEBANK("Pokébank"),
        POKEDEX("Pokédex"),
        BACKPACK("Backpack"),
        HATCHERY("Hatchery"),
        CANDY_JAR("Candy Jar"),
        INCUBATORS("Incubators");

        private String name;

        InventoryType(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
