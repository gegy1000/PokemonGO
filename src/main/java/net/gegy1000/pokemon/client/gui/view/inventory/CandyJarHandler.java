package net.gegy1000.pokemon.client.gui.view.inventory;

import POGOProtos.Enums.PokemonFamilyIdOuterClass;
import com.pokegoapi.api.inventory.CandyJar;
import com.pokegoapi.api.inventory.Inventories;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandyJarHandler extends InventoryHandler {
    @Override
    public void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) {
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.candies.name", this.getCandies(inventories.getCandyjar()).size(), PokemonFamilyIdOuterClass.PokemonFamilyId.values().length), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        CandyJar candyJar = inventories.getCandyjar();
        Map<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> candies = this.getCandies(candyJar);
        List<PokemonFamilyIdOuterClass.PokemonFamilyId> families = this.getFamilies(candies);
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        slotHandler.draw((slot) -> {
            PokemonFamilyIdOuterClass.PokemonFamilyId family = families.get(slot.getIndex());
            int count = candies.get(family);
            this.mc.getTextureManager().bindTexture(PokemonGUIHandler.getTexture(family));
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(slot.getX() + 3, slot.getY() + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
            this.fontRenderer.drawString("x" + count, (int) slot.getX() + 1, (int) slot.getY() + 3 + tileRenderSize - 11, LLibrary.CONFIG.getTextColor(), false);
            boolean shortened = false;
            String name = PokemonGUIHandler.getName(family);
            while (this.fontRenderer.getStringWidth(name + "..") >= tileRenderSize && name.length() > 1) {
                name = name.substring(0, name.length() - 1);
                shortened = true;
            }
            if (shortened) {
                name += "...";
            }
            this.fontRenderer.drawString(name, (int) slot.getX() + 1, (int) slot.getY() + 1, LLibrary.CONFIG.getTextColor());
            return null;
        }, (slot) -> {
            List<String> text = new ArrayList<>();
            PokemonFamilyIdOuterClass.PokemonFamilyId family = families.get(slot.getIndex());
            int count = candies.get(family);
            text.add(TextFormatting.BLUE + I18n.translateToLocalFormatted("pokeitem.candy.name", PokemonGUIHandler.getName(family)));
            text.add(TextFormatting.GREEN + "x" + count);
            return text;
        }, candies.size());
    }

    @Override
    public void click(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
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

    private List<PokemonFamilyIdOuterClass.PokemonFamilyId> getFamilies(Map<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> candies) {
        List<PokemonFamilyIdOuterClass.PokemonFamilyId> families = new ArrayList<>();
        for (Map.Entry<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> entry : candies.entrySet()) {
            families.add(entry.getKey());
        }
        families.sort((family1, family2) -> Integer.compare(family1.getNumber(), family2.getNumber()));
        return families;
    }
}
