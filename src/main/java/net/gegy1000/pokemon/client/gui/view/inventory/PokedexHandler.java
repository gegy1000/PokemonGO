package net.gegy1000.pokemon.client.gui.view.inventory;

import POGOProtos.Data.PokedexEntryOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.inventory.Pokedex;
import com.pokegoapi.util.PokeNames;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.util.PokemonSpriteHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class PokedexHandler extends InventoryHandler {
    @Override
    public void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) {
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.pokedex.name", this.getPokedexEntries(inventories.getPokedex()).size()), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        Pokedex pokedex = inventories.getPokedex();
        final List<PokedexEntryOuterClass.PokedexEntry> entries = this.getPokedexEntries(pokedex);
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        slotHandler.draw((slot) -> {
            PokedexEntryOuterClass.PokedexEntry pokemon = entries.get(slot.getIndex());
            AdvancedDynamicTexture texture = PokemonSpriteHandler.get(pokemon.getPokemonId());
            if (texture != null) {
                texture.bind();
                this.drawTexturedModalRect(slot.getX(), slot.getY(), 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize, tileRenderSize);
            }
            return null;
        }, (slot) -> {
            List<String> text = new ArrayList<>();
            text.add(TextFormatting.BLUE + PokeNames.getDisplayName(entries.get(slot.getIndex()).getPokemonId().getNumber(), Locale.ENGLISH));
            return text;
        }, entries.size());
    }

    @Override
    public void click(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
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
}
