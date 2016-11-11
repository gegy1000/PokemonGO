package net.gegy1000.pokemon.client.gui.view.inventory;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.inventory.Hatchery;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.pokemon.EggPokemon;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.ClientProxy;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HatcheryHandler extends InventoryHandler {
    @Override
    public void render(Inventories inventories, float mouseX, float mouseY, float partialTicks) {
        this.fontRenderer.drawString(I18n.translateToLocalFormatted("gui.hatchery.name", inventories.getHatchery().getEggs().size()), 85, 23, LLibrary.CONFIG.getTextColor());
    }

    @Override
    public void renderSlots(Inventories inventories, InventoryGridElement.SlotHandler slotHandler) {
        Hatchery hatchery = inventories.getHatchery();
        Set<EggPokemon> eggs = hatchery.getEggs();
        int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        DecimalFormat shortDecimalFormat = new DecimalFormat("#.###", symbols);
        slotHandler.draw((slot) -> {
            int index = 0;
            for (EggPokemon egg : eggs) {
                if (index == slot.getIndex()) {
                    this.mc.getTextureManager().bindTexture(PokemonGUIHandler.getEggTexture(egg.getEggKmWalkedTarget()));
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.drawTexturedModalRect(slot.getX() + 3, slot.getY() + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
                    this.fontRenderer.drawString((int) egg.getEggKmWalkedTarget() + "km", (int) slot.getX() + 1, (int) slot.getY() + 3 + tileRenderSize - 11, LLibrary.CONFIG.getTextColor(), false);
                    if (egg.isIncubate()) {
                        this.drawRectangle(slot.getX() + 1, slot.getY() + 1, tileRenderSize - 2, 4, LLibrary.CONFIG.getPrimaryColor());
                        this.drawRectangle(slot.getX() + 2, slot.getY() + 2, egg.getEggKmWalked() * (tileRenderSize - 4) / egg.getEggKmWalkedTarget(), 2, 0xFF4AD33C);
                    }
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
                        text.add(TextFormatting.GREEN + "" + shortDecimalFormat.format(egg.getEggKmWalked()) + "/" + shortDecimalFormat.format(egg.getEggKmWalkedTarget()) + "km");
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
        try {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
            DecimalFormat shortDecimalFormat = new DecimalFormat("#.##", symbols);
            Set<EggPokemon> eggs = inventories.getHatchery().getEggs();
            List<EggIncubator> incubators = new ArrayList<>();
            for (EggIncubator incubator : inventories.getIncubators()) {
                if (!incubator.isInUse()) {
                    incubators.add(incubator);
                }
            }
            ScaledResolution resolution = new ScaledResolution(ClientProxy.MINECRAFT);
            slotHandler.click(slot -> {
                int index = 0;
                for (EggPokemon egg : eggs) {
                    if (index == slot.getIndex()) {
                        if (!egg.isIncubate()) {
                            WindowElement<PokemonViewGUI> window = new WindowElement<>(this.getGUI(), I18n.translateToLocal("gui.incubate.name"), 200, 200, true);
                            int tileSize = resolution.getScaleFactor() * 21;
                            new InventoryGridElement<>(this.getGUI(), 4.0F, 18.0F, 196, 182, 200 / tileSize, tileSize, (hatcherySlotHandler) -> {
                                hatcherySlotHandler.draw((hatcherySlot) -> {
                                    EggIncubator incubator = incubators.get(hatcherySlot.getIndex());
                                    int usesRemaining = incubator.getUsesRemaining();
                                    String usesRemainingString = String.valueOf(usesRemaining);
                                    if (usesRemaining <= 0) {
                                        usesRemainingString = "∞";
                                        ClientProxy.MINECRAFT.getTextureManager().bindTexture(PokemonGUIHandler.getTexture(ItemIdOuterClass.ItemId.ITEM_INCUBATOR_BASIC_UNLIMITED));
                                    } else {
                                        ClientProxy.MINECRAFT.getTextureManager().bindTexture(PokemonGUIHandler.getTexture(ItemIdOuterClass.ItemId.ITEM_INCUBATOR_BASIC));
                                    }
                                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                                    this.drawTexturedModalRect(hatcherySlot.getX(), hatcherySlot.getY(), 0.0F, 0.0F, 1.0F, 1.0F, tileSize - 2, tileSize - 2);
                                    this.fontRenderer.drawString(usesRemainingString, (int) hatcherySlot.getX() + 1, (int) hatcherySlot.getY() + 3 + tileSize - 14, LLibrary.CONFIG.getTextColor(), false);
                                    return null;
                                }, (hatcherySlot) -> {
                                    List<String> text = new ArrayList<>();
                                    EggIncubator incubator = incubators.get(hatcherySlot.getIndex());
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
                                            text.add(TextFormatting.GREEN + "" + shortDecimalFormat.format(incubator.getKmCurrentlyWalked()) + "/" + shortDecimalFormat.format(incubator.getKmTarget()) + "km");
                                        }
                                        text.add(TextFormatting.GOLD + I18n.translateToLocalFormatted("gui.uses_remaining.name", usesRemainingString));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return text;
                                }, incubators.size());
                                return null;
                            }, (hatcherySlotHandler) -> {
                                hatcherySlotHandler.click((hatcherySlot) -> {
                                    PokemonHandler.addTask(() -> {
                                        EggIncubator incubator = incubators.get(hatcherySlot.getIndex());
                                        try {
                                            egg.incubate(incubator);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    });
                                    this.getGUI().removeElement(window);
                                    return true;
                                }, incubators.size());
                                return false;
                            }).withParent(window);
                            this.getGUI().addElement(window);
                            return true;
                        }
                    }
                    index++;
                }
                return false;
            }, eggs.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
