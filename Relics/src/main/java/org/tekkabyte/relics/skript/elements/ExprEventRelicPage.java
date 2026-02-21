package org.tekkabyte.relics.skript.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.tekkabyte.relics.event.RelicEquipEvent;
import org.tekkabyte.relics.event.RelicUnequipEvent;

public class ExprEventRelicPage extends SimpleExpression<Number> {

    @Override
    protected Number[] get(Event e) {
        if (e instanceof RelicEquipEvent equipEvent) {
            return new Number[]{equipEvent.getPage()};
        }
        if (e instanceof RelicUnequipEvent unequipEvent) {
            return new Number[]{unequipEvent.getPage()};
        }
        return new Number[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "event relic page";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        return true;
    }
}
