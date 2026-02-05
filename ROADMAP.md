StarChord Roadmap
==============


GOALS
-----------

The current main goal of StarChord is to extend it as to be a fully nomad on-the-run performance keyboard for diverse instruments, 
which would particularly fit street performers.

Longer term goals would have StarChord be a full midi keyboard and synthesizer, 
which just begins to be possible with the very recent (as of 2013) efforts of Google with low-latency Android Audio.

Anyway if you have any other idea of a feature or any code you would like to propose to the project, 
feel free to post in the Github Issues or Fork+Pull Request!


TODO
---------

Here is a list of a few features that are planned in near-term:

- piano HD (3 sounds every 4 are interpolated by the program currently! and in mono)
- a new free orchestral soundbank (maybe as an external module to keep the StarChord low in size)
- Connection to computer (via USB or OSC or Bluetooth) with velocity sensitivity support ( see low latency real-time sensors streaming over udp https://github.com/umer0586/SensaGram )
- background sound (in menu, add three buttons: load, play and loop, the last two changing to stop and pause when playing)
- external audio effects via gyrometer/accelerometer and send by USB/OSC/Bluetooth (mapping variable CC messages like Expression, and maybe could map independently 2 different CC functions for the two dimensions x and y).

MAYBE/ONE DAY
-------------

- multi-screens keyboards, so that you could quickly swap between different (multi-)instruments mapping by just swiping on the screen.
- Interactive tutorials (one for each keyboard) to know how to play major scales, minor, change root note, etc...
- Midi tutorial: load a midi file and the keys to press will be highlighted on the screen.
- internal audio effects by using an (external?) realtime audio effects processing library (eg: Flanger, Filter Enveloppe which could be used to simulate Expression, etc.).
- External audio anti-Jitter and consistency technologies: external audio constant latency (store timestamp of previous note with the note in the net packet + delta timestamp from previous so that on PC we can find previous timestamp and then with delta know when to play next + compute the new timestamp of this note to use for future notes) to avoid jitter + redundancy (send multiple times the same note, they will be dismissed based on their timestamp if exactly equal) to avoid missing notes (when you press a key and because of network packets drops your note isn't transmitted onto the PC).
- refactor setUpBoard() to use as arguments only two vectors (length being the pitch change)  and directions of those vectors to construct generically an hexagon board.
- full board option: draw not only the keys visible on-screen, but any possible key, and let the user move the screen wherever he wants to. This is possible and should not use much more resources since instruments load all notes sounds anyway, whether the note is visible or not.

DONE
---------

- Modifier / CC keys, first implemented being Sustain.
- ability to add soundbanks on sdcard directly without recompiling the program
- enhancing precision using gaps between keys (called Key Touch Surface)
- note overlap (trigger multiple neighbour keys = chord with one touch) using Key Touch Surface
- better quality piano sound
- Velocity sensitivity (via touch surface for capacitive screens and via real pressure on resistive screens) with auto calibration and may use different sound for different velocities of one midi note (real velocity) or just scale the sound volume if only one velocity is available (fake velocity)
- Multi-instruments feature: ability to map multiple instruments on the keyboard (eg: the first 2 rows will be piano, then 2 rows of violin, etc...)

NOT TO DO
---------
- StarChord used as a host for an external Midi keyboard. I see no objection if someone contributes such a code, but it's not part of the roadmap, there are plenty of other softwares that can do that, and the main purpose of StarChord is the keyboard interface.
