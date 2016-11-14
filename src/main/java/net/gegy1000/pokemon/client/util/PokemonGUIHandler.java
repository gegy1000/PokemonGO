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
import net.ilexiconn.llibrary.client.gui.element.ButtonElement;
import net.ilexiconn.llibrary.client.gui.element.IElementGUI;
import net.ilexiconn.llibrary.client.gui.element.LabelElement;
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
import java.util.function.Consumer;

public class PokemonGUIHandler {
    public static final ColorScheme THEME_TAB_ACTIVE = ColorScheme.create(() -> LLibrary.CONFIG.getAccentColor(), () -> LLibrary.CONFIG.getAccentColor());
    public static final ColorScheme THEME_WINDOW = ColorScheme.create(() -> LLibrary.CONFIG.getSecondaryColor(), () -> LLibrary.CONFIG.getAccentColor());
    public static final ColorScheme THEME_DISABLED = ColorScheme.create(() -> LLibrary.CONFIG.getSecondaryColor(), () -> LLibrary.CONFIG.getSecondaryColor());

    public static final Map<ItemIdOuterClass.ItemId, ResourceLocation> ITEM_TEXTURES = new HashMap<>();
    public static final ResourceLocation CANDY_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/candy.png");
    public static final ResourceLocation CANDY_STRIPE_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/candy_stripes.png");

    public static final Map<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> CANDY_PRIMARY = new HashMap<>();
    public static final Map<PokemonFamilyIdOuterClass.PokemonFamilyId, Integer> CANDY_SECONDARY = new HashMap<>();

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

    public static final Minecraft MINECRAFT = Minecraft.getMinecraft();

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

        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_BULBASAUR, 0x36C8A4, 0xA3FB83);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_CHARMANDER, 0xF09230, 0xFFE699);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_SQUIRTLE, 0x85C4D6, 0xF2E8BE);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_CATERPIE, 0xA5CD87, 0xFAE3B1);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_WEEDLE, 0xE7BC83, 0xDB76AD);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_PIDGEY, 0xE9E0B7, 0xD29E65);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_RATTATA, 0xA989BA, 0xD9D7BE);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_SPEAROW, 0xEBB9A0, 0xFE5D6C);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_EKANS, 0xCBA8C9, 0xF1E090);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_PIKACHU, 0xF5D368, 0xE2A65D);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_SANDSHREW, 0xE0D2A4, 0xC9B180);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_NIDORAN_FEMALE, 0xC5D3E4, 0x9697C5);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_NIDORAN_MALE, 0xD59FC1, 0xC37096);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_CLEFAIRY, 0xF1D3D1, 0xF1BFC0);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_VULPIX, 0xF5865E, 0xF6D29C);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_JIGGLYPUFF, 0xF1D2E1, 0xEAB9CE);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_ZUBAT, 0x478ABF, 0xDC8DD7);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_ODDISH, 0x7095BF, 0x75C06B);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_PARAS, 0xF1873D, 0xFFD159);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_VENONAT, 0x998FD6, 0xE24379);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_DIGLETT, 0xB08570, 0xEEC5DC);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_MEOWTH, 0xECE0C4, 0xFFE28A);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_PSYDUCK, 0xF4C487, 0xEEEED8);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_MANKEY, 0xE5D6CB, 0xC3927F);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_GROWLITHE, 0xF3A056, 0x3F3D2A);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_POLIWAG, 0x849FCA, 0xECECF6);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_ABRA, 0xE5CE5C, 0x8E7994);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_MACHOP, 0xA1BBDE, 0xDCCEB1);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_BELLSPROUT, 0xEBE16E, 0xAFD57E);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_TENTACOOL, 0x71ACD8, 0xC24589);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_GEODUDE, 0xACA078, 0x756108);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_PONYTA, 0xEDE7C7, 0xF59062);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_SLOWPOKE, 0xDFA1B9, 0xEEE1C7);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_MAGNEMITE, 0xD0DAE0, 0x92B6C6);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_FARFETCHD, 0xAC9E95, 0x95FB97);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_DODUO, 0xC89462, 0xAF755F);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_SEEL, 0xC7DFE8, 0xB6CAED);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_GRIMER, 0xBFA4C7, 0x5F5370);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_SHELLDER, 0xAB9CC5, 0xE0B5B3);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_GASTLY, 0x242223, 0x9B7FB7);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_ONIX, 0xB5B6B8, 0x626264);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_DROWZEE, 0xF8CB58, 0xAF7961);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_KRABBY, 0xEB9063, 0xEDD9CE);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_VOLTORB, 0xB64656, 0xF0E5EA);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_EXEGGCUTE, 0xF4DDE7, 0xEFC3C1);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_CUBONE, 0xD4D5D6, 0xCBB57A);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_HITMONLEE, 0xBD9F88, 0xEEE1C7);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_HITMONCHAN, 0xC8ABBB, 0xE4643B);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_LICKITUNG, 0xE3AEB9, 0xF0E4CA);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_KOFFING, 0x8B8FAE, 0xDEE0BF);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_RHYHORN, 0xBCBDBF, 0x959CA2);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_CHANSEY, 0xE0AEB2, 0xC68D87);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_TANGELA, 0x666C9D, 0xE46E8C);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_KANGASKHAN, 0x978781, 0xE3DDB8);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_HORSEA, 0x9FCFE9, 0xFCF7D7);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_GOLDEEN, 0xE6E6E7, 0xF38469);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_STARYU, 0xB49569, 0xF5E688);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_MR_MIME, 0xE56387, 0xFFCED5);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_SCYTHER, 0x92C587, 0xF6F0CF);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_JYNX, 0xC44552, 0x643187);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_ELECTABUZZ, 0xF5DB77, 0x14175E);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_MAGMAR, 0xF5D477, 0xF0664E);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_PINSIR, 0xBCB1AB, 0xCFD4D8);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_TAUROS, 0xD8A058, 0x887E6F);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_MAGIKARP, 0xE87839, 0xF6F0CF);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_LAPRAS, 0x6BA7D4, 0xFFF0DA);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_DITTO, 0xAD8DBE, 0xDBD8BE);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_EEVEE, 0xCA9761, 0x7E5621);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_PORYGON, 0xE7757C, 0x6BC7C5);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_OMANYTE, 0xDDDCCC, 0x73CEE2);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_KABUTO, 0xC18335, 0x4E4E48);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_AERODACTYL, 0xD4BAD3, 0xB196C5);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_SNORLAX, 0x326583, 0xE3DACE);
        registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_DRATINI, 0x90AED4, 0xEFEAE6);
    }

    private static void registerCandy(PokemonFamilyIdOuterClass.PokemonFamilyId pokemon, int primary, int secondary) {
        CANDY_PRIMARY.put(pokemon, primary);
        CANDY_SECONDARY.put(pokemon, secondary);
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

    public static <T extends IElementGUI> WindowElement<T> getWindow(T gui, String title, String text, Consumer<WindowElement<T>> clicked) {
        int windowWidth = MINECRAFT.fontRendererObj.getStringWidth(text) + 4;
        WindowElement<T> window = new WindowElement<>(gui, title, windowWidth, 45, false);
        new LabelElement<>(gui, text, 2, 18).withParent(window);
        new ButtonElement<>(gui, I18n.translateToLocal("gui.okay.name"), 1, 29, windowWidth - 2, 15, (button) -> {
            clicked.accept(window);
            return true;
        }).withParent(window).withColorScheme(PokemonGUIHandler.THEME_WINDOW);
        return window;
    }

    public static void colour(int colour) {
        int red = (colour >> 16) & 0xFF;
        int green = (colour >> 8) & 0xFF;
        int blue = colour & 0xFF;
        GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, 1.0F);
    }

    public static int getCandyPrimary(PokemonFamilyIdOuterClass.PokemonFamilyId pokemon) {
        Integer primary = CANDY_PRIMARY.get(pokemon);
        return primary == null ? 0xFF6A00 : primary;
    }

    public static int getCandySecondary(PokemonFamilyIdOuterClass.PokemonFamilyId pokemon) {
        Integer secondary = CANDY_SECONDARY.get(pokemon);
        return secondary == null ? 0xFFFFFF : secondary;
    }
}
