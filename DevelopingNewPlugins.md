Hello there! Welcome to the world of UTooL Plugin Development. UTooL is an Android application and plugin development framework for quickly and easily hosting various tournament types. These documents will take you through developing a new tournament plugin for UTooL. A basic understanding of Android projects is assumed in these documents.

## What UTooL Provides ##

  * Automatic discovery of locally hosted tournaments.
  * Establishing and maintaining network connections between a host device (server) and participant devices (clients).
  * Support for running multiple tournaments simultaneously, and easily switching between them.
  * Abstractions for setting up plugin activities to communicate with the UTooL Core.

## Getting Started ##

The first thing you want to do is create a new Android project. Link this project to the UTooL Common library. In Eclipse, this is done through the project properties, Android tab, add a library reference. A JAR can also be used, though is only recommended for developers working independently of the UTooL team.

## Activities ##

The main activity is UTooL's entry point into your plugin. Plugins are found by adding an intent filter to your main activity. An example of this is listed at the end of this document.

Several helper classes are included in the UTooL Common library to ease integration with the UTooL Core. Each class extends the functionality of the lower classes, so PluginMainActivityHelper extends PluginServiceActivityHelper extends PluginCommonActivityHelper.

|**Class**|**Purpose**|**What it does on creation**|
|:--------|:----------|:---------------------------|
|PluginMainActivityHelper |Helper for main activity functionality |Read extras sent by the core for tournament configuration, and configure the service connection|
|PluginServiceActivityHelper |Helper for all activities that need a direct connection to the UTooL Core Service |Configure the service connection, then calls runOnServiceConnected in creating activity|
|PluginCommonActivityHelper |Helper for common UTooL functionality, such as tournament ID extra passing and preconfigured intents back to the core. |Get the tournamentId extra from the activity's intent|

Any activity that needs the functionality provided by PluginMainActivityHelper or PluginServiceActivityHelper should implement IPluginServiceActivity. This allows for the helper objects to call back to your initialization code when the service has finished initialization. Be sure to use runOnServiceConnected() for all your service-dependent initialization code, as specified in the UTooL Service Connection section below.

Main activities should use PluginMainActivityHelper, or extend the UTooL provided class AbstractPluginMainReference. These classes automatically grab the intent extras provided by the UTooL Core and configure the service connection.

Most secondary activities should use PluginCommonActivityHelper, or extend the UTooL provided class AbstractPluginCommonReference. This abstract class automatically grabs the tournamentId extra, and provides the method getNewIntent, which automatically places the tournamentId extra in new intents. It is recommended that all non-main activities in your project use getNewIntent from either the plugin helper or the abstract classes to call other activities.

## UTooL Service Connection ##

The UTooL Service provides network communication functionality. Methods are provided to send and receive data as String messages, as well as detect whether the connection was set up as a client or server. This connection is automatically established by any instance of PluginServiceActivityHelper, which has hooks for running code on service connection and disconnection. Communicate with the service through the mICore object provided by the helper. It takes some time for the service connection to be established. DO NOT attempt to use the connection (mICore) before **runOnServiceConnected()** has been called.  For simplicity, it is recommended that your activity that requires a service connection implement IPluginServiceActivity, though you implement it with another class if desired.

The runOnServiceConnected() method is only called once for each tournament instance. Don't depend on it being called every time the same instance is resumed.

## IPluginServiceActivity ##

This interface provides a way for activities to hook into the service connection established by PluginServiceActivityHelper and PluginMainActivityHelper.

Once the service connection has been asynchronously established and configured, the code in **runOnServiceConnected()** will be run. **Put all of your activity initialization code that depends on the service in this method.** If the service disconnects, runOnServiceDisconnected() will be called. This is a good place to put emergency cleanup and state save code. The method sendMessage(String message) is available for your convinence only, and is only used if you are extending the provided CommunicationBridge class. The most common implementation is below.

```
public boolean sendMessage(String message)
    {
        return pluginHelper.sendMessage(message);
    }
```

## Intent Extras Passed to Main Activity ##

These are the extras sent to the main activity on plugin initialization. You may access these extras through a plugin helper, extend a provided abstract activity class, or manually.

**tournamentId** (Provided by common helper)

> Type: long

> Description: The tournament id is a long integer used by the UTooL Core to distinguish between tournament instances on a device. This should not be changed by a tournament, and will definitely not be unique across devices or program reloads.

> Accessing: getTournamentId()

**tournamentName**

> Type: String

> Description: The name of your tournament provided by the Core.

> Accessing: getTournamentName()

> Note: This method switches to using mICore to retrieve the tournament name after the service connection has been established.

**playerList**

> Type: `ArrayList<Player>`

> Description: All the players who joined your tournament before the plugin started.

> Accessing: getPlayerList()

> Note: This method switches to using mICore to retrieve the player list after the service connection has been established. Use this, along with PlayerMessages received over the service connection, to provide dynamic player management capabilities.

**permissionLevel**

> Type: int

> Values: Defined in utool.plugin.Player. Set to HOST on the server device.
|Name	|Value|
|:----|:----|
|PARTICIPANT	|0 |
|MODERATOR	|1 |
|HOST	|2 |
|DEVICELESS	|3 |

> Description: The permission level your plugin was started with. Do not use this to determine if you are the server, rather use the UTooL Core Service method isClient() to determine if you are a client device.

> Accessing: getPermissionLevel()

**pid**

> Type: UUID

> Description: The UUID of the profile your tournament was started with.

**isNewInstance**

> Type: boolean

> Description: Get whether this plugin instance is newly created, or if it is being resumed. In this case, being resumed means that the user backed out of the plugin to the UTooL Core UI, and has selected an already in progress tournament that has been loaded on the device. This value has nothing to do with saved tournaments. This value is not reset to true if a plugin crashes.

> Accessing: isNewInstance()

## Example of an Intent Filter ##

```
<activity
    android:name=".SingleEliminationMainActivity"
    android:label="@string/title_activity_main"
    android:exported="true">
        <intent-filter>
            <action android:name="utool.plugin.intent.PICK_PLUGIN" />
            <category android:name="utool.plugin.PLUGIN" />
        </intent-filter>
</activity>
```