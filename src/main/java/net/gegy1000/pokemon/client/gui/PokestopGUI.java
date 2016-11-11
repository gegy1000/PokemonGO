package net.gegy1000.pokemon.client.gui;

import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Inventory.Item.ItemAwardOuterClass;
import POGOProtos.Inventory.Item.ItemDataOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Responses.FortSearchResponseOuterClass;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.map.fort.FortDetails;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.util.PokemonGUIHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.Element;
import net.ilexiconn.llibrary.client.gui.element.LabelElement;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SideOnly(Side.CLIENT)
public class PokestopGUI extends PokemonGUI {
    private Pokestop pokestop;
    private FortDetails details;
    private AdvancedDynamicTexture icon;
    private BufferedImage iconImage;
    private ButtonElement<PokestopGUI> lootButton;
    private PokestopLootResult result;

    private List<PokestopLoot> loot;

    public PokestopGUI(Pokestop pokestop) {
        this.pokestop = pokestop;
        try {
            PokemonHandler.addTask(() -> {
                try {
                    this.details = pokestop.getDetails();
                    this.iconImage = ImageIO.read(new URL(this.details.getImageUrl().get(0)).openStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initElements() {
        ScaledResolution resolution = new ScaledResolution(this.mc);
        this.addElement(this.lootButton = new ButtonElement<>(this, I18n.translateToLocal("gui.loot.name"), 0, this.height - 36, this.width, 18, (button) -> {
            if (this.pokestop.canLoot()) {
                PokemonHandler.addTask(() -> {
                    try {
                        this.result = this.pokestop.loot();
                        if (this.result.getResult() == FortSearchResponseOuterClass.FortSearchResponse.Result.INVENTORY_FULL) {
                            WindowElement<PokestopGUI> window = new WindowElement<>(this, I18n.translateToLocal("gui.failure.name"), 115, 26);
                            new LabelElement<>(this, I18n.translateToLocal("gui.inventory_full.name"), 2, 16).withParent(window);
                            this.addElement(window);
                            PokemonHandler.API.getPlayerProfile().updateProfile();
                        }
                        this.loot = new ArrayList<>();
                        Map<ItemIdOuterClass.ItemId, Integer> items = new HashMap<>();
                        for (ItemAwardOuterClass.ItemAward item : this.result.getItemsAwarded()) {
                            Integer count = items.get(item.getItemId());
                            if (count == null) {
                                count = 0;
                            }
                            count += item.getItemCount();
                            items.put(item.getItemId(), count);
                        }
                        for (Map.Entry<ItemIdOuterClass.ItemId, Integer> entry : items.entrySet()) {
                            ItemDataOuterClass.ItemData.Builder builder = ItemDataOuterClass.ItemData.newBuilder();
                            builder.setCount(entry.getValue());
                            builder.setItemId(entry.getKey());
                            this.loot.add(new PokestopLoot(new Item(builder.build())));
                        }
                        if (this.result.toPrimitive().hasPokemonDataEgg()) {
                            this.loot.add(new PokestopLoot(this.result.toPrimitive().getPokemonDataEgg()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                });
                return true;
            }
            return false;
        }));
        int tileSize = resolution.getScaleFactor() * 21;
        int tileOffsetX = 25 + (this.width - 80 - tileSize * 8) / 2;
        int tileOffsetY = this.height - tileSize * 8 / 2 - 10;
        this.addElement(new InventoryGridElement<>(this, tileOffsetX, tileOffsetY, tileSize * 2, tileSize * 3, 2, tileSize, (slotHandler) -> {
            int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
            slotHandler.draw((slot) -> {
                if (this.loot != null) {
                    if (slot.getIndex() < this.loot.size()) {
                        PokestopLoot loot = this.loot.get(slot.getIndex());
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        if (!loot.hasEgg()) {
                            Item item = loot.getItem();
                            this.mc.getTextureManager().bindTexture(PokemonGUIHandler.getTexture(item.getItemId()));
                            this.drawTexturedModalRect(slot.getX() + 3, slot.getY() + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
                            this.fontRendererObj.drawString("x" + item.getCount(), slot.getX() + 2, slot.getY() + tileRenderSize - 9, LLibrary.CONFIG.getTextColor(), false);
                        } else {
                            this.mc.getTextureManager().bindTexture(PokemonGUI.EGG_TEXTURE);
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                            this.drawTexturedModalRect(slot.getX() + 3, slot.getY() + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
                            this.fontRendererObj.drawString("x1", slot.getX() + 2, slot.getY() + tileRenderSize - 9, LLibrary.CONFIG.getTextColor(), false);
                        }
                    }
                }
                return null;
            }, (slot) -> {
                List<String> text = new ArrayList<>();
                if (this.loot != null) {
                    if (slot.getIndex() < this.loot.size()) {
                        PokestopLoot loot = this.loot.get(slot.getIndex());
                        if (!loot.hasEgg()) {
                            Item item = loot.getItem();
                            text.add(TextFormatting.BLUE + I18n.translateToLocal("pokeitem." + item.getItemId().name().replaceAll("ITEM_", "").toLowerCase(Locale.ENGLISH) + ".name"));
                            text.add(TextFormatting.GREEN + "x" + item.getCount());
                        } else {
                            text.add(I18n.translateToLocal("item.egg.name"));
                        }
                    }
                }
                return text;
            }, Math.max(this.loot != null ? this.loot.size() : 0, 6));
            return null;
        }));
    }

    @Override
    public void drawScreen(float mouseX, float mouseY, float partialTicks) {
        try {
            this.drawRectangle(0, this.height - 18.0F, this.width, 18.0F, LLibrary.CONFIG.getPrimaryColor());
            this.drawRectangle(0, 0, this.width, 18.0F, LLibrary.CONFIG.getPrimaryColor());
            String titleString = I18n.translateToLocal("pokemon.pokestop.name");
            this.fontRendererObj.drawString(titleString, this.width / 2 - this.fontRendererObj.getStringWidth(titleString) / 2, 6, LLibrary.CONFIG.getTextColor(), false);
            if (PokemonHandler.API != null) {
                String lootability = I18n.translateToLocal("gui.lootable.name");
                boolean enabled = true;
                if (!this.pokestop.canLoot()) {
                    enabled = false;
                    if (!this.pokestop.inRange()) {
                        lootability = I18n.translateToLocal("gui.far.name");
                    } else {
                        long cooldown = this.pokestop.getCooldownCompleteTimestampMs() - PokemonHandler.API.currentTimeMillis();
                        if (cooldown > 0) {
                            lootability = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(cooldown), TimeUnit.MILLISECONDS.toMinutes(cooldown) % TimeUnit.HOURS.toMinutes(1), TimeUnit.MILLISECONDS.toSeconds(cooldown) % TimeUnit.MINUTES.toSeconds(1));
                        }
                    }
                }
                this.lootButton.withColorScheme(enabled ? Element.DEFAULT : THEME_DISABLED).setEnabled(enabled);
                lootability = "* " + lootability + " *";
                this.fontRendererObj.drawString(lootability, this.width / 2 - this.fontRendererObj.getStringWidth(lootability) / 2, this.height - 12, LLibrary.CONFIG.getTextColor(), false);
                if (enabled) {
                    this.result = null;
                }
            }
            if (this.details != null) {
                String description = "\"" + (this.details.getDescription().length() == 0 ? I18n.translateToLocal("gui.no_description.name") : this.details.getDescription()) + "\"";
                this.fontRendererObj.drawString(description, this.width / 2 - this.fontRendererObj.getStringWidth(description) / 2, 24.0F, LLibrary.CONFIG.getTextColor(), false);
                if (this.pokestop.hasLure()) {
                    this.fontRendererObj.drawString(I18n.translateToLocal("Luring"), 5, this.height - 12, LLibrary.CONFIG.getTextColor());
                }
            }
            if (this.iconImage != null && this.icon == null) {
                this.icon = new AdvancedDynamicTexture("pokestop_icon", this.iconImage);
            }
            ScaledResolution resolution = new ScaledResolution(this.mc);
            int size = resolution.getScaleFactor() * 64;
            int iconX = this.width / 2 - (size / 2);
            int iconY = this.height / 2 - (size / 2);
            this.drawRectangle(iconX - 1, iconY - 1, size + 2, size + 2, LLibrary.CONFIG.getSecondaryColor());
            if (this.icon != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableTexture2D();
                this.icon.bind();
                this.drawTexturedModalRect(iconX, iconY, 0, 0, size, size, size, size, 1.0, 1.0);
            }
            int tileSize = resolution.getScaleFactor() * 21;
            int tileOffsetX = 25 + (this.width - 80 - tileSize * 8) / 2;
            int tileOffsetY = this.height - tileSize * 8 / 2 - 10;
            this.fontRendererObj.drawString(I18n.translateToLocal("gui.looted_items.name"), tileOffsetX, tileOffsetY - 15, LLibrary.CONFIG.getTextColor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.icon != null) {
            this.icon.delete();
        }
    }

    private class PokestopLoot {
        private Item item;
        private PokemonDataOuterClass.PokemonData egg;

        public PokestopLoot(Item item) {
            this.item = item;
        }

        public PokestopLoot(PokemonDataOuterClass.PokemonData egg) {
            this.egg = egg;
        }

        public Item getItem() {
            return this.item;
        }

        public PokemonDataOuterClass.PokemonData getEgg() {
            return this.egg;
        }

        public boolean hasEgg() {
            return this.egg != null;
        }
    }
}
