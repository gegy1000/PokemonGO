package net.gegy1000.pokemon.client.util;

import POGOProtos.Data.PokemonDataOuterClass;
import POGOProtos.Inventory.Item.ItemIdOuterClass;
import com.pokegoapi.api.pokemon.Pokemon;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PokemonUtils {
    private static final int MAX_STATISTIC = 15;

    private static final Map<ItemIdOuterClass.ItemId, Function<PokemonViewGUI, Void>> ITEM_ACTIONS = new HashMap<>();

    public static void onPreInit() {
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_REVIVE, (gui) -> {
            PokemonGUIHandler.openReviveWindow(gui, ItemIdOuterClass.ItemId.ITEM_REVIVE);
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_MAX_REVIVE, (gui) -> {
            PokemonGUIHandler.openReviveWindow(gui, ItemIdOuterClass.ItemId.ITEM_REVIVE);
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_POTION, (gui) -> {
            PokemonGUIHandler.openHealWindow(gui, ItemIdOuterClass.ItemId.ITEM_POTION);
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_SUPER_POTION, (gui) -> {
            PokemonGUIHandler.openHealWindow(gui, ItemIdOuterClass.ItemId.ITEM_SUPER_POTION);
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_HYPER_POTION, (gui) -> {
            PokemonGUIHandler.openHealWindow(gui, ItemIdOuterClass.ItemId.ITEM_HYPER_POTION);
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_MAX_POTION, (gui) -> {
            PokemonGUIHandler.openHealWindow(gui, ItemIdOuterClass.ItemId.ITEM_MAX_POTION);
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_INCENSE_ORDINARY, (gui) -> {
            PokemonHandler.addTask(() -> {
                PokemonRequestHandler.useIncense(ItemIdOuterClass.ItemId.ITEM_INCENSE_ORDINARY);
                return null;
            });
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_INCENSE_COOL, (gui) -> {
            PokemonHandler.addTask(() -> {
                PokemonRequestHandler.useIncense(ItemIdOuterClass.ItemId.ITEM_INCENSE_COOL);
                return null;
            });
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_INCENSE_FLORAL, (gui) -> {
            PokemonHandler.addTask(() -> {
                PokemonRequestHandler.useIncense(ItemIdOuterClass.ItemId.ITEM_INCENSE_FLORAL);
                return null;
            });
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_INCENSE_SPICY, (gui) -> {
            PokemonHandler.addTask(() -> {
                PokemonRequestHandler.useIncense(ItemIdOuterClass.ItemId.ITEM_INCENSE_SPICY);
                return null;
            });
            return null;
        });
        ITEM_ACTIONS.put(ItemIdOuterClass.ItemId.ITEM_LUCKY_EGG, (gui) -> {
            PokemonHandler.addTask(() -> {
                try {
                    PokemonHandler.API.getInventories().getItemBag().useLuckyEgg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
            return null;
        });
    }

    public static Function<PokemonViewGUI, Void> getItemAction(ItemIdOuterClass.ItemId item) {
        return ITEM_ACTIONS.get(item);
    }

    public static double calculateIV(Pokemon pokemon) {
        int totalStatistic = pokemon.getIndividualAttack() + pokemon.getIndividualDefense() + pokemon.getIndividualStamina();
        return (totalStatistic / (double) (MAX_STATISTIC * 3)) * 100;
    }

    public static double calculateIV(PokemonDataOuterClass.PokemonData data) {
        int totalStatistic = data.getIndividualAttack() + data.getIndividualDefense() + data.getIndividualStamina();
        return (totalStatistic / (double) (MAX_STATISTIC * 3)) * 100;
    }
}
