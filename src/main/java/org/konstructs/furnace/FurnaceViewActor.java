package org.konstructs.furnace;

import akka.actor.ActorRef;
import akka.actor.Props;
import konstructs.api.*;
import konstructs.api.messages.*;
import konstructs.plugin.KonstructsActor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FurnaceViewActor extends KonstructsActor {

    /*
     *   [A]         - Input
     *        [C]    - Output
     *   [B]         - Fuel
     *
     */

    private InventoryView inputView = new InventoryView(6, 2, 1, 1);
    private InventoryView fuelView = new InventoryView(2, 2, 1, 1);
    private InventoryView outputView = new InventoryView(4, 4, 1, 1);
    private Stack inputStack = null;
    private Stack fuelStack = null;
    private Stack outputStack = null;
    private Map<InventoryId, InventoryView> inventoryViewMapping = new HashMap<>();

    private ActorRef player;
    private UUID blockId;

    private static final InventoryId INV_FUEL = InventoryId.fromString("org/konstructs/FUEL");

    public FurnaceViewActor(ActorRef universe, ActorRef player, UUID blockId) {
        super(universe);

        this.player = player;
        this.blockId = blockId;

        inventoryViewMapping.put(InventoryId.INPUT, inputView);
        inventoryViewMapping.put(InventoryId.OUTPUT, outputView);
        inventoryViewMapping.put(INV_FUEL, fuelView);

        player.tell(new ConnectView(getSelf(), View.EMPTY), getSelf());
        getUniverse().tell(new GetInventoriesView(blockId, inventoryViewMapping), player);
    }

    private boolean foo(Object message) {
        if(message instanceof PutViewStack) {
            PutViewStack putViewStack = (PutViewStack)message;
            for (Map.Entry<InventoryId, InventoryView> e : inventoryViewMapping.entrySet()) {
                if (e.getValue().contains(putViewStack.to())) {
                    getUniverse().tell(new PutStackIntoSlot(blockId, e.getKey(), e.getValue().translate(putViewStack.to()), putViewStack.stack()), player);
                    getUniverse().tell(new GetInventoriesView(blockId, inventoryViewMapping), player);
                }
            }
            return true;
        } else if (message instanceof RemoveViewStack) {
            RemoveViewStack removeViewStack = (RemoveViewStack)message;
            for (Map.Entry<InventoryId, InventoryView> e : inventoryViewMapping.entrySet()) {
                if (e.getValue().contains(removeViewStack.from())) {
                    getUniverse().tell(new RemoveStackFromSlot(blockId, e.getKey(), e.getValue().translate(removeViewStack.from()), removeViewStack.amount()), player);
                    getUniverse().tell(new GetInventoriesView(blockId, inventoryViewMapping), player);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof PutViewStack && outputView.contains(((PutViewStack)message).to())) {
            player.tell(new ReceiveStack(((PutViewStack)message).stack()), getSelf());
        } else if (foo(message)) {
            // pass
        } else if(CloseInventory$.MODULE$.equals(message)) {
            getContext().stop(getSelf());
        } else {
            super.onReceive(message);
        }
    }

    public static Props props(ActorRef universe, ActorRef player, UUID blockId) {
        return Props.create(FurnaceViewActor.class, universe, player, blockId);
    }
}
