package net.gegy1000.pokemon.client.gui.view.inventory;

import com.pokegoapi.api.inventory.Hatchery;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.pokemon.EggPokemon;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.ilexiconn.llibrary.LLibrary;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HatcheryHandler extends InventoryHandler {
    private static final ResourceLocation EGG_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/egg.png");

    @Override
    public void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) {
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.hatchery.name", inventories.getHatchery().getEggs().size()), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        Hatchery hatchery = inventories.getHatchery();
        Set<EggPokemon> eggs = hatchery.getEggs();
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        slotHandler.draw((slot) -> {
            int index = 0;
            for (EggPokemon egg : eggs) {
                if (index == slot.getIndex()) {
                    this.mc.getTextureManager().bindTexture(EGG_TEXTURE);
                    this.drawTexturedModalRect(slot.getX() + 3, slot.getY() + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
                    return null;
                }
                index++;
            }
            return null;
        }, (slot) -> {
            List<String> text = new ArrayList<>();
            int index = 0;
            for (EggPokemon egg : eggs) {
                if (index == slot.getIndex()) {
                    text.add(TextFormatting.BLUE + I18n.translateToLocalFormatted("item.egg.name", (egg.getEggKmWalkedTarget() + "km")));
                    try {
                        text.add(TextFormatting.GREEN + "" + egg.getEggKmWalked() + "/" + egg.getEggKmWalkedTarget() + "km");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return text;
                }
                index++;
            }
            return text;
        }, eggs.size());
    }

    @Override
    public void click(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        Set<EggPokemon> eggs = inventories.getHatchery().getEggs();
        slotHandler.click(slot -> true, eggs.size());
    }
}
