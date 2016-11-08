package net.gegy1000.pokemon.client.gui.view.inventory;

import POGOProtos.Data.PokedexEntryOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.inventory.Pokedex;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
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

public class PokedexHandler extends InventoryHandler {
    @Override
    public void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) {
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.pokedex.name", this.getPokedexEntries(inventories.getPokedex()).size()), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        Pokedex pokedex = inventories.getPokedex();
        final Map<PokemonIdOuterClass.PokemonId, PokedexEntryOuterClass.PokedexEntry> entries = this.getPokedexEntries(pokedex);
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        PokemonIdOuterClass.PokemonId[] pokemons = PokemonIdOuterClass.PokemonId.values();
        slotHandler.draw((slot) -> {
            PokemonIdOuterClass.PokemonId pokemon = pokemons[slot.getIndex() + 1];
            if (pokemon != PokemonIdOuterClass.PokemonId.UNRECOGNIZED && pokemon != PokemonIdOuterClass.PokemonId.MISSINGNO) {
                PokedexEntryOuterClass.PokedexEntry pokedexEntry = entries.get(pokemon);
                this.fontRenderer.drawString("#" + pokemon.getNumber(), (int) slot.getX() + 1, (int) slot.getY() + 1, LLibrary.CONFIG.getTextColor());
                GlStateManager.enableBlend();
                if (pokedexEntry != null) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                } else {
                    GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
                }
                AdvancedDynamicTexture texture = PokemonGUIHandler.getTexture(pokemon);
                if (texture != null) {
                    texture.bind();
                    this.drawTexturedModalRect(slot.getX(), slot.getY(), 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize, tileRenderSize);
                }
            }
            return null;
        }, (slot) -> {
            List<String> text = new ArrayList<>();
            PokemonIdOuterClass.PokemonId pokemon = pokemons[slot.getIndex() + 1];
            PokedexEntryOuterClass.PokedexEntry pokedexEntry = entries.get(pokemon);
            if (pokedexEntry != null) {
                text.add(TextFormatting.BLUE + PokemonGUIHandler.getName(pokemon));
                text.add(TextFormatting.GREEN + I18n.translateToLocalFormatted("gui.times_captured.name", String.valueOf(pokedexEntry.getTimesCaptured())));
                text.add(TextFormatting.YELLOW + I18n.translateToLocalFormatted("gui.times_encountered.name", String.valueOf(pokedexEntry.getTimesEncountered())));
            }
            return text;
        }, pokemons.length - 2);
    }

    @Override
    public void click(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
    }

    private Map<PokemonIdOuterClass.PokemonId, PokedexEntryOuterClass.PokedexEntry> getPokedexEntries(Pokedex pokedex) {
        Map<PokemonIdOuterClass.PokemonId, PokedexEntryOuterClass.PokedexEntry> caughtPokemon = new HashMap<>();
        for (PokemonIdOuterClass.PokemonId pokemon : PokemonIdOuterClass.PokemonId.values()) {
            PokedexEntryOuterClass.PokedexEntry entry = pokedex.getPokedexEntry(pokemon);
            if (entry != null) {
                caughtPokemon.put(pokemon, entry);
            }
        }
        return caughtPokemon;
    }
}
