package net.gegy1000.pokemon.client.util;

import POGOProtos.Enums.PokemonFamilyIdOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Requests.Messages.ClaimCodenameMessageOuterClass;
import POGOProtos.Networking.Requests.Messages.CollectDailyDefenderBonusMessageOuterClass;
import POGOProtos.Networking.Requests.Messages.UseItemPotionMessageOuterClass;
import POGOProtos.Networking.Requests.Messages.UseItemReviveMessageOuterClass;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import POGOProtos.Networking.Responses.ClaimCodenameResponseOuterClass;
import POGOProtos.Networking.Responses.CollectDailyDefenderBonusResponseOuterClass;
import POGOProtos.Networking.Responses.UseItemPotionResponseOuterClass;
import POGOProtos.Networking.Responses.UseItemReviveResponseOuterClass;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.EggPokemon;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.settings.MapSettings;
import com.pokegoapi.api.settings.Settings;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.google.common.geometry.S2LatLng;
import com.pokegoapi.main.AsyncServerRequest;
import com.pokegoapi.util.AsyncHelper;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.earth.server.util.TempFileUtil;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.pokemon.client.gui.element.InventoryGridElement;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.gegy1000.pokemon.client.renderer.pokemon.CatchableRenderedPokemon;
import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.gui.element.ElementHandler;
import net.ilexiconn.llibrary.client.gui.element.WindowElement;
import net.ilexiconn.llibrary.server.snackbar.Snackbar;
import net.ilexiconn.llibrary.server.snackbar.SnackbarHandler;
import net.ilexiconn.llibrary.server.snackbar.SnackbarPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import okhttp3.OkHttpClient;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class PokemonHandler {
    private static final OkHttpClient CLIENT = new OkHttpClient();

    private static final Map<ItemIdOuterClass.ItemId, ResourceLocation> ITEM_TEXTURES = new HashMap<>();
    private static final ResourceLocation CANDY_TEXTURE = new ResourceLocation(PokemonGO.MODID, "textures/items/candy.png");

    private static final Map<Integer, AdvancedDynamicTexture> POKEMON_SPRITES = new HashMap<>();
    private static final Map<Integer, BufferedImage> DOWNLOADED_POKEMON_SPRITES = new HashMap<>();

    private static final Map<ItemIdOuterClass.ItemId, Function<PokemonViewGUI, Void>> ITEM_ACTIONS = new HashMap<>();

    private static final List<NearbyPokemon> NEARBY_POKEMONS = new LinkedList<>();
    private static final List<CatchablePokemon> CATCHABLE_POKEMON = new LinkedList<>();
    private static final List<CatchableRenderedPokemon> CATCHABLE_RENDERED_POKEMON = new LinkedList<>();
    private static final List<Gym> GYMS = new LinkedList<>();
    private static final List<Pokestop> POKESTOPS = new LinkedList<>();

    private static long lastMapUpdate;
    private static long lastProfileUpdate;

    public static PokemonGo API;

    private static boolean loggingIn;
    private static boolean loginFailed;
    private static String username;

    private static int level;
    private static long experience;
    private static boolean collectingBonus;

    public static void onPreInit() {
        Thread pokemonUpdateThread = new Thread(() -> {
            while (true) {
                if (API != null) {
                    long time = API.currentTimeMillis();
                    Settings settings = API.getSettings();
                    if (settings != null && settings.getMapSettings() != null) {
                        MapSettings mapSettings = settings.getMapSettings();
                        try {
                            if (mapSettings.getMinRefresh() > 0 && time - lastMapUpdate > mapSettings.getMinRefresh()) {
                                lastMapUpdate = time;
                                List<NearbyPokemon> nearbyPokemon = API.getMap().getNearbyPokemon();
                                synchronized (NEARBY_POKEMONS) {
                                    NEARBY_POKEMONS.clear();
                                    NEARBY_POKEMONS.addAll(nearbyPokemon);
                                }
                                List<CatchablePokemon> catchablePokemon = API.getMap().getCatchablePokemon();
                                synchronized (CATCHABLE_POKEMON) {
                                    CATCHABLE_POKEMON.clear();
                                    CATCHABLE_POKEMON.addAll(catchablePokemon);
                                }
                                PokemonHandler.updateRenderedPokemon();
                                List<Gym> gyms = API.getMap().getGyms();
                                for (Gym gym : gyms) {
                                    gym.getGymMembers();
                                }
                                synchronized (GYMS) {
                                    GYMS.clear();
                                    GYMS.addAll(gyms);
                                }
                                Collection<Pokestop> pokestops = API.getMap().getMapObjects().getPokestops();
                                synchronized (POKESTOPS) {
                                    POKESTOPS.clear();
                                    POKESTOPS.addAll(pokestops);
                                }
                            }
                            if (time - lastProfileUpdate > 10000) {
                                API.getPlayerProfile().updateProfile();
                                API.getInventories().updateInventories();
                                lastProfileUpdate = time;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    synchronized (CATCHABLE_POKEMON) {
                        CATCHABLE_POKEMON.clear();
                    }
                    synchronized (CATCHABLE_RENDERED_POKEMON) {
                        CATCHABLE_RENDERED_POKEMON.clear();
                    }
                    synchronized (GYMS) {
                        GYMS.clear();
                    }
                    synchronized (POKESTOPS) {
                        POKESTOPS.clear();
                    }
                }
            }
        });
        pokemonUpdateThread.setName("Pokemon Update Thread");
        pokemonUpdateThread.start();
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
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_REVIVE, (gui) -> {
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
                        AdvancedDynamicTexture texture = PokemonHandler.getTexture(pokemon.getPokemonId());
                        if (texture != null) {
                            texture.bind();
                            gui.drawTexturedModalRect(slot.getX(), slot.getY(), 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize, tileRenderSize);
                        }
                        fontRenderer.drawString(I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()), (int) slot.getX() + 1, (int) slot.getY() + tileRenderSize - 9, LLibrary.CONFIG.getTextColor());
                        return null;
                    }, (slot) -> {
                        List<String> text = new ArrayList<>();
                        Pokemon pokemon = renderPokemons.get(slot.getIndex());
                        text.add(TextFormatting.BLUE + (pokemon.getNickname() != null && pokemon.getNickname().length() > 0 ? pokemon.getNickname() : PokemonHandler.getName(pokemon.getPokemonId())));
                        text.add(TextFormatting.GREEN + I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()));
                        return text;
                    }, renderPokemons.size());
                    return null;
                }, (slotHandler) -> {
                    slotHandler.click((slot) -> {
                        new Thread(() -> PokemonHandler.revive(renderPokemons.get(slot.getIndex()), ItemIdOuterClass.ItemId.ITEM_REVIVE)).start();
                        ElementHandler.INSTANCE.removeElement(gui, window);
                        return true;
                    }, renderPokemons.size());
                    return true;
                }).withParent(window);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ElementHandler.INSTANCE.addElement(gui, window);
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_POTION, (gui) -> {
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
                        AdvancedDynamicTexture texture = PokemonHandler.getTexture(pokemon.getPokemonId());
                        if (texture != null) {
                            texture.bind();
                            gui.drawTexturedModalRect(slot.getX(), slot.getY(), 0.0F, 0.0F, 1.0F, 1.0F, tileRenderSize, tileRenderSize);
                        }
                        fontRenderer.drawString(I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()), (int) slot.getX() + 1, (int) slot.getY() + tileRenderSize - 9, LLibrary.CONFIG.getTextColor());
                        return null;
                    }, (slot) -> {
                        List<String> text = new ArrayList<>();
                        Pokemon pokemon = renderPokemons.get(slot.getIndex());
                        text.add(TextFormatting.BLUE + (pokemon.getNickname() != null && pokemon.getNickname().length() > 0 ? pokemon.getNickname() : PokemonHandler.getName(pokemon.getPokemonId())));
                        text.add(TextFormatting.GREEN + I18n.translateToLocalFormatted("gui.cp.name", pokemon.getCp()));
                        return text;
                    }, renderPokemons.size());
                    return null;
                }, (slotHandler) -> {
                    slotHandler.click((slot) -> {
                        new Thread(() -> PokemonHandler.heal(renderPokemons.get(slot.getIndex()), ItemIdOuterClass.ItemId.ITEM_POTION)).start();
                        ElementHandler.INSTANCE.removeElement(gui, window);
                        return true;
                    }, renderPokemons.size());
                    return true;
                }).withParent(window);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ElementHandler.INSTANCE.addElement(gui, window);
            return null;
        });
    }

    public static void authenticate(String username, String password) {
        PokemonHandler.loggingIn = true;
        PokemonHandler.username = username;
        PokemonHandler.loginFailed = false;
        new Thread(() -> {
            try {
                API = new PokemonGo(CLIENT);
                API.login(new PtcCredentialProvider(CLIENT, username, password));
                PokemonHandler.username = API.getPlayerProfile().getPlayerData().getUsername();
                PokemonHandler.level = API.getPlayerProfile().getStats().getLevel();
                PokemonHandler.experience = API.getPlayerProfile().getStats().getExperience();
                PokemonHandler.loggingIn = false;
            } catch (Exception e) {
                API = null;
                System.err.println("Failed to authenticate.");
                e.printStackTrace();
                PokemonHandler.loggingIn = false;
                PokemonHandler.loginFailed = true;
            }
        }).start();
    }

    public static void update(EntityPlayer player) {
        if (player != null && player.ticksExisted % 4 == 0) {
            if (API != null && !loggingIn) {
                API.setLatitude(PokemonGO.GENERATOR.toLat(player.posZ));
                API.setLongitude(PokemonGO.GENERATOR.toLong(player.posX));
                if (Minecraft.getMinecraft().currentScreen == null) {
                    try {
                        PlayerProfile playerProfile = API.getPlayerProfile();
                        if (playerProfile != null) {
                            int level = playerProfile.getStats().getLevel();
                            if (PokemonHandler.level != level) {
                                if (level > PokemonHandler.level) {
                                    Snackbar snackbar = Snackbar.create((LLibrary.CONFIG.getColorMode().equals("dark") ? TextFormatting.BLACK : TextFormatting.WHITE) + I18n.translateToLocal("snackbar.level_up.name"));
                                    snackbar.setPosition(SnackbarPosition.UP);
                                    snackbar.setColor(LLibrary.CONFIG.getAccentColor());
                                    SnackbarHandler.INSTANCE.showSnackbar(snackbar);
                                    new Thread(() -> {
                                        try {
                                            API.getPlayerProfile().acceptLevelUpRewards(level);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                }
                                PokemonHandler.level = level;
                            }
                            long experience = playerProfile.getStats().getExperience();
                            if (PokemonHandler.experience != experience) {
                                if (experience > PokemonHandler.experience) {
                                    Snackbar snackbar = Snackbar.create((LLibrary.CONFIG.getColorMode().equals("dark") ? TextFormatting.BLACK : TextFormatting.WHITE) + I18n.translateToLocalFormatted("snackbar.experience.name", String.valueOf(experience - PokemonHandler.experience)));
                                    snackbar.setPosition(SnackbarPosition.UP);
                                    snackbar.setColor(LLibrary.CONFIG.getAccentColor());
                                    SnackbarHandler.INSTANCE.showSnackbar(snackbar);
                                }
                                PokemonHandler.experience = experience;
                            }
                            for (EggIncubator incubator : API.getInventories().getIncubators()) {
                                if (incubator.getKmLeftToWalk() <= 0) {
                                    for (EggPokemon egg : API.getInventories().getHatchery().getEggs()) {
                                        if (egg.getEggIncubatorId().equals(incubator.getId())) {
                                            incubator.hatchEgg(egg);
                                            break;
                                        }
                                    }
                                    //TODO Open GUI
                                    break;
                                }
                            }
                            if (!collectingBonus) {
                                long collectTime = PokemonHandler.API.getPlayerProfile().getDailyBonus().getNextCollectedTimestampMs();
                                if (collectTime <= 0) {
                                    collectingBonus = true;
                                    new Thread(() -> {
                                        try {
                                            CollectDailyDefenderBonusMessageOuterClass.CollectDailyDefenderBonusMessage message = CollectDailyDefenderBonusMessageOuterClass.CollectDailyDefenderBonusMessage.newBuilder().build();
                                            CollectDailyDefenderBonusResponseOuterClass.CollectDailyDefenderBonusResponse response = PokemonHandler.request(RequestTypeOuterClass.RequestType.COLLECT_DAILY_DEFENDER_BONUS, message, CollectDailyDefenderBonusResponseOuterClass.CollectDailyDefenderBonusResponse.class);
                                            if (response.getResult() == CollectDailyDefenderBonusResponseOuterClass.CollectDailyDefenderBonusResponse.Result.SUCCESS) {
                                                Snackbar snackbar = Snackbar.create((LLibrary.CONFIG.getColorMode().equals("dark") ? TextFormatting.BLACK : TextFormatting.WHITE) + I18n.translateToLocal("snackbar.daily_bonus.name"));
                                                snackbar.setPosition(SnackbarPosition.UP);
                                                snackbar.setColor(LLibrary.CONFIG.getAccentColor());
                                                SnackbarHandler.INSTANCE.showSnackbar(snackbar);
                                                collectingBonus = false;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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
            new Thread(() -> {
                URL imageURL = null;
                try {
                    imageURL = new URL("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + number + ".png");
                    HttpsURLConnection connection = (HttpsURLConnection) imageURL.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("user-agent", "Minecraft Earth-Mod/" + PokemonGO.VERSION);
                    BufferedImage image = ImageIO.read(connection.getInputStream());
                    synchronized (DOWNLOADED_POKEMON_SPRITES) {
                        DOWNLOADED_POKEMON_SPRITES.put(number, image);
                    }
                    ImageIO.write(image, "png", TempFileUtil.createTempFile("pokemon/sprites/" + number + ".png"));
                } catch (Exception e) {
                    System.err.println("Failed to download Pokemon sprite " + number);
                    e.printStackTrace();
                }
            }).start();
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
        return PokemonHandler.getName(PokemonIdOuterClass.PokemonId.values()[pokemon.getNumber()]);
    }

    public static Function<PokemonViewGUI, Void> getItemAction(ItemIdOuterClass.ItemId item) {
        return ITEM_ACTIONS.get(item);
    }

    public static void revive(Pokemon pokemon, ItemIdOuterClass.ItemId item) {
        try {
            UseItemReviveMessageOuterClass.UseItemReviveMessage message = UseItemReviveMessageOuterClass.UseItemReviveMessage.newBuilder().setPokemonId(pokemon.getId()).setItemId(item).build();
            UseItemReviveResponseOuterClass.UseItemReviveResponse response = PokemonHandler.request(RequestTypeOuterClass.RequestType.USE_ITEM_REVIVE, message, UseItemReviveResponseOuterClass.UseItemReviveResponse.class);
            if (response.getResult() == UseItemReviveResponseOuterClass.UseItemReviveResponse.Result.SUCCESS) {
                pokemon.setStamina(response.getStamina());
            }
            Item useItem = PokemonHandler.API.getInventories().getItemBag().getItem(item);
            useItem.setCount(useItem.getCount() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void heal(Pokemon pokemon, ItemIdOuterClass.ItemId item) {
        try {
            UseItemPotionMessageOuterClass.UseItemPotionMessage message = UseItemPotionMessageOuterClass.UseItemPotionMessage.newBuilder().setPokemonId(pokemon.getId()).setItemId(item).build();
            UseItemPotionResponseOuterClass.UseItemPotionResponse response = PokemonHandler.request(RequestTypeOuterClass.RequestType.USE_ITEM_POTION, message, UseItemPotionResponseOuterClass.UseItemPotionResponse.class);
            if (response.getResult() == UseItemPotionResponseOuterClass.UseItemPotionResponse.Result.SUCCESS) {
                pokemon.setStamina(response.getStamina());
            }
            Item useItem = PokemonHandler.API.getInventories().getItemBag().getItem(item);
            useItem.setCount(useItem.getCount() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        S2LatLng first = S2LatLng.fromDegrees(lat1, lon1);
        S2LatLng second = S2LatLng.fromDegrees(lat2, lon2);
        return first.getEarthDistance(second);
    }

    public static <RES, MSG extends GeneratedMessage> RES request(RequestTypeOuterClass.RequestType type, MSG message, Class<RES> responseClass) throws Exception {
        AsyncServerRequest serverRequest = new AsyncServerRequest(type, message);
        ByteString bytes = AsyncHelper.toBlocking(PokemonHandler.API.getRequestHandler().sendAsyncServerRequests(serverRequest));
        return (RES) responseClass.getDeclaredMethod("parseFrom", ByteString.class).invoke(null, bytes);
    }

    public static List<NearbyPokemon> getNearbyPokemon() {
        synchronized (NEARBY_POKEMONS) {
            return NEARBY_POKEMONS;
        }
    }

    public static List<CatchablePokemon> getCatchablePokemon() {
        synchronized (CATCHABLE_POKEMON) {
            return CATCHABLE_POKEMON;
        }
    }

    public static List<CatchableRenderedPokemon> getCatchableRenderedPokemon() {
        synchronized (CATCHABLE_RENDERED_POKEMON) {
            return CATCHABLE_RENDERED_POKEMON;
        }
    }

    public static List<Gym> getGyms() {
        synchronized (GYMS) {
            return GYMS;
        }
    }

    public static List<Pokestop> getPokestops() {
        synchronized (POKESTOPS) {
            return POKESTOPS;
        }
    }

    public static void removePokemon(CatchablePokemon pokemon) {
        synchronized (CATCHABLE_POKEMON) {
            CATCHABLE_POKEMON.remove(pokemon);
        }
        PokemonHandler.updateRenderedPokemon();
    }

    private static void updateRenderedPokemon() {
        synchronized (CATCHABLE_RENDERED_POKEMON) {
            CATCHABLE_RENDERED_POKEMON.clear();
            for (CatchablePokemon pokemon : CATCHABLE_POKEMON) {
                CATCHABLE_RENDERED_POKEMON.add(new CatchableRenderedPokemon(Minecraft.getMinecraft().theWorld, pokemon, true, true));
            }
        }
    }

    public static boolean isLoggingIn() {
        return PokemonHandler.loggingIn;
    }

    public static boolean isLoginFailed() {
        return PokemonHandler.loginFailed;
    }

    public static String getUsername() {
        return PokemonHandler.username;
    }

    public static ClaimCodenameResponseOuterClass.ClaimCodenameResponse.Status setUsername(String username) throws Exception {
        ClaimCodenameMessageOuterClass.ClaimCodenameMessage message = ClaimCodenameMessageOuterClass.ClaimCodenameMessage.newBuilder().setCodename(username).build();
        ClaimCodenameResponseOuterClass.ClaimCodenameResponse response = PokemonHandler.request(RequestTypeOuterClass.RequestType.CLAIM_CODENAME, message, ClaimCodenameResponseOuterClass.ClaimCodenameResponse.class);
        if (response.getStatus() == ClaimCodenameResponseOuterClass.ClaimCodenameResponse.Status.SUCCESS) {
            PokemonHandler.username = username;
        }
        return response.getStatus();
    }
}
