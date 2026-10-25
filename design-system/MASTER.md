# VolunteerHub design system

Source of truth for the Android UI. Code lives in `android/app/src/main/java/com/example/volunteerhub/ui/`.
Direction: playful, tactile and high-energy for ages 14-18, in the spirit of Duolingo and Brilliant
(chunky pressable surfaces, saturated role-based colour, one clear action per card, progress you can feel).

## Principles
1. **One obvious action per surface.** Cards lead with cause + date, then title, place, credit, and a single primary button.
2. **Tactile.** Cards and buttons sit on a 4dp darker "lip" and sink when pressed (`ChunkyCard`, `ChunkyButton`).
3. **Colour has a job, and never works alone.** Every colour is paired with an icon or text (cause pills, status pills, badges).
4. **Honest.** Hours are *planned* on sign-up and *verified* only after a supervisor signs. Sample data is always labelled.
5. **Accessible by default.** 48dp targets, 4.5:1 text contrast, spoken labels on icon buttons, reduced-motion respected.

## Tokens (`ui/theme/Color.kt`, `Dimens.kt`)
| Role | Light | Use |
|---|---|---|
| Violet `#6D3BEA` / deep `#4A22B8` | brand | primary actions, goal card |
| Sun `#FFC700` | reward | hours credit, XP, streak |
| Mint `#14C58C` / deep `#067A55` | success | verified, in-plan |
| Coral `#FF5A4F` / deep `#C93329` | urgent | happening soon, errors |
| Sky `#1CB0F6` / deep `#0F6FA3` | info | tech cause, sample banner |
| Cream `#FFF8EC` · Ink `#1B1535` · Ink-soft `#5B5578` | neutrals | canvas, text |

Dark mode uses a deep-indigo canvas (`#14102B`) with the same hues. Semantic colours outside Material's scheme are exposed through `Hub.colors`.

Spacing 4/8/12/16/24/32 · radii chip 14, button 16, card 20 · lip 4dp · min touch 48dp.

## Type (`ui/theme/Type.kt`)
Nunito (variable, rounded) for headings, numbers and buttons; Inter (variable) for body copy. Both are bundled in `res/font/`.

## Causes
Each event maps to one of six causes, each with its own colour **and** icon: Tech & Coding, Health, Nature, Teaching & Books, Food & Community, Business & Money.

## Components (`ui/components/`)
`ChunkyCard`, `ChunkyButton` (Primary/Reward/Success/Danger/Quiet), `SelectChip`, `StatusPill`, `CausePill`, `XpBadge`,
`ProgressRing`, `LinearGoalBar`, `BadgeTile`/`BadgeShelf`, `EventCard`, `EmptyState`, `ErrorState`, `ShimmerCard`, `AppTopBar`, `IconAction`.

## Motion
Springs for press and progress, 120-180ms fades between screens, confetti on a verified shift.
Everything animated checks `rememberReduceMotion()` (system animation scale = 0) and falls back to instant changes.

## Navigation
Home · Discover · My plan · Profile, plus Detail, Check-in, Signature and Transcript screens on a back stack (system Back works everywhere).
