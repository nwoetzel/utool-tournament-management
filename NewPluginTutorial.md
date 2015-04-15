# Getting started on a new plugin #

This tutorial is a supplemental to the developer documentation. It expected that you are using Eclipse for development, and have already installed the Android SDK.


## Import UTooL Common project into your Eclipse workspace ##
Download the latest version of the UTooL Common Library. The zip below is provided for convenience only, and may not be the latest version.

In Eclipse, import the common library as an existing project. You will need to change the classpath to include the version of android-support-v4.jar you are using for your project. This file is distributed with the Android SDK.

## Create a new Android Application Project ##
![http://wiki.utool-tournament-management.googlecode.com/hg/new1.png](http://wiki.utool-tournament-management.googlecode.com/hg/new1.png)

Name it whatever you want. The package name doesn't need to follow any special conventions. The UTooL Core has a Minimum Required SDK of API 8, so use this if you want your plugin to maintain full compatibility. After this, continue through the wizard before continuing.

![http://wiki.utool-tournament-management.googlecode.com/hg/new2.png](http://wiki.utool-tournament-management.googlecode.com/hg/new2.png)

## Modify AndroidManifest.xml ##
Your plugin needs the intent filter on your main activity to be modified so that UTooL can discover it.

Replace this:
```
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
```
With this:
```
<intent-filter>
    <action android:name="utool.plugin.intent.PICK_PLUGIN" />
    <category android:name="utool.plugin.PLUGIN" />
</intent-filter>
```
## Add UTooL Common Library ##
The UTooL Common Library is used for all communications with the UTooL Core. Be sure not to modify classes in the common library, otherwise your plugin may be unable to communicate with the core.

![http://wiki.utool-tournament-management.googlecode.com/hg/properties.png](http://wiki.utool-tournament-management.googlecode.com/hg/properties.png)

## Begin adding functionality ##
At this point you are ready to begin implementing your tournament logic. The simplest method of handling your tournament logic is to create three classes: HostTournamentLogic, PlayerTournamentLogic, and CommonTournamentLogic. You should have HostTournamentLogic and PlayerTournamentLogic extend CommonTournamentLogic, make the constructors protected, and use a HashMap to keep track of instances of which logic class instance is associated with a tournament id. When updating your UI, it works best for your logic classes to notify the UI that it needs to update, rather than try to update the UI from within the logic classes.

If you are looking for further guidance, use the King of the Hill plugin as a reference, as it is the simplest completed plugin. The logic classes are contained in the utool.plugin.kingofthehill.tournament package.