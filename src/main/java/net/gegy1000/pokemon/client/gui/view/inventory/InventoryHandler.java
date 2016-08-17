package net.gegy1000.pokemon.client.gui.view.inventory;

import com.pokegoapi.api.inventory.Inventories;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.List;

public abstract class InventoryHandler {
    protected Minecraft mc = Minecraft.getMinecraft();
    protected FontRenderer fontRenderer = this.mc.fontRendererObj;
    private PokemonViewGUI gui;
    private InventoryViewHandler inventoryView;

    public void init(PokemonViewGUI gui, InventoryViewHandler inventoryView) {
        this.inventoryView = inventoryView;
        this.gui = gui;
    }

    public abstract void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) throws Exception;

    public abstract void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) throws Exception;

    public abstract void click(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) throws Exception;

    protected void drawTexturedModalRect(float x, float y, float minU, float minV, float maxU, float maxV, float width, float height) {
        this.getGUI().drawTexturedModalRect(x, y, minU, minV, maxU, maxV, width, height);
    }

    protected void drawTexturedModalRect(float x, float y, int textureX, int textureY, int width, int height, int texWidth, int texHeight, double scaleX, double scaleY) {
        this.getGUI().drawTexturedModalRect(x, y, textureX, textureY, width, height, texWidth, texHeight, scaleX, scaleY);
    }

    protected void drawRectangle(double x, double y, double width, double height, int color) {
        this.getGUI().drawRectangle(x, y, width, height, color);
    }

    protected void drawHoveringText(List<String> textLines, int x, int y) {
        GuiUtils.drawHoveringText(textLines, x, y, this.getGUI().width, this.getGUI().height, -1, this.fontRenderer);
    }

    protected PokemonViewGUI getGUI() {
        return this.gui;
    }

    protected InventoryViewHandler getInventoryView() {
        return this.inventoryView;
    }
}
