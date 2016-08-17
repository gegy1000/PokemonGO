package net.gegy1000.pokemon.client.gui.view.inventory;

import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.ItemBag;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonSpriteHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class BackpackHandler extends InventoryHandler {
    @Override
    public void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) throws Exception {
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.backpack.name", inventories.getItemBag().getItemsCount(), PokemonHandler.GO.getPlayerProfile().getPlayerData().getMaxItemStorage()), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        ItemBag bag = inventories.getItemBag();
        final Collection<Item> items = bag.getItems();
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        slotHandler.draw((slot) -> {
            int index = 0;
            for (Item item : items) {
                if (index == slot.getIndex()) {
                    this.mc.getTextureManager().bindTexture(PokemonSpriteHandler.get(item.getItemId()));
                    this.drawTexturedModalRect(slot.getX() + 3, slot.getY() + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
                    this.fontRenderer.drawString("x" + item.getCount(), (int) slot.getX() + 1, (int) slot.getY() + 3 + tileRenderSize - 12, LLibrary.CONFIG.getTextColor(), false);
                    return null;
                }
                index++;
            }
            return null;
        }, (slot) -> {
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
        }, items.size());
    }

    @Override
    public void click(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
    }
}
