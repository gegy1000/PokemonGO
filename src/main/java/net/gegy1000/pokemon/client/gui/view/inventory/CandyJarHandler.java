package net.gegy1000.pokemon.client.gui.view.inventory;

import POGOProtos.Enums.PokemonFamilyIdOuterClass;
import com.pokegoapi.api.inventory.CandyJar;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.util.PokeNames;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.util.PokemonSpriteHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        slotHandler.draw((slot) -> {
            int index = 0;
            for (Map.Entry<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> candy : candies.entrySet()) {
                if (index == slot.getIndex()) {
                    this.mc.getTextureManager().bindTexture(PokemonSpriteHandler.get(candy.getKey()));
                    this.drawTexturedModalRect(slot.getX() + 3, slot.getY() + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
                    this.fontRenderer.drawString("x" + candy.getValue(), (int) slot.getX() + 1, (int) slot.getY() + 3 + tileRenderSize - 12, LLibrary.CONFIG.getTextColor(), false);
                    return null;
                }
                index++;
            }
            return null;
        }, (slot) -> {
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
}
