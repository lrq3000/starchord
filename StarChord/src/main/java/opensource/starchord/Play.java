/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                   *
 *   Copyleft  @ 2013 Stephen Larroque                                     *
 *   Copyright © 2012 James Haigh                                          *
 *   Copyright © 2011, 2012 David A. Randolph                              *
 *                                                                         *
 *   FILE: Play.java                                                       *
 *                                                                         *
 *   This file is part of Hexiano, an open-source project hosted at:       *
 *   https://github.com/lrq3000/hexiano                                         *
 *                                                                         *
 *   Hexiano is free software: you can redistribute it and/or              *
 *   modify it under the terms of the GNU General Public License           *
 *   as published by the Free Software Foundation, either version          *
 *   3 of the License, or (at your option) any later version.              *
 *                                                                         *
 *   Hexiano is distributed in the hope that it will be useful,            *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with Hexiano.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package opensource.hexiano;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.SoundPool;
import android.media.AudioManager;

import opensource.hexiano.R;

public class Play extends Activity implements OnSharedPreferenceChangeListener
{
	final static int ABOUT_DIALOG_ID = 1;
	static SharedPreferences mPrefs;
	//static FrameLayout mFrame;
	static HexKeyboard mBoard;
	static HashMap<String, Instrument> mInstrument;
	static Iterator<Instrument> instrument_load_queue;
	static Instrument currLoadingInstrument;
	static boolean mInstrumentChanged = false;
	static int POLYPHONY_COUNT = 16;
	public static SoundPool mSoundPool;
	boolean configChanged = false;

	private String getVersionName()
	{
		String versionName = "";
		
		try
		{
			versionName = getPackageManager().
					getPackageInfo(getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e)
		{
			Log.e("getVersionName", e.getMessage());
		}
		
		return versionName;
	}

	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Lock volume control to Media volume.
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
		
    	String versionStr = this.getVersionName();
        
		// Hide both title bar and status bar for maximum screen space
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
       
        loadKeyboard();
	}

	protected int setOrientation()
	{
		String layout = mPrefs.getString("layout", null);

		int orientationId = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

		if (layout.equals("Sonome"))
		{
			boolean isLandscape = mPrefs.getBoolean("sonomeLandscape", false);
			if (! isLandscape)
			{
				orientationId = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}
		else if (layout.equals("Jammer"))
		{
			boolean isLandscape = mPrefs.getBoolean("jammerLandscape", false);
			if (! isLandscape)
			{
				orientationId = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}
		else if (layout.equals("Janko"))
		{
			boolean isLandscape = mPrefs.getBoolean("jankoLandscape", false);
			if (! isLandscape)
			{
				orientationId = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}

		this.setRequestedOrientation(orientationId);
		return orientationId;
	}
	
	// Load SoundPool, the sound manager
	public static void loadSoundManager() {
        POLYPHONY_COUNT = Integer.parseInt(HexKeyboard.mPrefs.getString("polyphonyCount", "8")); // Reload polyphony count
        if (mSoundPool != null) mSoundPool.release();
        mSoundPool = null; System.gc();
		mSoundPool = new SoundPool(POLYPHONY_COUNT, AudioManager.STREAM_MUSIC, 0);

		// Load another sound whenever the previous one has finished loading
		// NOTE: ensure that this function ALWAYS load only one sound, and that only ONE SOUND is loaded prior (so that this function is only called once. If you load two sounds, this function will be called twice, and so on). This is critical to ensure that the UI remains responsive during sound loading.
		// NOTE2: SoundPool already works in its own thread when loading sounds, so there's no need to use an AsyncTask. There's no way to enhance the UI responsiveness while loading, it's because of SoundPool using lots of resources when loading (hence why you should always ensure to load only one sound to kickstart the SoundPool listener).
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool mSoundPool, int sampleId, int status) {
				mBoard.invalidate(); // Redraw board to refresh keys that now have their sound loaded

				// If there are yet others sounds to load in this batch of sounds (tuples)
				if (currLoadingInstrument.sound_load_queue != null && currLoadingInstrument.sound_load_queue.hasNext()) {
					ArrayList tuple = currLoadingInstrument.sound_load_queue.next();
					if (!currLoadingInstrument.mExternal) {
						currLoadingInstrument.addSound((Integer)tuple.get(0), (Integer)tuple.get(1), (Integer)tuple.get(2)); // Instrument class (not external): we use a ressource ID int
					} else {
						currLoadingInstrument.addSound((Integer)tuple.get(0), (Integer)tuple.get(1), (String)tuple.get(2)); // Instrument external: we use a string path
					}

				// Else if batch of sounds (tuples) empty but we have other notes sounds to load
				} else if (currLoadingInstrument.notes_load_queue != null && currLoadingInstrument.notes_load_queue.hasNext()) {
					currLoadingInstrument.currListOfTuples = currLoadingInstrument.notes_load_queue.next();
					currLoadingInstrument.sound_load_queue =  currLoadingInstrument.currListOfTuples.iterator();
					onLoadComplete(mSoundPool, sampleId, status); // try to load sounds for this next list of tuples (batch of sounds)

				// Else if no more sound for this instrument but we have another instrument for which sounds are to be loaded, we switch to the next instrument
				} else if (instrument_load_queue.hasNext()) {
					// Switch to the next instrument
					currLoadingInstrument = instrument_load_queue.next();
					// Setup the sound load queue for this instrument
					currLoadingInstrument.notes_load_queue = currLoadingInstrument.sounds_to_load.values().iterator();
					onLoadComplete(mSoundPool, sampleId, status); // try to load sounds for this next instrument

				// Else all sounds loaded! Show a short notification so that the user knows that (s)he can start playing without lags
				} else {
					Toast.makeText(HexKeyboard.mContext, R.string.finished_loading, Toast.LENGTH_SHORT).show();
				}
			}
        });
	}
	
	protected static void addInstrument(String instrumentName) {
		// Add instrument only if not already in the map
		if (!mInstrument.containsKey(instrumentName)) {
			// Choose the correct instrument class to load, deducting from instrument's name

			// Piano
			if (instrumentName.equalsIgnoreCase("Piano") || instrumentName.equalsIgnoreCase("DUMMY (dont choose)")) // Place an if conditional for each staticInstrument (included in APK resources)
			{
				mInstrument.put(instrumentName, new Piano(HexKeyboard.mContext));
			// Generic external instrument for any other case
			} else {
				try {
					mInstrument.put(instrumentName, new GenericInstrument(HexKeyboard.mContext, instrumentName));
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static boolean loadInstruments() {
		boolean multiInstrumentsEnabled = mPrefs.getBoolean("multiInstrumentsEnable", false);
		mInstrument = null;
		mInstrument = new HashMap<String, Instrument>();
		// Single instrument
		if (!multiInstrumentsEnabled) {
			String instrumentName = mPrefs.getString("instrument", "Piano");
			addInstrument(instrumentName);
		// Multi instruments
		} else {
			TreeMap<Integer, HashMap<String, String>> mapping = Prefer.getMultiInstrumentsMappingHashMap(mPrefs);
			if (mapping != null && mapping.size() > 0) {
				for(HashMap<String, String> instru : mapping.values()) {
					String instrumentName = instru.get("instrument");
					if (!mInstrument.containsKey(instrumentName)) {
						addInstrument(instrumentName);
					}
				}
			}
			// Also add single instrument as the default instrument for undefined keys in mapping
			String instrumentName = mPrefs.getString("instrument", "Piano");
			addInstrument(instrumentName);
		}
		return true;
	}
	
	// Load the first sound and then delegate the rest of the loading to the SoundManager (SoundPool)
	protected void loadFirstSound() {
		// Setup the instruments iterator (for the soundmanager to iteratively load all sounds for each instrument)
		instrument_load_queue = mInstrument.values().iterator();
		// Loop until we find a sound to load
		// Note: we do that because with an incorrect multi-instruments setup, it may happen that an instrument has no sound mapped on it (the range specified by user is out of visible screen), in this case we will try to load sounds from other instruments that are correctly mapped (normally, the default single instrument should always map at least one sound in this case)
		boolean found_a_sound = false;
		while (!found_a_sound && instrument_load_queue.hasNext()) { // Loop until either we have found a sound, or we have iterated through all instruments and none have any sound to load (this should never happen!)
			// Initiate the loading process, by loading the first instrument and the first sound for this instrument
			currLoadingInstrument = instrument_load_queue.next();
			// Setup the sound load queue for this instrument
			currLoadingInstrument.notes_load_queue = currLoadingInstrument.sounds_to_load.values().iterator();

			// If we have found a sound to load, we stop here
			if (currLoadingInstrument.notes_load_queue.hasNext()) found_a_sound = true;
		}
		// If we iterated through all instruments and never found any sound to load, then it's a fatal error that should never happen!
		if (!found_a_sound) {
			Log.e("Play::loadFirstSound", "Cannot load the first sound for any instrument! Fatal error!");
			Toast.makeText(HexKeyboard.mContext, R.string.error_no_first_sound, Toast.LENGTH_LONG).show();
		} else {
			// Start loading the first instrument and the first sound for the first note, the rest of the sounds are loaded from the Play::loadKeyboard()::OnLoadCompleteListener()
			currLoadingInstrument.currListOfTuples = currLoadingInstrument.notes_load_queue.next();
			currLoadingInstrument.sound_load_queue = currLoadingInstrument.currListOfTuples.iterator();
			ArrayList tuple = currLoadingInstrument.sound_load_queue.next();
			if (!currLoadingInstrument.mExternal) {
				currLoadingInstrument.addSound((Integer)tuple.get(0), (Integer)tuple.get(1), (Integer)tuple.get(2)); // Instrument class (not external): we use a ressource ID int
			} else {
				currLoadingInstrument.addSound((Integer)tuple.get(0), (Integer)tuple.get(1), (String)tuple.get(2)); // Instrument external: we use a string path
			}
		}
	}
	
	protected void loadKeyboard()
	{
	    int orientationId = setOrientation();
		Context con = this.getApplicationContext();
		
		Toast.makeText(con, R.string.beginning_loading, Toast.LENGTH_SHORT).show(); // Show a little message so that user know that the app is loading
		
		//mFrame = new FrameLayout(con);
		mBoard = new HexKeyboard(con);

		// Reload previously cached data (SoundPool decoded sounds) if available, this really speeds up orientation switches!
		//mInstrument = (HashMap<String, Instrument>) getLastNonConfigurationInstance();
		Play prevActivity = (Play)getLastNonConfigurationInstance();
		if(prevActivity != null) {
			Log.d("Play", "prevActivity is not null!");
			Play.mSoundPool = prevActivity.mSoundPool;
			Play.mInstrument = prevActivity.mInstrument;
		} else { // If no retained audio (or changed), load it all up (slow).
		//if (mInstrument == null || mInstrumentChanged) {
			// Load SoundPool, the sound manager
	        loadSoundManager();
	        // Then, load the instruments
			loadInstruments();
		//}
		}
		mBoard.setUpBoard(orientationId);
		mBoard.invalidate();

		loadFirstSound(); // Do this only after setUpBoard() so that it can setup the keyboard and limit the range of notes to load

		// Create a frame layout to hold both the keyboard and floating menu button
		FrameLayout frameLayout = new FrameLayout(con);
		frameLayout.addView(mBoard);
		
		// Add a floating menu button for modern devices
		Button menuButton = new Button(con);
		menuButton.setText("⋮");
		menuButton.setTextSize(20);
		menuButton.setAlpha(0.8f);
		menuButton.setPadding(12, 8, 12, 8);
		
		FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
			FrameLayout.LayoutParams.WRAP_CONTENT,
			FrameLayout.LayoutParams.WRAP_CONTENT
		);
		buttonParams.setMargins(16, 16, 16, 16);
		buttonParams.gravity = android.view.Gravity.TOP | android.view.Gravity.RIGHT;
		
		menuButton.setLayoutParams(buttonParams);
		menuButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showFloatingMenu(v);
			}
		});
		
		frameLayout.addView(menuButton);
		this.setContentView(frameLayout);
	}

	// Cache data to speedup orientation switches
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		Log.d("Play", "onRetainNonConfigurationInstance()");
		// Retain the audio across configuration changes.
		// TODO: try to retain SoundPool when just quitting preferences without changing anything, but not sure it could be retained since it's not serializable. Maybe as a service? http://stackoverflow.com/questions/16169488/how-to-get-an-android-bound-service-to-survive-a-configuration-restart
		// TODO: or use fragments: https://github.com/alexjlockwood/worker-fragments
		// TODO: or change SoundPool for AudioTrack to read directly from file (but does it handle ogg?): http://www.martinhoeller.net/2012/01/13/developing-a-musical-instrument-app-for-android/
		//return mInstrument;
		return Play.this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		Log.d("Play", "Menu, ho!");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu); 
		return true; 
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ABOUT_DIALOG_ID:
                return new AboutDialog(this);
            default:
                return null;
        }
    }
   
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.preferences) {
			startActivity(new Intent(this, Prefer.class));
		} else if (item.getItemId() == R.id.quit) {
			finish();
		} else if (item.getItemId() == R.id.about) {
			showDialog(ABOUT_DIALOG_ID);
		}

		return true; 
	}

	// Show floating popup menu
	private void showFloatingMenu(View anchor) {
		PopupMenu popup = new PopupMenu(this, anchor);
		popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return onOptionsItemSelected(item);
			}
		});
		
		popup.show();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		configChanged = true;
		Log.d("Play", "Preference changed: " + key);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Check if settings have changed when returning from preferences
		if (configChanged) {
			Log.d("Play", "Config changed detected in onResume, reloading keyboard");
			configChanged = false; // Reset the flag
			unbindDrawables(mBoard);
			System.gc();
			loadKeyboard();
		}
	}
	
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// This method is kept for compatibility but onResume now handles the reload
		if (configChanged) {
			Log.d("Play", "Config changed detected in onOptionsMenuClosed");
			// Don't reload here, let onResume handle it
		}
	}
	
	public void unbindDrawables(View view) {
		if (view.getBackground() != null)
			view.getBackground().setCallback(null);
		
		if (view instanceof ImageView) {
			ImageView imageView = (ImageView) view;
			imageView.setImageBitmap(null);
		} else if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;
			for (int i = 0; i < viewGroup.getChildCount(); i++)
				unbindDrawables(viewGroup.getChildAt(i));
		
		if (!(view instanceof AdapterView))
			viewGroup.removeAllViews();
		}
	}
	
	// Clean all playing states (eg: sounds playing, etc)
	public void cleanStates() {
		HexKeyboard.stopAll();
	}
	
	@Override
	protected void onPause() {
		// If app is closed/minimized (home button is pressed)
		//if (this.isFinishing()){ // The function isFinishing() returns a boolean. True if your App is actually closing, False if your app is still running but for example the screen turns off.
			// Clean all playing states
			this.cleanStates();
		//}
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		this.cleanStates();
		super.onDestroy();
	}
}
