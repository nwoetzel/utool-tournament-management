package utool.core.tests;

import utool.core.R;
import utool.core.mocks.MockProfileActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.view.View;
import android.widget.EditText;

/**
 * Verifies the functionality of ProfileActivity 
 * @author Justin Kreier
 * @version 1/19/2013
 */
public class TestProfileActivity extends ActivityUnitTestCase<MockProfileActivity> {

	/**
	 * Required constructor
	 */
	public TestProfileActivity() {
		super(MockProfileActivity.class);
	}

	/**
	 * The activity under test
	 */
	private MockProfileActivity mActivity;

	@Override
	protected void setUp() throws Exception{
		super.setUp();
		
		//clear application data
		AndroidTestHelperMethods.clearApplicationData(getInstrumentation().getTargetContext());
		
		//start the activity
		Intent i = new Intent(getInstrumentation().getTargetContext(), MockProfileActivity.class);
		mActivity = startActivity(i, (Bundle)null, (Object)null);
		mActivity.onResume();
	}

	@Override
	protected void tearDown() throws Exception{
		super.tearDown();
		
		//clear application data
		AndroidTestHelperMethods.clearApplicationData(getInstrumentation().getTargetContext());
	}


	/**
	 * Tests that profiles initialize properly
	 */
	public void testInitialization(){
		assertNotNull(mActivity.getProfiles());
		assertEquals(0, mActivity.getProfiles().size());
		assertEquals(0,mActivity.getIndexToEdit());
		assertTrue(mActivity.isEditOpen());
		assertNotNull(mActivity.getImageUri());
		assertEquals(0,mActivity.getNumUse());

	}


	/**
	 * Tests that deleting a profile from context works properly
	 */
	public void testDeletePressedFromContext(){
		
		//add two profiles
		mActivity.addTestProfile("Test Profile 1", "Filepath 1");
		mActivity.addTestProfile("Test Profile 2", "Filepath 2");
		assertEquals(2,mActivity.getProfiles().size());

		//delete one of them
		mActivity.deletePressedFromContext(1);
		assertEquals(1,mActivity.getProfiles().size());
		assertFalse(mActivity.isEditOpen());

		//delete the other one
		mActivity.deletePressedFromContext(0);
		assertEquals(0,mActivity.getProfiles().size());
	}

	/**
	 * Tests that editing a profile from context works properly
	 */
	public void testEditPressedFromContext(){

		//add two profiles
		mActivity.addTestProfile("Test Profile 1", "Filepath 1");
		mActivity.addTestProfile("Test Profile 2", "Filepath 2");
		assertEquals(2,mActivity.getProfiles().size());

		//edit one of them
		mActivity.editPressedFromContext(1);
		assertEquals(1, mActivity.getIndexToEdit());
		assertEquals("/Filepath 2", mActivity.getImageUri().getPath());
		

		EditText edit = (EditText)mActivity.findViewById(R.id.nameTextField);
		assertEquals("Test Profile 2", edit.getText().toString());

		assertTrue(mActivity.isEditOpen());
	}

	/**
	 * Tests that a profile is selected when select is pressed from context
	 */
	public void selectPressedFromContext(){

		//add three profiles
		mActivity.addTestProfile("Test Profile 1", "Filepath 1");
		mActivity.addTestProfile("Test Profile 2", "Filepath 2");
		mActivity.addTestProfile("Test Profile 3", "Filepath 3");
		assertEquals(3,mActivity.getProfiles().size());

		mActivity.selectPressedFromContext(0);
		assertEquals(0,mActivity.getProfiles().getSelectedProfileIndex());
		mActivity.selectPressedFromContext(2);
		assertEquals(2,mActivity.getProfiles().getSelectedProfileIndex());
		mActivity.selectPressedFromContext(1);
		assertEquals(1,mActivity.getProfiles().getSelectedProfileIndex());
	}

	/**
	 * Tests the functionality of add being pressed from menu
	 */
	public void testAddPressedFromMenu(){
		
		//add three profiles
		mActivity.addTestProfile("Test Profile 1", "Filepath 1");
		mActivity.addTestProfile("Test Profile 2", "Filepath 2");
		mActivity.addTestProfile("Test Profile 3", "Filepath 3");
		assertEquals(3,mActivity.getProfiles().size());

		mActivity.addPressedFromMenu();

		assertTrue(mActivity.isEditOpen());
		EditText edit = (EditText)mActivity.findViewById(R.id.nameTextField);
		assertEquals("", edit.getText().toString());
		assertEquals(mActivity.getProfiles().size(), mActivity.getIndexToEdit());
		assertEquals(3, mActivity.getIndexToEdit());
	}

	/**
	 * Tests that the save button works properly
	 */
	public void testSaveButtonPressed(){

		mActivity.addPressedFromMenu();
		EditText edit = (EditText)mActivity.findViewById(R.id.nameTextField);
		edit.setText("Test Profile 1");
		mActivity.setImageUri(Uri.parse("Test Filepath 1"));
		mActivity.saveButtonPressed();
	}

	/**
	 * Tests that toggling the edit fields works
	 */
	public void testToggleEditFields(){

		mActivity.toggleEditFields(true);

		//get all of the things we need to check
		View nameView = mActivity.findViewById(R.id.nameTextView);
		View nameField = mActivity.findViewById(R.id.nameTextField);
		View portraitView = mActivity.findViewById(R.id.portraitTextView);
		View portraitImage = mActivity.findViewById(R.id.portraitImageView);
		View camButton = mActivity.findViewById(R.id.cameraButton);
		View galleryButton = mActivity.findViewById(R.id.galleryButton);
		View saveButton = mActivity.findViewById(R.id.saveButton);
		View pbar = mActivity.findViewById(R.id.progressBar2);

		//check they are visible
		assertEquals(View.VISIBLE,nameView.getVisibility());
		assertEquals(View.VISIBLE,nameField.getVisibility());
		assertEquals(View.VISIBLE,portraitView.getVisibility());
		assertEquals(View.VISIBLE,portraitImage.getVisibility());
		assertEquals(View.VISIBLE,camButton.getVisibility());
		assertEquals(View.VISIBLE,galleryButton.getVisibility());
		assertEquals(View.VISIBLE,saveButton.getVisibility());
		assertEquals(View.VISIBLE,pbar.getVisibility());

		mActivity.toggleEditFields(false);

		//check they are gone
		assertEquals(View.GONE,nameView.getVisibility());
		assertEquals(View.GONE,nameField.getVisibility());
		assertEquals(View.GONE,portraitView.getVisibility());
		assertEquals(View.GONE,portraitImage.getVisibility());
		assertEquals(View.GONE,camButton.getVisibility());
		assertEquals(View.GONE,galleryButton.getVisibility());
		assertEquals(View.GONE,saveButton.getVisibility());
		assertEquals(View.GONE,pbar.getVisibility());
	}


	/**
	 * Tests that select profile works properly
	 */
	public void testSelectProfile(){

		assertEquals(0, mActivity.getProfiles().size());
		
		//add three profiles
		mActivity.addTestProfile("Test Profile 1", "Filepath 1");
		mActivity.addTestProfile("Test Profile 2", "Filepath 2");
		mActivity.addTestProfile("Test Profile 3", "Filepath 3");
		assertEquals(3,mActivity.getProfiles().size());

		mActivity.selectProfile(0);
		assertEquals(0,mActivity.getProfiles().getSelectedProfileIndex());
		mActivity.selectProfile(2);
		assertEquals(2,mActivity.getProfiles().getSelectedProfileIndex());
		mActivity.selectProfile(1);
		assertEquals(1,mActivity.getProfiles().getSelectedProfileIndex());
	}

//	/**
//	 * Wipes the application data
//	 */
//	private void clearApplicationData() {
//		File cache = getInstrumentation().getTargetContext().getCacheDir();
//		File appDir = new File(cache.getParent());
//		if (appDir.exists()) {
//			String[] children = appDir.list();
//			for (String s : children) {
//				if (!s.equals("lib")) {
//					deleteDir(new File(appDir, s));
//				}
//			}
//		}
//	}
//
//	/**
//	 * Deletes a directory
//	 * @param dir The directory to delete
//	 * @return True if deleted
//	 */
//	private static boolean deleteDir(File dir) {
//		if (dir != null && dir.isDirectory()) {
//			String[] children = dir.list();
//			for (int i = 0; i < children.length; i++) {
//				boolean success = deleteDir(new File(dir, children[i]));
//				if (!success) {
//					return false;
//				}
//			}
//		}
//		return dir.delete();
//	}

}
