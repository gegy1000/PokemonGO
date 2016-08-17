package net.gegy1000.pokemon.client.gui.view.inventory;

import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.inventory.Inventories;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.List;

public class IncubatorsHandler extends InventoryHandler {
    @Override
    public void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) {
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.incubators.name", inventories.getIncubators().size()), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        List<EggIncubator> incubators = inventories.getIncubators();
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        slotHandler.draw((slot) -> {
            EggIncubator incubator = incubators.get(slot.getIndex());
            int usesRemaining = incubator.getUsesRemaining();
            String usesRemainingString = String.valueOf(usesRemaining);
            if (usesRemaining <= 0) {
                usesRemainingString = "∞";
            }
            this.fontRenderer.drawString(usesRemainingString, (int) slot.getX() + 1, (int) slot.getY() + 3 + tileRenderSize - 12, LLibrary.CONFIG.getTextColor(), false);
            return null;
        }, (slot) -> {
            List<String> text = new ArrayList<>();
            EggIncubator incubator = incubators.get(slot.getIndex());
            try {
                int usesRemaining = incubator.getUsesRemaining();
                String usesRemainingString = String.valueOf(usesRemaining);
                boolean infinite = false;
                if (usesRemaining <= 0) {
                    usesRemainingString = "∞";
                    infinite = true;
                }
                text.add(TextFormatting.BLUE + I18n.translateToLocal("item." + (infinite ? "incubator_basic_unlimited" : "incubator_basic") + ".name"));
                if (incubator.isInUse()) {
                    text.add(TextFormatting.RED + I18n.translateToLocal("gui.use.name"));
                    text.add(TextFormatting.GREEN + "" + incubator.getKmCurrentlyWalked() + "/" + incubator.getKmTarget() + "km");
                }
                text.add(TextFormatting.GOLD + I18n.translateToLocalFormatted("gui.uses_remaining.name", usesRemainingString));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return text;
        }, incubators.size());
    }

    @Override
    public void click(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
    }
}
