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
package mage.cards.b;

import java.util.UUID;
import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.counter.AddCountersSourceEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Outcome;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.GameEvent.EventType;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.TargetPlayer;
import mage.util.CardUtil;

/**
 *
 * @author L_J
 */
public class BaronVonCount extends CardImpl {

    public BaronVonCount(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{1}{B}{R}");
        addSuperType(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.VILLAIN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Baron Von Count enters the battlefield with a doom counter on "5."
        this.addAbility(new EntersBattlefieldAbility(new BaronVonCountPutCounterEffect()));

        // Whenever you cast a spell with the indicated numeral in its mana cost, text box, power, or toughness, move the doom counter one numeral to the left.
        this.addAbility(new BaronVonCountTriggeredAbility());

        // When the doom counter moves from "1," destroy target player and put that doom counter on "5."
        this.addAbility(new BaronVonCountSecondTriggeredAbility());
    }

    public BaronVonCount(final BaronVonCount card) {
        super(card);
    }

    @Override
    public BaronVonCount copy() {
        return new BaronVonCount(this);
    }
}

class BaronVonCountPutCounterEffect extends OneShotEffect {

    public BaronVonCountPutCounterEffect() {
        super(Outcome.Benefit);
        staticText = "with a doom counter on \"5.\"";
    }

    public BaronVonCountPutCounterEffect(final BaronVonCountPutCounterEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject mageObject = game.getPermanentEntering(source.getSourceId());
        if (mageObject == null) {
            mageObject = game.getObject(source.getSourceId());
        }
        if (controller != null && mageObject != null) {
            Integer doomNumber = 5;
            game.getState().setValue(mageObject.getId() + "_doom", doomNumber);
            if (mageObject instanceof Permanent) {
                ((Permanent) mageObject).addInfo("doom counter", CardUtil.addToolTipMarkTags("Doom counter at: " + doomNumber), game);
                // This isn't exactly correct - from what I understand from Maro's rulings, the game "can't see" the counter on Baron (i.e. the original counter can't be removed by Vampire Hexmage etc.), 
                // but it can still be proliferated to put an additional doom counter on itself (the new counters can be removed and aren't placed on the "numbers" - i.e. they don't influence the card's 
                // functionality in any direct way). To simplify things, I merely put a do-nothing Doom counter on Baron that can be proliferated, etc., in addition to the value that tracks the 
                // the placement of the functional "counter". This only has fringe incorrect interactions with a few cards like Thief of Blood which now gets an extra counter from Baron.
                new AddCountersSourceEffect(CounterType.DOOM.createInstance()).apply(game, source);
            }
            return true;
        }
        return false;
    }

    @Override
    public BaronVonCountPutCounterEffect copy() {
        return new BaronVonCountPutCounterEffect(this);
    }
}

class BaronVonCountTriggeredAbility extends TriggeredAbilityImpl {

    public BaronVonCountTriggeredAbility() {
        super(Zone.BATTLEFIELD, new BaronVonCountMoveDoomCounterEffect());
    }

    public BaronVonCountTriggeredAbility(final BaronVonCountTriggeredAbility abiltity) {
        super(abiltity);
    }

    @Override
    public BaronVonCountTriggeredAbility copy() {
        return new BaronVonCountTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getPlayerId().equals(this.getControllerId())) {
            Permanent sourcePermanent = game.getPermanent(getSourceId());
            MageObject mageObject = game.getObject(getSourceId());
            Spell spell = game.getStack().getSpell(event.getTargetId());
            if (game.getState().getValue(mageObject.getId() + "_doom") == null) {
                return false;
            }
            Integer doomNumber = (Integer) game.getState().getValue(mageObject.getId() + "_doom");
            if (spell != null && sourcePermanent != null && mageObject != null && doomNumber > 0) {
                if (!spell.isFaceDown(game)) {
                    String doomString = doomNumber.toString();
                    if (spell.getCard().getManaCost().getText().contains(doomString) 
                            || String.valueOf(spell.getPower().getBaseValue()).contains(doomString) 
                            || String.valueOf(spell.getToughness().getBaseValue()).contains(doomString)) {
                        return true;
                    } else {
                        for (String string : spell.getCard().getRules()) {
                            if (string.contains(doomString)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getRule() {
        return "Whenever you cast a spell with the indicated numeral in its mana cost, text box, power, or toughness, " + super.getRule();
    }
}

class BaronVonCountMoveDoomCounterEffect extends OneShotEffect {

    public BaronVonCountMoveDoomCounterEffect() {
        super(Outcome.Neutral);
        staticText = "move the doom counter one numeral to the left";
    }

    public BaronVonCountMoveDoomCounterEffect(final BaronVonCountMoveDoomCounterEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent sourcePermanent = game.getPermanent(source.getSourceId());
        MageObject mageObject = game.getObject(source.getSourceId());
        if (controller != null && sourcePermanent != null && mageObject != null) {
            if (game.getState().getValue(mageObject.getId() + "_doom") == null) {
                return false;
            }
            Integer doomNumber = (Integer) game.getState().getValue(mageObject.getId() + "_doom");
            if (doomNumber == 1) {
                // not completely sure if counter should be moving here or not (relevant in case the second trigger gets countered)
                game.fireEvent(GameEvent.getEvent(GameEvent.EventType.CUSTOM_EVENT, source.getSourceId(), source.getSourceId(), controller.getId(), "DoomCounterReset", 1));
            }
            if (doomNumber > 0) {
                doomNumber--;
                game.getState().setValue(mageObject.getId() + "_doom", doomNumber);
                ((Permanent) mageObject).addInfo("doom counter", CardUtil.addToolTipMarkTags("Doom counter at: " + doomNumber), game);
            }
            return true;
        }
        return false;
    }

    @Override
    public BaronVonCountMoveDoomCounterEffect copy() {
        return new BaronVonCountMoveDoomCounterEffect(this);
    }
}

class BaronVonCountSecondTriggeredAbility extends TriggeredAbilityImpl {

    public BaronVonCountSecondTriggeredAbility() {
        super(Zone.BATTLEFIELD, new BaronVonCountDestroyPlayerEffect());
        this.addTarget(new TargetPlayer());
    }

    public BaronVonCountSecondTriggeredAbility(BaronVonCountSecondTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == EventType.CUSTOM_EVENT;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return event.getData().equals("DoomCounterReset") && event.getTargetId().equals(this.getSourceId());
    }

    @Override
    public BaronVonCountSecondTriggeredAbility copy() {
        return new BaronVonCountSecondTriggeredAbility(this);
    }

    @Override
    public String getRule() {
        return "When the doom counter moves from \"1,\" " + super.getRule();
    }
}

class BaronVonCountDestroyPlayerEffect extends OneShotEffect {

    public BaronVonCountDestroyPlayerEffect() {
        super(Outcome.Neutral);
        staticText = "destroy target player and put that doom counter on \"5.\"";
    }

    public BaronVonCountDestroyPlayerEffect(final BaronVonCountDestroyPlayerEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player targetPlayer = game.getPlayer(source.getFirstTarget());
        if (targetPlayer != null && targetPlayer.canLose(game)) {
            game.informPlayers(targetPlayer.getLogName() + " was destroyed");
            targetPlayer.lost(game); // double checks canLose, but seems more future-proof than lostForced
        }
        Permanent sourcePermanent = game.getPermanent(source.getSourceId());
        MageObject mageObject = game.getObject(source.getSourceId());
        if (sourcePermanent != null && mageObject != null) {
            if (game.getState().getValue(mageObject.getId() + "_doom") == null) {
                return false;
            }
            Integer doomNumber = 5;
            game.getState().setValue(mageObject.getId() + "_doom", doomNumber);
            ((Permanent) mageObject).addInfo("doom counter", CardUtil.addToolTipMarkTags("Doom counter at: " + doomNumber), game);
            return true;
        }
        return false;
    }

    @Override
    public BaronVonCountDestroyPlayerEffect copy() {
        return new BaronVonCountDestroyPlayerEffect(this);
    }
}
