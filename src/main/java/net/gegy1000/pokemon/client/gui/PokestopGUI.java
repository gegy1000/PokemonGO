package net.gegy1000.pokemon.client.gui;

import POGOProtos.Inventory.Item.ItemAwardOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Map.Fort.FortModifierOuterClass;
import POGOProtos.Networking.Responses.FortSearchResponseOuterClass;
import com.pokegoapi.api.map.fort.FortDetails;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.pokemon.PokemonHandler;
import net.gegy1000.pokemon.pokemon.PokemonSpriteHandler;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.Element;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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

    public PokestopGUI(Pokestop pokestop) {
        this.pokestop = pokestop;
        try {
            this.details = pokestop.getDetails();
            new Thread(() -> {
                try {
                    this.iconImage = ImageIO.read(new URL(this.details.getImageUrl().get(0)).openStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
        }
    }

    @Override
    public void initElements() {
        ElementHandler.INSTANCE.addElement(this, this.lootButton = new ButtonElement<>(this, "Loot", 0, this.height - 36, this.width, 18, (button) -> {
            if (this.pokestop.canLoot()) {
                try {
                    this.result = this.pokestop.loot();
                    if (this.result.getResult() == FortSearchResponseOuterClass.FortSearchResponse.Result.INVENTORY_FULL) {
                        WindowElement<PokestopGUI> window = new WindowElement<>(this, "Failed to loot!", 115, 26);
                        new LabelElement<>(this, "Your inventory is full!", 2, 16).withParent(window);
                        ElementHandler.INSTANCE.addElement(this, window);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }));
    }

    @Override
    public void drawScreen(float mouseX, float mouseY, float partialTicks) {
        try {
            this.drawRectangle(0, this.height - 18.0F, this.width, 18.0F, LLibrary.CONFIG.getPrimaryColor());
            this.drawRectangle(0, 0, this.width, 18.0F, LLibrary.CONFIG.getPrimaryColor());
            String titleString = "Pokéstop";
            this.fontRendererObj.drawString(titleString, this.width / 2 - this.fontRendererObj.getStringWidth(titleString) / 2, 6, LLibrary.CONFIG.getTextColor(), false);
            if (PokemonHandler.GO != null) {
                String lootability = "Lootable";
                boolean enabled = true;
                if (!this.pokestop.canLoot()) {
                    enabled = false;
                    if (!this.pokestop.inRange()) {
                        lootability = "Out of range";
                    } else {
                        long cooldown = this.pokestop.getCooldownCompleteTimestampMs() - PokemonHandler.GO.currentTimeMillis();
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
                String description = "\"" + (this.details.getDescription().length() == 0 ? "No description" : this.details.getDescription()) + "\"";
                this.fontRendererObj.drawString(description, this.width / 2 - this.fontRendererObj.getStringWidth(description) / 2, 24.0F, LLibrary.CONFIG.getTextColor(), false);
                if (this.hasLure()) {
                    this.fontRendererObj.drawString("Luring", 5, this.height - 12, LLibrary.CONFIG.getTextColor());
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
                GlStateManager.enableTexture2D();
                this.icon.bind();
                this.drawTexturedModalRect(iconX, iconY, 0, 0, size, size, size, size, 1.0, 1.0);
            }
            int tileSize = resolution.getScaleFactor() * 21;
            int tileRenderSize = tileSize - 2;
            int tileOffsetX = 25 + (this.width - 80 - tileSize * 8) / 2;
            int tileOffsetY = this.height - tileSize * 8 / 2 - 10;
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 3; y++) {
                    this.drawRectangle(x * tileSize + tileOffsetX, y * tileSize + tileOffsetY, tileRenderSize, tileRenderSize, LLibrary.CONFIG.getSecondaryColor());
                }
            }
            this.fontRendererObj.drawString("Looted Items:", tileOffsetX, tileOffsetY - 15, LLibrary.CONFIG.getTextColor());
            if (this.result != null) {
                if (!this.result.wasSuccessful()) {
                    this.result = null;
                } else {
                    String earnedXP = "Earned " + this.result.getExperience() + " XP!";
                    this.fontRendererObj.drawString(earnedXP, this.width / 2 - this.fontRendererObj.getStringWidth(earnedXP) / 2, iconY - 15, LLibrary.CONFIG.getTextColor());
                    int x = 0;
                    int y = 0;
                    Map<ItemIdOuterClass.ItemId, Integer> awardedItems = new HashMap<>();
                    for (ItemAwardOuterClass.ItemAward item : this.result.getItemsAwarded()) {
                        Integer count = awardedItems.get(item.getItemId());
                        if (count == null) {
                            count = 0;
                        }
                        count += item.getItemCount();
                        awardedItems.put(item.getItemId(), count);
                    }
                    for (Map.Entry<ItemIdOuterClass.ItemId, Integer> entry : awardedItems.entrySet()) {
                        int renderX = x * tileSize + tileOffsetX;
                        int renderY = y * tileSize + tileOffsetY;
                        this.mc.getTextureManager().bindTexture(PokemonSpriteHandler.get(entry.getKey()));
                        this.drawTexturedModalRect(renderX + 3, renderY + 3, 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize - 6, tileRenderSize - 6);
                        this.fontRendererObj.drawString("x" + entry.getValue(), renderX + 2, renderY + tileRenderSize - 9, LLibrary.CONFIG.getTextColor(), false);
                        x++;
                        if (x >= 2) {
                            x = 0;
                            y++;
                        }
                        if (y > 3) {
                            break;
                        }
                    }
                    x = 0;
                    y = 0;
                    for (Map.Entry<ItemIdOuterClass.ItemId, Integer> entry : awardedItems.entrySet()) {
                        float renderX = x * tileSize + tileOffsetX;
                        float renderY = y * tileSize + tileOffsetY;
                        if (mouseX >= renderX && mouseX <= renderX + tileRenderSize && mouseY >= renderY && mouseY <= renderY + tileRenderSize) {
                            List<String> text = new LinkedList<>();
                            ItemIdOuterClass.ItemId itemId = entry.getKey();
                            text.add(TextFormatting.BLUE + I18n.translateToLocal("item." + itemId.name().replaceAll("ITEM_", "").toLowerCase(Locale.ENGLISH) + ".name"));
                            text.add(TextFormatting.GREEN + "x" + entry.getValue());
                            this.drawHoveringText(text, (int) mouseX, (int) mouseY);
                        }
                        x++;
                        if (x >= 2) {
                            x = 0;
                            y++;
                        }
                        if (y > 3) {
                            break;
                        }
                    }
                    GlStateManager.disableLighting();
                }
            }
        } catch (Exception e) {
        }
    }

    private boolean hasLure() {
        List<FortModifierOuterClass.FortModifier> modifiers = this.details.getModifier();
        Iterator iterator = modifiers.iterator();
        FortModifierOuterClass.FortModifier mod;
        do {
            if (!iterator.hasNext()) {
                return false;
            }
            mod = (FortModifierOuterClass.FortModifier) iterator.next();
        } while (mod.getItemId() != ItemIdOuterClass.ItemId.ITEM_TROY_DISK);
        return true;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.icon != null) {
            this.icon.delete();
        }
    }
}
