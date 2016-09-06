package net.gegy1000.pokemon.client.util;

import POGOProtos.Data.Battle.BattleActionOuterClass;
import POGOProtos.Data.Battle.BattleActionTypeOuterClass;
import POGOProtos.Data.Battle.BattleLogOuterClass;
import POGOProtos.Data.Battle.BattlePokemonInfoOuterClass;
import POGOProtos.Data.Battle.BattleStateOuterClass;
import POGOProtos.Networking.Requests.Messages.AttackGymMessageOuterClass;
import POGOProtos.Networking.Requests.Messages.StartGymBattleMessageOuterClass;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import POGOProtos.Networking.Responses.AttackGymResponseOuterClass;
import POGOProtos.Networking.Responses.StartGymBattleResponseOuterClass;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.gym.Gym;
import com.pokegoapi.api.pokemon.Pokemon;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GymBattle {
    private PokemonGo api;
    private Gym gym;
    private Pokemon[] team;
    private String battleId;
    private boolean inProgress;
    private BattleStateOuterClass.BattleState state;

    private int targetIndex;

    private long serverTimeOffset;

    private StartGymBattleResponseOuterClass.StartGymBattleResponse startResponse;
    private BattlePokemonInfoOuterClass.BattlePokemonInfo activeAttacker;
    private BattlePokemonInfoOuterClass.BattlePokemonInfo activeDefender;
    private AttackGymResponseOuterClass.AttackGymResponse lastResponse;

    private final List<BattleActionOuterClass.BattleAction> sendActionQueue = new ArrayList<>();

    public GymBattle(PokemonGo api, Gym gym, Pokemon[] team) {
        this.api = api;
        this.gym = gym;
        this.team = team;
    }

    public StartGymBattleResponseOuterClass.StartGymBattleResponse start() throws Exception {
        StartGymBattleMessageOuterClass.StartGymBattleMessage.Builder builder = StartGymBattleMessageOuterClass.StartGymBattleMessage.newBuilder();
        for (Pokemon pokemon : this.team) {
            builder.addAttackingPokemonIds(pokemon.getId());
        }
        builder.setGymId(this.gym.getId());
        builder.setPlayerLatitude(this.api.getLatitude());
        builder.setPlayerLongitude(this.api.getLongitude());
        builder.setDefendingPokemonId(this.gym.getDefendingPokemon().get(0).getId());
        this.startResponse = PokemonHandler.request(RequestTypeOuterClass.RequestType.START_GYM_BATTLE, builder.build(), StartGymBattleResponseOuterClass.StartGymBattleResponse.class);
        this.battleId = this.startResponse.getBattleId();
        if (this.startResponse.getResult() == StartGymBattleResponseOuterClass.StartGymBattleResponse.Result.SUCCESS) {
            this.inProgress = true;
        }
        this.performActions(new ArrayList<>());
        this.targetIndex += this.lastResponse.getBattleLog().getBattleActionsCount();
        return this.startResponse;
    }

    public AttackGymResponseOuterClass.AttackGymResponse performActions(List<BattleActionOuterClass.BattleAction> actions) throws Exception {
        System.out.println("Performing actions " + actions);
        AttackGymMessageOuterClass.AttackGymMessage.Builder builder = AttackGymMessageOuterClass.AttackGymMessage.newBuilder();
        builder.setPlayerLatitude(this.api.getLatitude());
        builder.setPlayerLongitude(this.api.getLongitude());
        builder.setGymId(this.gym.getId());
        builder.setBattleId(this.battleId);
        builder.addAllAttackActions(actions);
        BattleActionOuterClass.BattleAction lastAction = this.getLastAction();
        if (lastAction != null) {
            builder.setLastRetrievedActions(lastAction);
        }
        this.lastResponse = PokemonHandler.request(RequestTypeOuterClass.RequestType.ATTACK_GYM, builder.build(), AttackGymResponseOuterClass.AttackGymResponse.class);
        this.activeAttacker = this.lastResponse.getActiveAttacker();
        this.activeDefender = this.lastResponse.getActiveDefender();
        this.state = this.lastResponse.getBattleLog().getState();
        this.battleId = this.lastResponse.getBattleId();
        this.serverTimeOffset = this.lastResponse.getBattleLog().getServerMs() - System.currentTimeMillis();
        System.out.println("Result: " + this.lastResponse.getResult());
        System.out.println();
        if (this.state == BattleStateOuterClass.BattleState.DEFEATED || this.state == BattleStateOuterClass.BattleState.VICTORY || this.state == BattleStateOuterClass.BattleState.TIMED_OUT) {
            this.inProgress = false;
        }
        return this.lastResponse;
    }

    public void addActionToQueue(BattleActionOuterClass.BattleAction.Builder action) {
        synchronized (this.sendActionQueue) {
            this.sendActionQueue.add(action.build());
        }
    }

    public AttackGymResponseOuterClass.AttackGymResponse sendQueuedActions() throws Exception {
        List<BattleActionOuterClass.BattleAction> actions;
        synchronized (this.sendActionQueue) {
            actions = new ArrayList<>(this.sendActionQueue);
            this.sendActionQueue.clear();
        }
        return this.performActions(actions);
    }

    private BattleActionOuterClass.BattleAction getLastAction() {
        if (this.lastResponse == null) {
            return null;
        }
        BattleLogOuterClass.BattleLog log = this.lastResponse.getBattleLog();
        if (log.getBattleActionsCount() == 0) {
            return null;
        }
        return log.getBattleActions(log.getBattleActionsCount() - 1);
    }

    public boolean inProgress() {
        return this.inProgress;
    }

    public BattlePokemonInfoOuterClass.BattlePokemonInfo getActiveAttacker() {
        return this.activeAttacker;
    }

    public BattlePokemonInfoOuterClass.BattlePokemonInfo getActiveDefender() {
        return this.activeDefender;
    }

    public long getServerTime() {
        return this.getServerTime(PokemonHandler.API.currentTimeMillis());
    }

    public long getServerTime(long clientTime) {
        return this.serverTimeOffset + clientTime;
    }

    public BattleStateOuterClass.BattleState getState() {
        return this.state;
    }

    public void end() {
        if (this.inProgress) {
            this.inProgress = false;
            try {
                BattleActionOuterClass.BattleAction.Builder builder = BattleActionOuterClass.BattleAction.newBuilder();
                builder.setType(BattleActionTypeOuterClass.BattleActionType.ACTION_PLAYER_QUIT);
                this.addActionToQueue(builder);
                this.sendQueuedActions();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
