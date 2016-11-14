package net.gegy1000.pokemon.client.util;

import POGOProtos.Inventory.Item.ItemIdOuterClass;
import POGOProtos.Networking.Requests.Messages.CheckChallenge;
import POGOProtos.Networking.Requests.Messages.ClaimCodenameMessageOuterClass;
import POGOProtos.Networking.Requests.Messages.UseItemPotionMessageOuterClass;
import POGOProtos.Networking.Requests.Messages.UseItemReviveMessageOuterClass;
import POGOProtos.Networking.Requests.Messages.VerifyChallenge;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import POGOProtos.Networking.Responses.CheckChallengeResponseOuterClass;
import POGOProtos.Networking.Responses.ClaimCodenameResponseOuterClass;
import POGOProtos.Networking.Responses.UseItemPotionResponseOuterClass;
import POGOProtos.Networking.Responses.UseItemReviveResponseOuterClass;
import POGOProtos.Networking.Responses.VerifyChallengeResponseOuterClass;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.main.AsyncServerRequest;
import com.pokegoapi.util.AsyncHelper;

import java.net.URL;

public class PokemonRequestHandler {
    public static <RES, MSG extends GeneratedMessage> RES request(RequestTypeOuterClass.RequestType type, MSG message, Class<RES> responseClass) throws Exception {
        AsyncServerRequest serverRequest = new AsyncServerRequest(type, message);
        ByteString bytes = AsyncHelper.toBlocking(PokemonHandler.API.getRequestHandler().sendAsyncServerRequests(serverRequest));
        return (RES) responseClass.getDeclaredMethod("parseFrom", ByteString.class).invoke(null, bytes);
    }

    public static URL getChallenge() throws Exception {
        CheckChallenge.CheckChallengeMessage message = CheckChallenge.CheckChallengeMessage.newBuilder().build();
        CheckChallengeResponseOuterClass.CheckChallengeResponse response = PokemonRequestHandler.request(RequestTypeOuterClass.RequestType.CHECK_CHALLENGE, message, CheckChallengeResponseOuterClass.CheckChallengeResponse.class);
        if (response.getShowChallenge()) {
            return new URL(response.getChallengeUrl());
        }
        return null;
    }

    public static boolean verify(String token) throws Exception {
        VerifyChallenge.VerifyChallengeMessage message = VerifyChallenge.VerifyChallengeMessage.newBuilder().setToken(token).build();
        VerifyChallengeResponseOuterClass.VerifyChallengeResponse response = PokemonRequestHandler.request(RequestTypeOuterClass.RequestType.VERIFY_CHALLENGE, message, VerifyChallengeResponseOuterClass.VerifyChallengeResponse.class);
        return response.getSuccess();
    }

    public static ClaimCodenameResponseOuterClass.ClaimCodenameResponse.Status setUsername(String username) throws Exception {
        ClaimCodenameMessageOuterClass.ClaimCodenameMessage message = ClaimCodenameMessageOuterClass.ClaimCodenameMessage.newBuilder().setCodename(username).build();
        ClaimCodenameResponseOuterClass.ClaimCodenameResponse response = PokemonRequestHandler.request(RequestTypeOuterClass.RequestType.CLAIM_CODENAME, message, ClaimCodenameResponseOuterClass.ClaimCodenameResponse.class);
        if (response.getStatus() == ClaimCodenameResponseOuterClass.ClaimCodenameResponse.Status.SUCCESS) {
            PokemonHandler.username = username;
        }
        return response.getStatus();
    }

    public static void revive(Pokemon pokemon, ItemIdOuterClass.ItemId item) {
        try {
            UseItemReviveMessageOuterClass.UseItemReviveMessage message = UseItemReviveMessageOuterClass.UseItemReviveMessage.newBuilder().setPokemonId(pokemon.getId()).setItemId(item).build();
            UseItemReviveResponseOuterClass.UseItemReviveResponse response = PokemonRequestHandler.request(RequestTypeOuterClass.RequestType.USE_ITEM_REVIVE, message, UseItemReviveResponseOuterClass.UseItemReviveResponse.class);
            if (response.getResult() == UseItemReviveResponseOuterClass.UseItemReviveResponse.Result.SUCCESS) {
                pokemon.setStamina(response.getStamina());
            }
            Item useItem = PokemonHandler.API.getInventories().getItemBag().getItem(item);
            useItem.setCount(useItem.getCount() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void heal(Pokemon pokemon, ItemIdOuterClass.ItemId item) {
        try {
            UseItemPotionMessageOuterClass.UseItemPotionMessage message = UseItemPotionMessageOuterClass.UseItemPotionMessage.newBuilder().setPokemonId(pokemon.getId()).setItemId(item).build();
            UseItemPotionResponseOuterClass.UseItemPotionResponse response = PokemonRequestHandler.request(RequestTypeOuterClass.RequestType.USE_ITEM_POTION, message, UseItemPotionResponseOuterClass.UseItemPotionResponse.class);
            if (response.getResult() == UseItemPotionResponseOuterClass.UseItemPotionResponse.Result.SUCCESS) {
                pokemon.setStamina(response.getStamina());
            }
            Item useItem = PokemonHandler.API.getInventories().getItemBag().getItem(item);
            useItem.setCount(useItem.getCount() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void useIncense(ItemIdOuterClass.ItemId item) {
        try {
            PokemonHandler.API.getInventories().getItemBag().useIncense(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
