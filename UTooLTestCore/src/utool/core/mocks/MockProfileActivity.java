package utool.core.mocks;

import android.net.Uri;
import android.widget.EditText;
import utool.core.ProfileActivity;
import utool.core.R;
import utool.persistence.SavableProfileList;

/**
 * Testable version of ProfileActivity (because I didn't want to create public getters and setters in Profile Activity)
 * @author Justin Kreier
 * @version 1/19/2013
 */
public class MockProfileActivity extends ProfileActivity{

	/**
	 * Gets the profiles
	 * @return The profiles
	 */
	public SavableProfileList getProfiles(){
		return this.profiles;
	}
	
	/**
	 * Gets the index to edit
	 * @return The index to edit
	 */
	public int getIndexToEdit(){
		return this.indexToEdit;
	}
	
	/**
	 * Checks if edit is open
	 * @return true if edit is open
	 */
	public boolean isEditOpen(){
		return this.isEditOpen;
	}
	
	/**
	 * Gets the image uri
	 * @return The image uri
	 */
	public Uri getImageUri(){
		return this.imageUri;
	}
	
	/**
	 * Sets the imageUri
	 * @param newUri The new imageUri
	 */
	public void setImageUri(Uri newUri){
		imageUri = newUri;
	}
	
	/**
	 * Gets the num use
	 * @return The num use
	 */
	public int getNumUse(){
		return this.numUse;
	}
	
	/**
	 * Adds a test profile
	 * @param name The profile name
	 * @param filepath The filepath to the profile portrait
	 */
	public void addTestProfile(String name, String filepath){
		super.addPressedFromMenu();
		EditText nameField = (EditText)this.findViewById(R.id.nameTextField);
		nameField.setText(name);
		imageUri = Uri.parse(filepath);
		super.saveButtonPressed();
	}
	

	@Override
	public void deletePressedFromContext(int position){
		super.deletePressedFromContext(position);
	}
	
	@Override
	public void editPressedFromContext(int position){
		super.editPressedFromContext(position);
	}
	
	@Override
	public void selectPressedFromContext(int position){
		super.selectPressedFromContext(position);
	}
	
	@Override
	public void addPressedFromMenu(){
		super.addPressedFromMenu();
	}
	
	@Override
	public void saveButtonPressed(){
		super.saveButtonPressed();
	}
	
	@Override
	public void toggleEditFields(boolean on){
		super.toggleEditFields(on);
	}
	
	@Override
	public void selectProfile(int i){
		super.selectProfile(i);
	}
}
