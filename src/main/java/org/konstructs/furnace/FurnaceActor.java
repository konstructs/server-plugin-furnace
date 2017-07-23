package org.konstructs.furnace;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.typesafe.config.ConfigValue;
import konstructs.api.*;
import konstructs.api.messages.*;
import konstructs.plugin.Config;
import konstructs.plugin.KonstructsActor;
import konstructs.plugin.PluginConstructor;

import java.util.*;

public class FurnaceActor extends KonstructsActor {

    static BlockTypeId FURNACE = BlockTypeId.fromString("org/konstructs/furnace/furnace");

    public FurnaceActor(ActorRef universe) {
        super(universe);
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);

        System.out.println(message);
    }

    @Override
    public void onInteractTertiaryFilter(InteractTertiaryFilter filter) {
        System.out.println("foobar");
        Block blockAtPosition = filter.getMessage().getBlockAtPosition();

        // World phase is the 2nd phase where we know which world block the player has selected
        if (filter.getMessage().isWorldPhase()) {

            // Check if a block was selected, and if it's the correct one
            if (blockAtPosition != null && blockAtPosition.getType().equals(FURNACE)) {

                // Check if the block has an UUID, if not assign one
                if (blockAtPosition.getId() == null) {
                    blockAtPosition = blockAtPosition.withId(UUID.randomUUID());
                }

                // Create a props object for the child actor
                Props furnaceActorViewProps = FurnaceViewActor.props(
                        getUniverse(),
                        filter.getMessage().getSender(),
                        blockAtPosition.getId()
                );

                // Spawn the child actor, name it by the block ID
                getContext().actorOf(furnaceActorViewProps, blockAtPosition.getId().toString());
            }
        }

        // Forward the block down in the filter chain
        filter.nextWith(self(), filter.getMessage().withBlockAtPosition(blockAtPosition));
    }

    @PluginConstructor
    public static Props props(String pluginName, ActorRef universe) {
        return Props.create(FurnaceActor.class, universe);
    }
}
