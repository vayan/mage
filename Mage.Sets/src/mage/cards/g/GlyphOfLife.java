/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.g;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateDelayedTriggeredAbilityEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.mageobject.SubtypePredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.DamagedCreatureEvent;
import mage.game.events.GameEvent.EventType;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCreaturePermanent;

/**
 *
 * @author fireshoes & L_J
 */
public class GlyphOfLife extends CardImpl {

    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("Wall creature");

    static {
        filter.add(new SubtypePredicate(SubType.WALL));
    }

    public GlyphOfLife(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.INSTANT},"{W}");

        // Choose target Wall creature. Whenever that creature is dealt damage by an attacking creature this turn, you gain that much life.
        this.getSpellAbility().addTarget(new TargetCreaturePermanent(filter));
        this.getSpellAbility().addEffect(new InfoEffect("Choose target Wall creature"));
        this.getSpellAbility().addEffect(new CreateDelayedTriggeredAbilityEffect(new GlyphOfLifeTriggeredAbility()));
    }

    public GlyphOfLife(final GlyphOfLife card) {
        super(card);
    }

    @Override
    public GlyphOfLife copy() {
        return new GlyphOfLife(this);
    }
}

class GlyphOfLifeTriggeredAbility extends DelayedTriggeredAbility {

    public GlyphOfLifeTriggeredAbility() {
        super(new GlyphOfLifeGainLifeEffect(), Duration.EndOfTurn, false);
    }

    public GlyphOfLifeTriggeredAbility(final GlyphOfLifeTriggeredAbility effect) {
        super(effect);
    }

    @Override
    public GlyphOfLifeTriggeredAbility copy() {
        return new GlyphOfLifeTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == EventType.DAMAGED_CREATURE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getTargetId().equals(this.getFirstTarget())) {
            DamagedCreatureEvent damageEvent = (DamagedCreatureEvent) event;
            Permanent attackingCreature = game.getPermanentOrLKIBattlefield(damageEvent.getSourceId());
            if (attackingCreature != null && attackingCreature.isCreature() && attackingCreature.isAttacking()) {
                this.getEffects().get(0).setValue("damageAmount", event.getAmount());
                return true;
            }
        }
        return false;
    }

    @Override
    public String getRule() {
        return "Whenever that creature is dealt damage by an attacking creature this turn, " + super.getRule();
    }
}

class GlyphOfLifeGainLifeEffect extends OneShotEffect {

    public GlyphOfLifeGainLifeEffect() {
        super(Outcome.GainLife);
        staticText = "you gain that much life";
    }

    public GlyphOfLifeGainLifeEffect(final GlyphOfLifeGainLifeEffect effect) {
        super(effect);
    }

    @Override
    public GlyphOfLifeGainLifeEffect copy() {
        return new GlyphOfLifeGainLifeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player != null) {
            player.gainLife((Integer) this.getValue("damageAmount"), game);
        }
        return true;
    }

}
