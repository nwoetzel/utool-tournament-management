Emailing and Texting of information has been simplified using the UTooL Common Library. In the utool.plugin.email package of UTooL Common there are classes designed to send texts, to save contact information to a database, and to send emails. Detailed information about to harness all of this functionality is below.

# Sending Emails #

One of the ways to support non-Android devices is to harness the power of utool.plugin.email.GMailSender. This class is designed so that you can very easily send out emails of the matchups at every round.

Required Permissions: to send Emails you need "android.permission.INTERNET"

An example use of this class is as follows:
```
public void updateSubscriber(String address)
{
    //send notification to subscriber of setup
    new RetreiveFeedTask().execute(address);
}
class RetreiveFeedTask extends AsyncTask<String, Void, String> {
    protected String doInBackground(String... urls) {
    try {   
        GMailSender sender = new GMailSender("emailaddress@gmail.com", "password");
        String subject = "This is the Subject";
        String body = "This is the email body";
        String senderEmail = "msoetablet@gmail.com";
        sender.sendMail(subject, body, , urls[0]);   
        Log.d("email", "sent");
    } catch (Exception e) {   
        Log.e("AEH", "Error:"+e.getMessage());
    } 
    return null;
    }
    protected void onPostExecute(String feed) {
    }
}
```

## Sending Texts ##

Sending texts from the local device is very easy with the library functions. Calling TextSended.sendSMS([number](phone.md), [message](message.md), [context](context.md)) will handle everything. The phone number should just be a string of digits with no separations.

Required Permissions: to send Texts you need "android.permission.SEND\_SMS"

An example use of this class is as follows:
```
try
{
    TextSender.sendSMS(phoneNumber,message,c);        
}
catch(Exception e)
{
    Log.e("AutomaticTextHandler","Message Failed");
}
```
Saving Contact information
Saving the contact information for texting and for emails has been simplified using the Commons library. Since  we wanted to reduce the load on the user, all plugins are able to access a central database (stored in sdcard/utool/databases) which will hold a list of phone numbers (#########) and a list of email addresses.
If you do decided to use the common database keep in mind that clearing the database will clear ALL of the other plugins contacts as well. In addition not following the protocol and committing unexpected items can also cause all the other plugins to not work.
There are two main ways that we explored to save the contacts.
### Save them to shared Preferences ###

Instead of tapping into the shared database, contacts can be saved to your plugins shared preferences. The downside of this is that other plugins cannot access this information, menaing the user will have to enter each contact they want into each plugin they have. The upside is that if one plugin misbehaves and either erases or corrupts the database, your plugins contacts are safe.

An example use of doing it this way:
```
/**
* Saves the email list and the phone number list to preferences
* @param ems email list of contacts comma delimited
* @param pn phone number list of contacts comma delimited
*/
private void saveContactsPreferences(String ems, String pn) {
//save list to preferences
SwissTournament t = (SwissTournament) TournamentContainer.getInstance(this.getTournamentId());
SharedPreferences prefs = t.getSwissConfiguration().pref;
prefs.edit().putString(SHARED_PREF_EMAIL_ADDRESSES, ems).commit();
prefs.edit().putString(SHARED_PREF_PHONE_NUMBERS, pn).commit();
}
/**
* Loads the contacts from preferences into contacts
* @param contacts the list of contacts
*/
private void loadContactsPreferences(ArrayList<Contact> contacts) 
{
//load email addresses from preferences and add to list if unique
SwissTournament t = (SwissTournament) TournamentContainer.getInstance(this.getTournamentId());
SharedPreferences prefs = t.getSwissConfiguration().pref;
String em2= prefs.getString(SHARED_PREF_EMAIL_ADDRESSES, ""); 
StringTokenizer e2 = new StringTokenizer(em2, ",");
while(e2.hasMoreTokens())
{
addPossibleSubscriber(contacts, new Contact(e2.nextToken(), Contact.EMAIL_ADDRESS));
}
//load phone numbers
String em= prefs.getString(SHARED_PREF_PHONE_NUMBERS, ""); 
StringTokenizer e = new StringTokenizer(em, ",");
while(e.hasMoreTokens())
{
addPossibleSubscriber(contacts, new Contact(e.nextToken(), Contact.PHONE_NUMBER));
}
}
Where the method addPossibleSubscriber is:
/**
* Adds nextToken to list if not already in emails
* @param emails list of addresses
* @param nextToken email to add if unique
* @return true if added, false if not added
*/
public boolean addPossibleSubscriber(ArrayList<Contact> emails, Contact nextToken) 
{
for(int i=0;i<emails.size();i++)
{
if(emails.get(i).equals(nextToken))
{
return false;
}
}
//not in list
emails.add(nextToken);
return true;
}
```

### Save them to The Database ###
The other main way is to save the contacts to the database. This is the preferred method for the user since it means contacts entered in one plugin will be visible on all of the others. The downside of this is that if one of the plugins malforms a commit it can affect your plugin inadvertently. As mentioned earlier, the database will be located at sdcard/utool/databases. If the user's device does not have an sdcard, this method will not work.
Required Permissions: to write to the database you need "android.permission.WRITE\_EXTERNAL\_STORAGE"
In order to access the database, it is recomended you create a ContactDAO object
```
/**
* holds access to the database
*/
private ContactDAO dao;
//create dao  
dao = new ContactDAO(this.getBaseContext());
```
From here, there are two methods of implementing this. The first is to use dao.putContactList(list); and dao.getContactListArray(); which deal with lists of Contacts. This way is preferred since it is much safer and will not result in malformed entries in the database. The second way is using dao.getContactList(ContactDAO.EMAIL\_TYPE); and dao.putContactList(ems, ContactDAO.EMAIL\_TYPE);. These two methods use a String to represent the list which is comma delimited.
Warning: With both methods, when you put the contacts list, you are replacing the previous contact list. This means if you PUT a blank list, you will CLEAR all of the contacts of that type.
An example use of doing it this way:
```
/**
* Saves the email list and the phone number list to db
* @param list the list of contacts
*/
private void saveContactsDatabase(List<Contact> list) 
{
try{
//open connection
dao.open();
dao.putContactList(list);
} catch(SQLException e){
} finally {
try{
//close connection
dao.close();
} catch (Exception e){
//ignore, we wanted it closed... it closed
}
}
}

/**
* Loads the contacts from db into contacts
* @param contacts the list of contacts
*/
private void loadContactsDatabase(ArrayList<Contact> contacts) 
{
try{
//open connection
dao.open();
//load contacts from database and add to list if unique
List<Contact> dbc= dao.getContactListArray();
//only add unique ones
for(int i=0;i<dbc.size();i++)
{
addPossibleSubscriber(contacts, dbc.get(i));
}
} catch (SQLException e){
Log.e("SQLException", "Could not open or read from the database", e);
} finally{
try{
//close connection
dao.close();
} catch (Exception e){
//ignore, we wanted it closed... it closed
}
}
}
```