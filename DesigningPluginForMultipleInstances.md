# Designing the plugin to support multiple instances #

Even though the UTooL Core provides support for managing instances of a plugin, this support does not come automatically. To ensure your plugin will operate correctly, follow these general guidelines:

  * Never use static variables in your activities, unless you want the variable to persist across all instances
  * Never use static references to tournament controllers, except in the form of a HashMap within your controller class
  * Store all plugin state inside a controller class, and use this to refresh your UI
  * Don't depend on your UI to manage state. See #5 below
  * Don't depend on instance variables in your activities to stick around on resume. Android can garbage collect your activities when another plugin or instance of your plugin has been loaded, and will almost always create a new instance of your activity when resuming anyway.
  * Only use a static variable to manage instances of your controller class
  * It is recommended this be done with a HashMap, using a key of tournamentId
  * Use the provided plugin helpers, or extend the provided AbstractPluginCommonReference and AbstractPluginMainReference classes, rather than the standard Android Activity class
  * Threads live forever, unless stopped or on a crash. Therefore, it is OK to start a long-running thread from an activity. However, remember that your activity will be recreated every time it is brought back by the core, so keep track of your threads per tournament id.
  * Differentiate between a newly created plugin instance and a resumed plugin instance. PluginMainActivityHelper provides the method isNewInstance() for detecting this, though the value may be incorrect if your plugin crashes.