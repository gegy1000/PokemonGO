package net.gegy1000.pokemon.pokemon;

import POGOProtos.Enums.PokemonFamilyIdOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.pokemon.PokemonGO;
import net.gegy1000.earth.server.util.TempFileUtil;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PokemonSpriteHandler {
    private static final Map<ItemIdOuterClass.ItemId, ResourceLocation> ITEM_TEXTURES = new HashMap<>();
    private static final Map<PokemonFamilyIdOuterClass.PokemonFamilyId, ResourceLocation> CANDY_TEXTURES = new HashMap<>();

    private static final Map<Integer, AdvancedDynamicTexture> POKEMON_SPRITES = new HashMap<>();
    private static final Map<Integer, BufferedImage> DOWNLOADED_POKEMON_SPRITES = new HashMap<>();

    public static void load() {
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
        for (PokemonFamilyIdOuterClass.PokemonFamilyId family : PokemonFamilyIdOuterClass.PokemonFamilyId.values()) {
            if (family != PokemonFamilyIdOuterClass.PokemonFamilyId.UNRECOGNIZED) {
                CANDY_TEXTURES.put(family, new ResourceLocation(PokemonGO.MODID, "textures/items/" + family.name().replaceAll("FAMILY_", "").toLowerCase(Locale.ENGLISH)));
            }
        }
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

    public static AdvancedDynamicTexture get(PokemonIdOuterClass.PokemonId pokemon) {
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

    public static ResourceLocation get(ItemIdOuterClass.ItemId item) {
        ResourceLocation texture = ITEM_TEXTURES.get(item);
        return texture == null ? TextureMap.LOCATION_MISSING_TEXTURE : texture;
    }

    public static ResourceLocation get(PokemonFamilyIdOuterClass.PokemonFamilyId candy) {
        ResourceLocation texture = CANDY_TEXTURES.get(candy);
        return texture == null ? TextureMap.LOCATION_MISSING_TEXTURE : texture;
    }
}
