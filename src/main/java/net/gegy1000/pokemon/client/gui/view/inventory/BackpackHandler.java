package net.gegy1000.pokemon.client.gui.view.inventory;

import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.ItemBag;
import net.gegy1000.pokemon.client.gui.PokemonGUI;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
import net.ilexiconn.llibrary.client.gui.element.LabelElement;
import net.ilexiconn.llibrary.client.gui.element.SliderElement;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class BackpackHandler extends InventoryHandler {
    @Override
    public void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) throws Exception {
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.backpack.name", inventories.getItemBag().getItemsCount(), PokemonHandler.API.getPlayerProfile().getPlayerData().getMaxItemStorage()), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        ItemBag bag = inventories.getItemBag();
        final Collection<Item> items = bag.getItems();
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        slotHandler.draw((slot) -> {
            int index = 0;
            GlStateManager.enableBlend();
            for (Item item : items) {
                if (index == slot.getIndex()) {
                    this.mc.getTextureManager().bindTexture(PokemonHandler.getTexture(item.getItemId()));
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
                    text.add(TextFormatting.BLUE + I18n.translateToLocal("pokeitem." + item.getItemId().name().replaceAll("ITEM_", "").toLowerCase(Locale.ENGLISH) + ".name"));
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
        ItemBag bag = inventories.getItemBag();
        final Collection<Item> items = bag.getItems();
        slotHandler.click((slot) -> {
            int index = 0;
            for (Item item : items) {
                if (index == slot.getIndex()) {
                    Function<PokemonViewGUI, Void> itemAction = PokemonHandler.getItemAction(item.getItemId());
                    if (itemAction != null) {
                        WindowElement<PokemonViewGUI> window = new WindowElement<>(this.getGUI(), I18n.translateToLocal("gui.select_action.name"), 99, 34);
                        new ButtonElement<>(this.getGUI(), I18n.translateToLocal("gui.use_item.name"), 1.0F, 15.0F, 48, 18, (button) -> {
                            ElementHandler.INSTANCE.removeElement(this.getGUI(), window);
                            itemAction.apply(this.getGUI());
                            return true;
                        }).withParent(window).withColorScheme(PokemonGUI.THEME_WINDOW);
                        new ButtonElement<>(this.getGUI(), I18n.translateToLocal("gui.remove.name"), 50.0F, 15.0F, 48, 18, (button) -> {
                            ElementHandler.INSTANCE.removeElement(this.getGUI(), window);
                            this.removeItem(bag, item);
                            return true;
                        }).withParent(window).withColorScheme(PokemonGUI.THEME_WINDOW);
                        ElementHandler.INSTANCE.addElement(this.getGUI(), window);
                    } else {
                        this.removeItem(bag, item);
                    }
                    return true;
                }
                index++;
            }
            return false;
        }, items.size());
    }

    private void removeItem(ItemBag bag, Item item) {
        WindowElement<PokemonViewGUI> window = new WindowElement<>(this.getGUI(), I18n.translateToLocal("gui.remove_item.name"), 100, 39);
        new LabelElement<>(this.getGUI(), I18n.translateToLocal("gui.amount.name"), 2.0F, 16.0F).withParent(window);
        SliderElement<PokemonViewGUI> slider = (SliderElement<PokemonViewGUI>) new SliderElement<>(this.getGUI(), 2.0F, 26.0F, true, 0.0F, 0.0F, item.getCount(), value -> true, key -> key >= 0x02 && key <= 0x0B).withParent(window);
        slider.getValueInput().clearText();
        slider.getValueInput().writeText(String.valueOf(1));
        new ButtonElement<>(this.getGUI(), I18n.translateToLocal("gui.remove.name"), 41.0F, 15.0F, 58, 23, (button) -> {
            int value = slider.getValueInput().getText().length() > 0 ? Integer.parseInt(slider.getValueInput().getText()) : 0;
            if (value > 0 && value < item.getCount()) {
                new Thread(() -> {
                    try {
                        bag.removeItem(item.getItemId(), value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ElementHandler.INSTANCE.removeElement(this.getGUI(), window);
                }).start();
                return true;
            }
            return false;
        }).withParent(window).withColorScheme(PokemonGUI.THEME_WINDOW);
        ElementHandler.INSTANCE.addElement(this.getGUI(), window);
    }
}
