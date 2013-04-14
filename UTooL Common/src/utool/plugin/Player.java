package utool.plugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

/**
 * Data class that represents a Player.
 * 
 * @author Thomas
 * @author Cory
 * @author Justin
 *
 * @version 1/6/2013
 */
public class Player implements Parcelable {

	/**
	 * List of bitmaps being used by players
	 */
	private static final HashMap<UUID, Bitmap> portraits = new HashMap<UUID, Bitmap>();

	/**
	 * Participant level of permissions.. meaning none
	 */
	public final static int PARTICIPANT = 0;

	/**
	 * Moderator level of permissions, so they can set scores
	 */
	public final static int MODERATOR = 1;

	/**
	 * Host level of permissions, so they can do everything
	 */
	public final static int HOST = 2;

	/**
	 * Player without a device connected
	 */
	public final static int DEVICELESS = 3;


	/**
	 * Constant UUID indicating the player is a "bye"
	 */
	public static final UUID BYE = new UUID(-1,-1);

	/**
	 * Holds the permissions of the player
	 */
	private int permissionsLevel = DEVICELESS;

	/**
	 * Creator object needed by Android for every parcelable class
	 */
	public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {

		public Player createFromParcel(Parcel in) {
			return new Player(in);
		}

		public Player[] newArray(int size) {
			return new Player[size];
		}

	};

	/**
	 * The player's UUID
	 */
	protected UUID uuid;

	/**
	 * Holds whether or not a player is a ghost. A ghost is one who wants tournament updates but
	 * is not participating
	 */
	protected boolean isGhost = false;

	/**
	 * The seed of the player. -1 indicates no seed is set
	 */
	protected int seedValue=-1;

	/**
	 * The player's name
	 */
	protected String name;

	/**
	 * Filepath to the player portrait
	 */
	protected String portraitFilepath = null;

	/**
	 * Whether or not the portrait has changed
	 */
	private boolean hasPortraitChanged = false;

	/**
	 * Read player data from a parcel
	 * @param in The parcel containing player data
	 */
	public Player(Parcel in){
		ParcelUuid pUUID = in.readParcelable(null);
		uuid = pUUID.getUuid();
		name = in.readString();
		permissionsLevel = in.readInt();
		byte[] b = in.createByteArray();
		Bitmap bm = portraits.get(uuid);
		if (bm != null){
			bm.recycle();
		}
		portraits.put(uuid, BitmapFactory.decodeByteArray(b, 0, b.length));
		hasPortraitChanged = false;
	}

	/**
	 * Create a new player from existing player data
	 * @param uuid The player's uuid
	 * @param name The player's name
	 */
	public Player(UUID uuid, String name){
		this.uuid = uuid;
		this.name = name;
	}

	/**
	 * Create a new player
	 * @param name The player's name
	 */
	public Player(String name)
	{
		this.uuid = UUID.randomUUID();
		this.name = name;
	}

	/**
	 * Create a new player
	 * @param id The player's id
	 * @param name The player's name
	 * @param isGhost whether or not they are participating
	 * @param seedValue the seed of the player
	 */
	public Player(UUID id, String name, boolean isGhost, int seedValue){
		this.uuid = id;
		this.name = name;
		this.isGhost = isGhost;
		this.seedValue = seedValue;
	}

	/**
	 * Create a new player
	 * @param id The player's id
	 * @param name The player's name
	 * @param isGhost whether or not they are participating
	 * @param seedValue the seed of the player
	 * @param filepath the filepath to the player's portrait
	 */
	public Player(UUID id, String name, boolean isGhost, int seedValue, String filepath){
		this.uuid = id;
		this.name = name;
		this.isGhost = isGhost;
		this.seedValue = seedValue;
		this.portraitFilepath = filepath;
		if (filepath == null){
			hasPortraitChanged = false;
		} else {
			hasPortraitChanged = true;
		}
	}

	/**
	 * Create a new player
	 * @param id The player's id
	 * @param name The player's name
	 * @param isGhost whether or not they are participating
	 * @param seedValue the seed of the player
	 * @param filepath the filepath to the player's portrait
	 * @param bm The player's portrait
	 */
	public Player(UUID id, String name, boolean isGhost, int seedValue, String filepath, Bitmap bm){
		this.uuid = id;
		this.name = name;
		this.isGhost = isGhost;
		this.seedValue = seedValue;
		this.portraitFilepath = filepath;
		if (filepath == null){
			hasPortraitChanged = false;
		} else {
			hasPortraitChanged = true;
		}
		if (bm != null){
			Bitmap old = portraits.get(uuid);
			if (old != null && old != bm){
				old.recycle();
			}
			portraits.put(uuid, bm);
		}
	}
	/**
	 * Create a new player
	 * @param id The player's id
	 * @param name The player's name
	 * @param isGhost whether or not they are participating
	 * @param seedValue the seed of the player
	 * @param filepath the filepath to the player's portrait
	 * @param bm the picture
	 * @param permissionLevel the permission level 
	 */
	public Player(UUID id, String name, boolean isGhost, int seedValue, String filepath, Bitmap bm, int permissionLevel){
		this.uuid = id;
		this.name = name;
		this.isGhost = isGhost;
		this.seedValue = seedValue;
		this.portraitFilepath = filepath;
		if (filepath == null){
			hasPortraitChanged = false;
		} else {
			hasPortraitChanged = true;
		}
		if (bm != null){
			Bitmap old = portraits.get(uuid);
			if (old != null && old != bm){
				old.recycle();
			}
			portraits.put(uuid, bm);
		}
		this.permissionsLevel = permissionLevel;
	}

	/**
	 * Get the player's name
	 * @return Player's name as a string
	 */
	public String getName(){
		return name;
	}

	/**
	 * Setter for player name
	 * @param name the name of the player
	 */
	public void setName(String name){
		this.name = name;
	}

	/**
	 * Get the player's number
	 * @return Player's number as a UUID
	 */
	public UUID getUUID(){
		return uuid;
	}

	/**
	 * Check if the player objects are the same
	 * @param o The object to compare against
	 * @return True if equal, otherwise false
	 */
	public boolean equals(Object o){
		if (o instanceof Player){
			Player p = (Player)o;
			//Check for id and name in case id is same
			//TODO this should ONLY be uuid imo
			//changed on 2/1/13 by Maria
			if(p.getUUID().equals(this.uuid))// && p.getName().equals(this.name))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode(){
		return this.uuid.hashCode();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		ParcelUuid pUUID = new ParcelUuid(uuid);
		out.writeParcelable(pUUID, 0);
		out.writeString(name);
		out.writeInt(permissionsLevel);
		
		//reload portrait if file path changed
		if (hasPortraitChanged){
			getPortrait();
		}

		//nulls can't be placed in a parcel, so at least put a 0-length byte array for the portrait
		byte[] b = new byte[0];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (portraits.get(uuid) != null){
			portraits.get(uuid).compress(Bitmap.CompressFormat.PNG, 100, baos);  
			b = baos.toByteArray();	
		}
		out.writeByteArray(b);
		try {
			baos.close();
		} catch (IOException e) {
			//do nothing
		}
	}

	/**
	 * Gets if the player is a ghost
	 * @return the isGhost
	 */
	public boolean isGhost() {
		return isGhost;
	}

	/**
	 *  Sets if the player is a ghost
	 * @param isGhost the isGhost to set
	 */
	public void setGhost(boolean isGhost) {
		this.isGhost = isGhost;
	}

	/**
	 * Setter for the seed value. -1 indicates no seed is set
	 * @return the seedValue
	 */
	public int getSeedValue() {
		return seedValue;
	}

	/**
	 * Getter for the seed value. -1 indicates no seed is set
	 * @param seedValue the seedValue to set
	 */
	public void setSeedValue(int seedValue) {
		this.seedValue = seedValue;
	}


	@Override
	public String toString(){
		return name+":"+uuid;
	}

	/**
	 * Sets the player's portrait
	 * @param image The image to use for the portrait
	 */
	public void setPortrait(Bitmap image){
		if (image != null){

			Bitmap old = portraits.get(uuid);
			if (old != null && old != image){
				old.recycle();
			}
			portraits.put(uuid, image);
			hasPortraitChanged = false;
		}
	}

	/**
	 * Retrieves the player's portrait as a bitmap
	 * @return The image used as the portrait
	 */
	public Bitmap getPortrait(){
		if (hasPortraitChanged){
			Bitmap bm = PictureLoader.loadAndOrientPicture(portraitFilepath);
			if (bm!= null){
				portraits.put(uuid, bm);
			} else {
				portraits.put(uuid, null);
			}
			if (bm != null){
				hasPortraitChanged = false;
			}
		}
		return portraits.get(uuid);
	}

	/**
	 * Returns true if the portrait has changed since it last loaded
	 * @return True if the portrait has changed
	 */
	public boolean hasPortraitChanged(){
		return hasPortraitChanged;
	}

	/**
	 * Getter for the permission level
	 * @return permission level
	 */
	public int getPermissionsLevel() {
		return permissionsLevel;
	}

	/**
	 * Setter for the permissions level
	 * @param permissionsLevel the level of permission
	 */
	public void setPermissionsLevel(int permissionsLevel) {
		this.permissionsLevel = permissionsLevel;
	}

	/**
	 * Gets the filepath to the player's portrait
	 * @return The filepath to the player's portrait
	 */
	public String getPortraitFilepath(){
		return portraitFilepath;
	}

	/**
	 * Sets the filepath to the player's portrait
	 * @param newFilepath The filepath to the player's portrait
	 */
	public void setPortraitFilepath(String newFilepath){
		hasPortraitChanged = true;
		portraitFilepath = newFilepath;
	}
}
