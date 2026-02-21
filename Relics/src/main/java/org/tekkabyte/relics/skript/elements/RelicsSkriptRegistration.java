package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tekkabyte.relics.event.RelicEquipEvent;
import org.tekkabyte.relics.event.RelicUnequipEvent;

public final class RelicsSkriptRegistration {

    static {
        Skript.registerEvent("Relic Equip", SimpleEvent.class, RelicEquipEvent.class, "[relic] equip");
        Skript.registerEvent("Relic Unequip", SimpleEvent.class, RelicUnequipEvent.class, "[relic] unequip");

        EventValues.registerEventValue(RelicEquipEvent.class, Player.class, new Getter<>() {
            @Override
            public Player get(RelicEquipEvent event) {
                return event.getPlayer();
            }
        }, 0);
        EventValues.registerEventValue(RelicUnequipEvent.class, Player.class, new Getter<>() {
            @Override
            public Player get(RelicUnequipEvent event) {
                return event.getPlayer();
            }
        }, 0);
        EventValues.registerEventValue(RelicEquipEvent.class, ItemStack.class, new Getter<>() {
            @Override
            public ItemStack get(RelicEquipEvent event) {
                return event.getRelicItem();
            }
        }, 0);
        EventValues.registerEventValue(RelicUnequipEvent.class, ItemStack.class, new Getter<>() {
            @Override
            public ItemStack get(RelicUnequipEvent event) {
                return event.getRelicItem();
            }
        }, 0);

        Skript.registerEffect(EffTagRelic.class, "tag %itemstack% as relic with id %string%");
        Skript.registerEffect(EffSaveRelicTemplate.class,
                "save %itemstack% to relic templates",
                "set %itemstack% as relic with id %string% and save [it] to relic templates");
        Skript.registerEffect(EffOpenRelicsGui.class, "open relics gui for %player%");
        Skript.registerEffect(EffSetActiveRelic.class,
                "activate %itemstack% relic for %player% at page %number% slot %number%",
                "deactivate relic for %player% at page %number% slot %number%");
        Skript.registerCondition(CondIsRelic.class, "%itemstack% is relic", "%itemstack% is not relic");
        Skript.registerCondition(CondHasActiveRelic.class,
                "%player% has [an] active relic %string%",
                "%player% does(n't| not) have [an] active relic %string%");
        Skript.registerExpression(ExprRelicId.class, String.class, ExpressionType.SIMPLE, "relic id of %itemstack%");
        Skript.registerExpression(ExprActiveRelicIds.class, String.class, ExpressionType.SIMPLE, "active relic ids of %player%");
        Skript.registerExpression(ExprActiveRelicAtSlot.class, ItemStack.class, ExpressionType.SIMPLE,
                "active relic at page %number% slot %number% of %player%");
        Skript.registerExpression(ExprEventRelicId.class, String.class, ExpressionType.SIMPLE, "event-relic-id");
        Skript.registerExpression(ExprEventRelicPage.class, Number.class, ExpressionType.SIMPLE, "event-relic-page");
        Skript.registerExpression(ExprEventRelicSlot.class, Number.class, ExpressionType.SIMPLE, "event-relic-slot");
    }

    private RelicsSkriptRegistration() {
    }
}
