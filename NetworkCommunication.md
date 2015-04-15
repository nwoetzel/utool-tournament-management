# Network Communications #

Note: This document assumes you are using the plugin helpers as defined in the reference activities.

Network communications are easy with UTooL. The UTooL Core handles advertising and discovering tournaments on your local network, and manages connections between client devices and the server device for you. Plugins are automatically connected to each other as soon as the connection is established.

Beware of the following limitations in UTooL:

1. The UTooL Core does not track your tournament messages, so new connections need to be synchronized. You have to handle this in your plugin, probably by having the client request a total synchronization on connection.


## Sending and receiving data ##

Sending and receiving data to connected devices with UTooL is easy. The UTooL Core does the hard work of connecting your devices together, and provides easy to use methods to send and receive data between them.

To send data, all you need to do is call pluginHelper.mICore.send(String message). This method, provided by the UTooL Core Service, is designed to send XML string messages. You don't even need to worry about whether you are the client or server, the UTooL Core will make this distinction for you without any extra code.

Receiving data is almost as easy. It is recommended that you start a new thread for receiving, as the method pluginHelper.mICore.receive() blocks until data is received. On a connection error, receive returns a string containing the value defined by PluginCommonActivityHelper.UTOOL\_SOCKET\_CLOSED\_MESSAGE.

As a general reminder, your receive thread will keep running until it finishes, is shutdown, or crashes. Do not start your receive thread more than once per tournament instance.

**Example receive runnable:**

```
Runnable receiveRunnable = new Runnable()
    {
        public void run()
        {
            try {
                while (true)
                {
                    String msg = mICore.receive();
                    if (msg.equals(PluginCommonActivityHelper.UTOOL_SOCKET_CLOSED_MESSAGE)){
                        return;
                    }
                   processMessage(msg); //You write this method
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
```