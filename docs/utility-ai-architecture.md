# Utility AI

`Core` owns one fight-local AI instance per bot. On each bot tick it refreshes
the opponent and combat state, updates the fight-scoped profile, and ticks the
current multi-step action. `ActionManager` only scores candidates when a new
action is needed; it does not run a selector or allocate a candidate list on
every tick.

```text
PlayerBot.tick()
  └─ Core.tick()
      ├─ DifficultyController  skill curves, reaction gates, combo tier
      ├─ OpponentProfile       observations for this opponent and fight
      ├─ ActionManager          discovers, scores, selects and remembers actions
      └─ Action.tick(lla)       existing prepare/process action lifecycle
```

## Selecting and scoring

An action is considered when it is enabled, meets its minimum skill, and is
eligible for the current selection phase. Its `requirements()` remain the
action's live execution/preparation preconditions: actions can be selected
before those preconditions are met so their existing prepare stage can move,
aim, or equip the required item.

The score is:

```text
base utility
× opponent modifier
× difficulty modifier
× history modifier
× learning modifier
```

Each factor is included in `Core.getActionUtilityDebugInfo()`. Learning uses
`0.95 + 0.10 × successRate`, so it adjusts a score by at most five percent in
either direction and cannot change skill unlocks or the configured mistake
rate. A completed action records one success or failure; selected follow-ups
also count as uses.

At skill 20, 50, and 100, the chance to choose the top-scoring action is 50%,
80%, and 98%. If the bot makes a mistake and alternatives exist, it chooses
one of the other scored actions. Reaction delay, aim interpolation, movement
prediction, and combo tier are also derived from skill; hit registration is
not randomized by skill.

## Fight-scoped adaptation

`OpponentProfile` samples jumps, lateral movement, swings, mace-fall attempts,
wind-charge use, shield use, engagement distance, and a smoothed aggression
score. It is reset when the target changes or a fight starts/ends. `ActionMemory`
and the reaction timer reset at those same boundaries, so neither profile nor
learning makes the bot permanently stronger.

Actions express opponent and history adjustments in their own utility
overrides. For example, `SlamHit` values nearby, airborne opponents and height
advantage; `Eat` is less attractive against aggressive, nearby opponents; and
`Shield` is more attractive against frequent attackers.

## Adding an action

Add a concrete class under `ai.action` with a public constructor accepting
`Core`. `ActionManager` discovers action classes in that package at startup;
Core only keeps typed references to actions used by existing combo flows. Set
the action's base utility, minimum skill, enabled state, and requirements, then
implement its existing tick stages. Skill gates in `getMinimumSkill()` control
progressive unlocking.

The scanner runs once during bot initialization. Scoring happens only when a
new action is needed, reusing the candidate list's capacity. Opponent
observation and action execution use primitive counters and values, keeping
per-tick work small.
