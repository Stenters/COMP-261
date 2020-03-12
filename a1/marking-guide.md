[q]Minimum: 30[/q]
[q]Core: 35[/q]
[q]Completion: 20[/q]
[q]Challenge: 15[/q]

Minimum out of 30:
* [15] The program reads the data and draws a map.
* [15] It constructs a graph structure using collections of stops, trips, and connections.

Core out of 35 (up to 65):
* [15] The map can be zoomed and panned, and the user can select stops with the mouse,
and some details are shown.
* [10] Trip ids can be entered into the text box and the whole trip is highlighted on the map.
Can be either exact or prefix matches, check that all connections are highlighted.
* [5] The ids of all trips joining the selected stop are outputted, without duplicates.
* [5] Getting from stop to connection to trip should be constant time.

Completion out of 20 (up to 85):
* [15] Correct code for a trie structure, with methods to add an element and find all elements
with a given prefix.
* [5] Trie is used to output the id of all trips which match a prefix in the search box, as well
as highlighting them on the map. If the prefix exactly matches a trip id, only output and
highlight trip(s) of that id.

Challenge out of 15 (up to 100 max):
* [5] A quad-tree structure is used to quickly find a stop near a mouse click.
* [5] The quad-tree retrieves the closest stop in all cases.
* [5] The JourneyPlanner.GUI is improved, with one of mouse-based panning and zooming or adding a drop-down
suggestion box to the search bar.

Final mark calculation:

[q]Minimum: 30[/q] + [q]Core: 35[/q] + [q]Completion: 20[/q] + [q]Challenge: 15[/q]