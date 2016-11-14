package net.gegy1000.pokemon.client.gui.view.inventory;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.inventory.Inventories;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.ClientProxy;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IncubatorsHandler extends InventoryHandler {
    @Override
    public void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) {
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.incubators.name", inventories.getIncubators().size()), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        List<EggIncubator> incubators = inventories.getIncubators();
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        DecimalFormat shortDecimalFormat = new DecimalFormat("#.##", symbols);
        slotHandler.draw((slot) -> {
            GlStateManager.enableBlend();
            EggIncubator incubator = incubators.get(slot.getIndex());
            int usesRemaining = incubator.getUsesRemaining();
            String usesRemainingString = String.valueOf(usesRemaining);
            if (usesRemaining <= 0) {
                usesRemainingString = "∞";
                ClientProxy.MINECRAFT.getTextureManager().bindTexture(PokemonGUIHandler.getTexture(ItemIdOuterClass.ItemId.ITEM_INCUBATOR_BASIC_UNLIMITED));
            } else {
                ClientProxy.MINECRAFT.getTextureManager().bindTexture(PokemonGUIHandler.getTexture(ItemIdOuterClass.ItemId.ITEM_INCUBATOR_BASIC));
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(slot.getX() + 1, slot.getY() + (incubator.isInUse() ? 4 : 1), 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 4, tileRenderSize - 4);
            this.fontRenderer.drawString(usesRemainingString, (int) slot.getX() + 1, (int) slot.getY() + 3 + tileRenderSize - 11, LLibrary.CONFIG.getTextColor(), false);
            if (incubator.isInUse()) {
                this.drawRectangle(slot.getX() + 1, slot.getY() + 1, tileRenderSize - 2, 4, LLibrary.CONFIG.getPrimaryColor());
                this.drawRectangle(slot.getX() + 2, slot.getY() + 2, incubator.getKmCurrentlyWalked() * (tileRenderSize - 4) / (incubator.getKmTarget() - incubator.getKmStart()), 2, 0xFF4AD33C);
            }
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
                text.add(TextFormatting.BLUE + I18n.translateToLocal("pokeitem." + (infinite ? "incubator_basic_unlimited" : "incubator_basic") + ".name"));
                if (incubator.isInUse()) {
                    text.add(TextFormatting.RED + I18n.translateToLocal("gui.use.name"));
                    text.add(TextFormatting.GREEN + "" + shortDecimalFormat.format(incubator.getKmCurrentlyWalked()) + "/" + shortDecimalFormat.format(incubator.getKmTarget() - incubator.getKmStart()) + "km");
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
