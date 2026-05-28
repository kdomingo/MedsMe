# MedTracker app icon

## Concept

**Tracked dose** — capsule + check + reminder arc on a teal→blue gradient. Matches the app name: medication tracking with reminders and inventory.

## Assets

| Path | Purpose |
|------|---------|
| `docs/medtracker-icon-concept.png` | Source concept (1024×1024) |
| `app/src/main/res/mipmap-*/ic_launcher.png` | Legacy launcher (API &lt; 26) |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | Adaptive icon (API 26+) |
| `app/src/main/res/drawable/ic_launcher_background.xml` | Adaptive background gradient |
| `app/src/main/res/drawable-nodpi/ic_launcher_foreground_asset.png` | Adaptive foreground |

## Colors

- Start: `#2DD4BF` (teal)
- End: `#0EA5E9` (sky blue)

## Regenerate mipmaps from concept

```bash
SRC=docs/medtracker-icon-concept.png
BASE=app/src/main/res
for spec in "48:mipmap-mdpi" "72:mipmap-hdpi" "96:mipmap-xhdpi" "144:mipmap-xxhdpi" "192:mipmap-xxxhdpi"; do
  size="${spec%%:*}"; dir="${spec##*:}"
  sips -z $size $size "$SRC" --out "$BASE/$dir/ic_launcher.png"
  cp "$BASE/$dir/ic_launcher.png" "$BASE/$dir/ic_launcher_round.png"
done
sips -z 432 432 "$SRC" --out "$BASE/drawable-nodpi/ic_launcher_foreground_asset.png"
```
