# Notes from the supplied JARs

The supplied Bon Client classes expose module names/settings including:
- SUS CHUNK FINDER
- Scan Radius
- Sim Chunks
- Tuff Chunk Finder
- Repeater chunk found!
- tuff-chunk-scan
- tuff-repeater-scan

This source uses those high-level ideas only. It does not include extracted/decompiled code from the supplied JARs.

## Sus Chunk Finder / Spawner Finder

Added two more clean-room modules based on generic, widely-known
"chunk finder" style concepts (not extracted from any third-party
jar):

- `Sus Chunk Finder` - flags chunks whose underground blocks contain
  an unusual amount of player-placed materials (planks, cobblestone,
  chests, doors, redstone, etc).
- `Spawner Finder` - scans loaded chunks' block entities for mob
  spawner blocks and reports their positions.

Both are original implementations written from scratch.
