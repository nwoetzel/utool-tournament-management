package utool.plugin.email;

/**
 * Holds a contact.
 * @author waltzm
 * @version 4/20/2012
 */
public class Contact 
{
	/**
	 * Hold the type for email address
	 */
	public static final int EMAIL_ADDRESS = 346;

	/**
	 * Holds the type for Phone numbers
	 */
	public static final int PHONE_NUMBER = 234;

	/**
	 * holds the contact info
	 */
	private String info;

	/**
	 * holds the type
	 */
	private int type;

	/**
	 * Holds a contact
	 * If an incorrect type ia passed in, defaults to email address.
	 * @param info contact info (e.g. email address, phone number)
	 * @param type the type of contact
	 */
	public Contact(String info, int type)
	{
		this.setInfo(info);
		this.setType(type);

	}

	/**
	 * Getter for the info
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * setter for the info
	 * @param info the info
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * Getter for the type
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Setter for the type
	 * Defaults to email
	 * @param type the type of contact
	 */
	public void setType(int type)
	{
		//error checking
		if(type!=EMAIL_ADDRESS && type!=PHONE_NUMBER)
		{
			type = EMAIL_ADDRESS;
		}
		else
		{
			this.type = type;
		}
	}
	
	@Override
	public String toString()
	{
		return this.info;
	}
	
	@Override
	public boolean equals(Object c)
	{
		if(c instanceof Contact)
		{
			Contact con = (Contact)c;
			if(con.getInfo().equals(this.getInfo()))
			{
				if(con.getType()==this.getType())
				{
					return true;
				}
			}
		}
		return false;
	}
}
