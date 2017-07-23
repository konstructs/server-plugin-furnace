package org.konstructs.furnace;

import akka.actor.ActorRef;
import akka.actor.Props;
import konstructs.api.*;
import konstructs.plugin.KonstructsActor;

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

    private ActorRef player;

    public FurnaceViewActor(ActorRef universe, ActorRef player, UUID blockId) {
        super(universe);

        this.player = player;
        player.tell(new ConnectView(self(), createView()), self());
    }

    public View createView() {
        return View.EMPTY
                .add(inputView, inputStack)
                .add(fuelView, fuelStack)
                .add(outputView, outputStack);
    }

    public void updateView() {
        player.tell(new UpdateView(createView()), getSelf());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof PutViewStack) {
            PutViewStack putViewStack = (PutViewStack)message;
            Stack stack = putViewStack.stack();
            int pos = putViewStack.to();

            if (inputView.contains(pos)) {
                if (inputStack == null) {
                    inputStack = stack;
                } else {
                    if (inputStack.canAcceptPartOf(stack)) {
                        AcceptResult<Stack> stackAcceptResult = inputStack.acceptPartOf(stack);
                        if (stackAcceptResult.getGiving() != null) {
                            player.tell(new ReceiveStack(stackAcceptResult.getGiving()), self());
                        }
                        inputStack = stackAcceptResult.getAccepting();
                    } else {
                        player.tell(new ReceiveStack(inputStack), self());
                        inputStack = stack;
                    }
                }
            } else if (outputView.contains(pos)) {
                player.tell(new ReceiveStack(stack), self());
            } else if (fuelView.contains(pos)) {
                player.tell(new ReceiveStack(stack), self()); // TODO
            }

            updateView();

        } else if(message instanceof RemoveViewStack) {
            // TODO
            RemoveViewStack removeViewStack = (RemoveViewStack)message;
            Stack sendStack = inputStack.take(removeViewStack.amount());
            player.tell(new ReceiveStack(sendStack), self());
            inputStack = inputStack.drop(removeViewStack.amount());
        } else {
            super.onReceive(message);
        }
    }

    public static Props props(ActorRef universe, ActorRef player, UUID blockId) {
        return Props.create(FurnaceViewActor.class, universe, player, blockId);
    }
}
