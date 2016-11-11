package net.gegy1000.pokemon.client.util;

import POGOProtos.Enums.PokemonFamilyIdOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.pokemon.Pokemon;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.earth.server.util.TempFileUtil;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.ilexiconn.llibrary.client.gui.element.color.ColorScheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;

public class PokemonGUIHandler {
    public static final ColorScheme THEME_TAB_ACTIVE = ColorScheme.create(() -> LLibrary.CONFIG.getAccentColor(), () -> LLibrary.CONFIG.getAccentColor());
    public static final ColorScheme THEME_WINDOW = ColorScheme.create(() -> LLibrary.CONFIG.getSecondaryColor(), () -> LLibrary.CONFIG.getAccentColor());
    public static final ColorScheme THEME_DISABLED = ColorScheme.create(() -> LLibrary.CONFIG.getSecondaryColor(), () -> LLibrary.CONFIG.getSecondaryColor());

    public static final Map<ItemIdOuterClass.ItemId, ResourceLocation> ITEM_TEXTURES = new HashMap<>();
    public static final ResourceLocation CANDY_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/candy.png");

    public static final ResourceLocation NEUTRAL_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/neutral.png");
    public static final ResourceLocation INSTINCT_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/instinct.png");
    public static final ResourceLocation VALOR_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/valor.png");
    public static final ResourceLocation MYSTIC_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/mystic.png");
    public static final ResourceLocation STARDUST_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/stardust.png");
    public static final ResourceLocation POKECOIN_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/pokecoin.png");
    public static final ResourceLocation EGG_2_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/egg_2.png");
    public static final ResourceLocation EGG_5_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/egg_5.png");
    public static final ResourceLocation EGG_10_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/egg_10.png");

    public static final Map<Integer, AdvancedDynamicTexture> POKEMON_SPRITES = new HashMap<>();
    public static final Map<Integer, BufferedImage> DOWNLOADED_POKEMON_SPRITES = new HashMap<>();

    private static final Queue<FutureTask<?>> TASKS = new LinkedBlockingDeque<>();

    public static void onPreInit() {
        Thread taskThread = new Thread(() -> {
            try {
                if (TASKS.size() > 0) {
                    FutureTask<?> task = TASKS.poll();
                    task.run();
                    task.get();
                }
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        taskThread.setName("Pokemon Sprite Downloader");
        taskThread.setDaemon(true);
        taskThread.start();

        File tempPokemonDirectory = TempFileUtil.getTempFile("pokemon/sprites");
        if (tempPokemonDirectory.exists()) {
            for (File sprite : tempPokemonDirectory.listFiles()) {
                if (sprite.isFile()) {
                    try {
                        int type = Integer.parseInt(sprite.getName().split("\\.")[0]);
                        BufferedImage read = ImageIO.read(sprite);
                        POKEMON_SPRITES.put(type, new AdvancedDynamicTexture("pokemon." + type, read));
                    } catch (NumberFormatException e) {
                    } catch (Exception e) {
                        System.err.println("Failed to load cached pokemon sprite: " + sprite.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
        for (ItemIdOuterClass.ItemId item : ItemIdOuterClass.ItemId.values()) {
            if (item != ItemIdOuterClass.ItemId.ITEM_UNKNOWN) {
                ITEM_TEXTURES.put(item, new ResourceLocation(PokemonGO.MODID, "textures/items/" + item.name().toLowerCase(Locale.ENGLISH).replaceAll("item_", "") + ".png"));
            }
        }
    }

    public static void addTask(Callable<?> task) {
        TASKS.add(new FutureTask<>(task));
    }

    public static void openReviveWindow(PokemonViewGUI gui, ItemIdOuterClass.ItemId item) {
        WindowElement<PokemonViewGUI> window = new WindowElement<>(gui, I18n.translateToLocal("gui.revive.name"), 200, 200);
        try {
            PokeBank pokebank = PokemonHandler.API.getInventories().getPokebank();
            List<Pokemon> pokemons = pokebank.getPokemons();
            List<Pokemon> renderPokemons = new ArrayList<>();
            for (Pokemon pokemon : pokemons) {
                if (pokemon.getStamina() <= 0) {
                    renderPokemons.add(pokemon);
                }
            }
            new InventoryGridElement<>(gui, 2.0F, 16.0F, 194, 182, 3, 63, (slotHandler) -> {
                int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                slotHandler.draw((slot) -> {
                    Pokemon pokemon = renderPokemons.get(slot.getIndex());
                    AdvancedDynamicTexture texture = PokemonGUIHandler.getTexture(pokemon.getPokemonId());
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    if (texture != null) {
                        texture.bind();
                        gui.drawTexturedModalRect(slot.getX(), slot.getY(), 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize, tileRenderSize);
                    }
                    fontRenderer.drawString(I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()), (int) slot.getX() + 1, (int) slot.getY() + tileRenderSize - 9, LLibrary.CONFIG.getTextColor());
                    return null;
                }, (slot) -> {
                    List<String> text = new ArrayList<>();
                    Pokemon pokemon = renderPokemons.get(slot.getIndex());
                    text.add(TextFormatting.BLUE + (pokemon.getNickname() != null && pokemon.getNickname().length() > 0 ? pokemon.getNickname() : PokemonGUIHandler.getName(pokemon.getPokemonId())));
                    text.add(TextFormatting.GREEN + I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()));
                    return text;
                }, renderPokemons.size());
                return null;
            }, (slotHandler) -> {
                slotHandler.click((slot) -> {
                    Pokemon pokemon = renderPokemons.get(slot.getIndex());
                    PokemonHandler.addTask(() -> {
                        PokemonRequestHandler.revive(pokemon, item);
                        return null;
                    });
                    gui.removeElement(window);
                    return true;
                }, renderPokemons.size());
                return true;
            }).withParent(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
        gui.addElement(window);
    }

    public static void openHealWindow(PokemonViewGUI gui, ItemIdOuterClass.ItemId item) {
        WindowElement<PokemonViewGUI> window = new WindowElement<>(gui, I18n.translateToLocal("gui.heal.name"), 200, 200);
        try {
            PokeBank pokebank = PokemonHandler.API.getInventories().getPokebank();
            List<Pokemon> pokemons = pokebank.getPokemons();
            List<Pokemon> renderPokemons = new ArrayList<>();
            for (Pokemon pokemon : pokemons) {
                if (pokemon.getStamina() > 0 && pokemon.getStamina() < pokemon.getMaxStamina()) {
                    renderPokemons.add(pokemon);
                }
            }
            new InventoryGridElement<>(gui, 2.0F, 16.0F, 194, 182, 3, 63, (slotHandler) -> {
                int tileRenderSize = slotHandler.getGrid().getRenderTileSize();
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                slotHandler.draw((slot) -> {
                    Pokemon pokemon = renderPokemons.get(slot.getIndex());
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    AdvancedDynamicTexture texture = PokemonGUIHandler.getTexture(pokemon.getPokemonId());
                    if (texture != null) {
                        texture.bind();
                        gui.drawTexturedModalRect(slot.getX(), slot.getY(), 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize, tileRenderSize);
                    }
                    fontRenderer.drawString(I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()), (int) slot.getX() + 1, (int) slot.getY() + tileRenderSize - 9, LLibrary.CONFIG.getTextColor());
                    return null;
                }, (slot) -> {
                    List<String> text = new ArrayList<>();
                    Pokemon pokemon = renderPokemons.get(slot.getIndex());
                    text.add(TextFormatting.BLUE + (pokemon.getNickname() != null && pokemon.getNickname().length() > 0 ? pokemon.getNickname() : PokemonGUIHandler.getName(pokemon.getPokemonId())));
                    text.add(TextFormatting.GREEN + I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()));
                    return text;
                }, renderPokemons.size());
                return null;
            }, (slotHandler) -> {
                slotHandler.click((slot) -> {
                    Pokemon pokemon = renderPokemons.get(slot.getIndex());
                    PokemonHandler.addTask(() -> {
                        PokemonRequestHandler.heal(pokemon, item);
                        return null;
                    });
                    gui.removeElement(window);
                    return true;
                }, renderPokemons.size());
                return true;
            }).withParent(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
        gui.addElement(window);
    }

    public static void update() {
        synchronized (DOWNLOADED_POKEMON_SPRITES) {
            synchronized (POKEMON_SPRITES) {
                for (Map.Entry<Integer, BufferedImage> entry : DOWNLOADED_POKEMON_SPRITES.entrySet()) {
                    int type = entry.getKey();
                    POKEMON_SPRITES.put(type, new AdvancedDynamicTexture("pokemon." + type, entry.getValue()));
                }
            }
            DOWNLOADED_POKEMON_SPRITES.clear();
        }
    }

    public static AdvancedDynamicTexture getTexture(PokemonIdOuterClass.PokemonId pokemon) {
        int number = pokemon.getNumber();
        if (POKEMON_SPRITES.containsKey(number)) {
            return POKEMON_SPRITES.get(number);
        } else {
            synchronized (POKEMON_SPRITES) {
                POKEMON_SPRITES.put(number, null);
            }
            PokemonGUIHandler.addTask(() -> {
                URL imageURL = null;
                try {
                    imageURL = new URL("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + number + ".png");
                    HttpsURLConnection connection = (HttpsURLConnection) imageURL.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("user-agent", "MinecraftPokemonGO/" + PokemonGO.VERSION);
                    BufferedImage image = ImageIO.read(connection.getInputStream());
                    synchronized (DOWNLOADED_POKEMON_SPRITES) {
                        DOWNLOADED_POKEMON_SPRITES.put(number, image);
                    }
                    ImageIO.write(image, "png", TempFileUtil.createTempFile("pokemon/sprites/" + number + ".png"));
                } catch (Exception e) {
                    System.err.println("Failed to download Pokemon sprite " + number);
                    e.printStackTrace();
                }
                return null;
            });
            return null;
        }
    }

    public static ResourceLocation getTexture(ItemIdOuterClass.ItemId item) {
        ResourceLocation texture = ITEM_TEXTURES.get(item);
        return texture == null ? TextureMap.LOCATION_MISSING_TEXTURE : texture;
    }

    public static ResourceLocation getTexture(PokemonFamilyIdOuterClass.PokemonFamilyId candy) {
        return CANDY_TEXTURE;
    }

    public static String getName(PokemonIdOuterClass.PokemonId pokemon) {
        return I18n.translateToLocal("pokemon." + pokemon.name().toLowerCase(Locale.ENGLISH) + ".name");
    }

    public static String getName(PokemonFamilyIdOuterClass.PokemonFamilyId pokemon) {
        return PokemonGUIHandler.getName(PokemonIdOuterClass.PokemonId.values()[pokemon.getNumber()]);
    }

    public static ResourceLocation getEggTexture(double length) {
        if (length > 6) {
            return PokemonGUIHandler.EGG_10_TEXTURE;
        } else if (length > 3) {
            return PokemonGUIHandler.EGG_5_TEXTURE;
        }
        return PokemonGUIHandler.EGG_2_TEXTURE;
    }
}
