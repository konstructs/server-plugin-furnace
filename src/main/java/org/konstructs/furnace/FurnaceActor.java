package org.konstructs.furnace;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.typesafe.config.ConfigValue;
import konstructs.api.*;
import konstructs.api.messages.*;
import konstructs.plugin.Config;
import konstructs.plugin.KonstructsActor;
import konstructs.plugin.PluginConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FurnaceActor extends KonstructsActor {

    public FurnaceActor(ActorRef universe) {
        super(universe);
        universe.tell(GetBlockFactory.MESSAGE, getSelf());
    }

    @Override
    public void onReceive(Object message) {

        if(message instanceof BlockFactory) {
            // factory = (BlockFactory)message;
        } else if(message instanceof InteractTertiaryFilter) {
             InteractTertiaryFilter filter = (InteractTertiaryFilter)message;
             if (filter.getMessage().isWorldPhase()) {
                 Block block = filter.getMessage().getBlockAtPosition();
                 if (block != null) {
                     if (block.getType().equals(BlockTypeId.fromString("org/konstructs/furnace/furnace"))) {
                         System.out.println("Clicked at thing");
                         filter.getMessage().getSender().tell(new ConnectView(self(), View.EMPTY.add(new InventoryView(2, 2, 4, 4), Inventory.createEmpty(16))), self());
                     }
                 }
             }
             filter.next(self());
        } else {
            super.onReceive(message); // Handle konstructs messages
        }
    }

    @PluginConstructor
    public static Props props(String pluginName, ActorRef universe) {
        return Props.create(FurnaceActor.class, universe);
    }
}
