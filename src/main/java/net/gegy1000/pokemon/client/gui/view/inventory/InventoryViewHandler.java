package net.gegy1000.pokemon.client.gui.view.inventory;

import com.pokegoapi.api.inventory.Inventories;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.gegy1000.pokemon.client.gui.view.ViewHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
import net.ilexiconn.llibrary.client.gui.element.ListElement;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class InventoryViewHandler extends ViewHandler {
    private ListElement<PokemonViewGUI> inventoriesList;
    private InventoryGridElement<PokemonViewGUI> inventoryGrid;
    private InventoryType selectedInventoryType = null;

    public InventoryViewHandler(PokemonViewGUI gui) {
        super(gui);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        try {
            Inventories inventories = PokemonHandler.API.getInventories();
            this.selectedInventoryType.getHandler().render(inventories, mouseX, mouseY, partialTicks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(float mouseX, float mouseY) {

    }

    @Override
    public void initView() {
        if (this.selectedInventoryType == null) {
            this.selectedInventoryType = InventoryType.POKEBANK;
            this.selectedInventoryType.getHandler().init(this.getGUI(), this);
        }
        if (this.inventoriesList == null || this.inventoryGrid == null) {
            new Thread(() -> {
                try {
                    PokemonHandler.API.getInventories().updateInventories(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            List<String> inventoryTypes = new ArrayList<>();
            for (InventoryType type : InventoryType.values()) {
                inventoryTypes.add(type.getName());
            }
            if (this.inventoriesList == null) {
                ElementHandler.INSTANCE.addElement(this.getGUI(), this.inventoriesList = new ListElement<>(this.getGUI(), 0.0F, 18.0F, 80, this.getGUI().height - 52, inventoryTypes, 20, list -> {
                    this.selectedInventoryType = InventoryType.values()[list.getSelectedIndex()];
                    this.selectedInventoryType.getHandler().init(this.getGUI(), this);
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
                ElementHandler.INSTANCE.addElement(this.getGUI(), this.inventoryGrid = new InventoryGridElement<>(this.getGUI(), (this.getGUI().width - 80 - tileSize * 8) / 2 + 80, 35, tilesX * tileSize, tilesY * tileSize, tilesX, tileSize, (slotHandler) -> {
                    try {
                        Inventories inventories = PokemonHandler.API.getInventories();
                        this.selectedInventoryType.getHandler().renderSlots(inventories, slotHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }, slotHandler -> {
                    try {
                        Inventories inventories = PokemonHandler.API.getInventories();
                        this.selectedInventoryType.getHandler().click(inventories, slotHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
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
        POKEBANK("pokebank", new PokebankHandler()),
        POKEDEX("pokedex", new PokedexHandler()),
        BACKPACK("backpack", new BackpackHandler()),
        HATCHERY("hatchery", new HatcheryHandler()),
        CANDY_JAR("candy_jar", new CandyJarHandler()),
        INCUBATORS("incubators", new IncubatorsHandler());

        private String name;
        private InventoryHandler handler;

        InventoryType(String name, InventoryHandler handler) {
            this.name = "inventory." + name + ".name";
            this.handler = handler;
        }

        public String getName() {
            return I18n.translateToLocal(this.name);
        }

        public InventoryHandler getHandler() {
            return this.handler;
        }
    }
}
