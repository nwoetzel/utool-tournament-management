# Adding support for communicating with the dummy plugin #

The Dummy Plugin is a basic UI provided by the UTooL Core for displaying tournament information when a plugin isn't installed. It provides functionality for displaying text and an image to the user.

**NOTE: Keep your dummy plugin images relatively small. Currently, they will be transmitted to all connected users, even those using a real plugin.**

Sending data to the dummy plugin is as easy as sending a DummyMessage with your text, image, or both embedded inside. This class is provided by the UTooL Library for your convenience.