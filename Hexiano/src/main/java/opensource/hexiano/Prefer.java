/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                   *
 *   Copyleft  @ 2013 Stephen Larroque                                     *
 *   Copyright © 2012 James Haigh                                          *
 *   Copyright © 2011, 2012 David A. Randolph                              *
 *                                                                         *
 *   FILE: Prefer.java                                                     *
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
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.preference.*;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import opensource.hexiano.R;

public class Prefer extends PreferenceActivity
{

	static SharedPreferences mPrefs;
	static SharedPreferences.Editor mPrefsEditor;
	
	static String multiInstrumentsSeparator = "|";
	static String multiInstrumentsAttrSeparator = ";";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		mPrefsEditor = mPrefs.edit();
		
		mPrefsEditor.putBoolean("multiInstrumentsEditClick", false);
		mPrefsEditor.commit();
		
		// Update instruments list dynamically
		ArrayList<String> externalInstruments = GenericInstrument.listExternalInstruments(); // Get the list of external instruments (eg: sd card)
		String[] staticInstruments = getApplicationContext().getResources().getStringArray(R.array.instruments); // Get the list of static instruments in APK (defined in preferences.xml and strings.xml)
		int pos = 0; // used to prepend in the right order (from first static instrument to the last set in config)
		for (String instr : staticInstruments) { // prepend static instruments before external instruments
			externalInstruments.add(pos++, instr);
		}
		final CharSequence[] entries = externalInstruments.toArray(new CharSequence[externalInstruments.size()]); // convert to a CharSequence (necessary for setEntries() and setEntryValues())
		ListPreference lp = (ListPreference)findPreference("instrument"); // get the ListPreference instrument item
		ListPreference lpm = (ListPreference)findPreference("minstrument"); // same for multi instruments chooser
		lp.setEntries(entries); // set the human readable (labels) entries
		lp.setEntryValues(entries); // set the values for these entries (the same in fact as the labels)
		lpm.setEntries(entries);
		lpm.setEntryValues(entries);
		
		// Update the list of saved mapping profiles (in both the Load mapping and Delete mapping ListPreferences)
		memoUpdateLoadMultiInstrumentsMapping(mPrefs);
		
		// On menu access, check if multiInstrumentsEnable is checked and enable/disable the correct entries
		Boolean isMultiInstrumentsEnabled = Prefer.mPrefs.getBoolean("multiInstrumentsEnable", false);
        Preference instrument = findPreference("instrument");
		Preference multiInstrumentsScreen = findPreference("multiInstrumentsScreen");
        if (isMultiInstrumentsEnabled) {
			multiInstrumentsScreen.setEnabled(true);
		} else {
			multiInstrumentsScreen.setEnabled(false);
		}
		
		// On enabling/disabling multi instruments, enable/disable the correct entries
		Preference multiInstrumentsEnable = findPreference("multiInstrumentsEnable");
		multiInstrumentsEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference multiInstrumentsEnable, Object newValue)
			{
				Log.d("Instrument", "onPreferenceClick, multiInstrumentsEnable");
				if(newValue instanceof Boolean){
		            Boolean boolVal = (Boolean)newValue;
		            Preference instrument = findPreference("instrument");
					Preference multiInstrumentsScreen = findPreference("multiInstrumentsScreen");
		            if (boolVal) {
						multiInstrumentsScreen.setEnabled(true);
					} else {
						multiInstrumentsScreen.setEnabled(false);
					}
		        }
				return true;
			}
			
		});
		
		// Multi instruments add/edit instrument submit button
		Preference multiInstrumentsSubmit = (Preference)findPreference("multiInstrumentsSubmit");
		multiInstrumentsSubmit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		                @Override
		                public boolean onPreferenceClick(Preference multiInstrumentsSubmit) {
		                	boolean error = false;
		                	
		                	// -- Fetch user's inputs
		                	// Instrument name
		                	String minstrumentStr = mPrefs.getString("minstrument", "");

		                	// Keys range (where the instrument will be mapped on the keyboard)
		                	String multiInstrumentsRangeStr = mPrefs.getString("multiInstrumentsRange", "");
		                	Pattern pat = Pattern.compile("(K|C|R)([0-9]+)-([0-9]+)", Pattern.CASE_INSENSITIVE);
		                	Matcher mat = pat.matcher(multiInstrumentsRangeStr);
		                	if (!mat.find())
		        			{
		                		error = true;
		                		Toast.makeText(getBaseContext(), "Error: invalid Range", Toast.LENGTH_LONG).show();
		                		return false;
		        			} else {
		        				if (Integer.parseInt(mat.group(3)) < Integer.parseInt(mat.group(2))) {
		        					error = true;
		        					Toast.makeText(getBaseContext(), "Error: invalid Range", Toast.LENGTH_LONG).show();
			                		return false;
		        				}
		        			}

		                	// Base note
		                	String mbaseNoteStr = mPrefs.getString("mbaseNote", "");
		                	
		                	// Base octave
		                	String mbaseOctaveStr = mPrefs.getString("mbaseOctave", "");
		                	if (mbaseOctaveStr.replaceAll("[^0-9]", "").length() == 0) {
		                		error = true;
	        					Toast.makeText(getBaseContext(), "Error: invalid Base Octave", Toast.LENGTH_LONG).show();
		                		return false;
		                	}
		                	
		                	// Use layout's base note and octave (skip multi-instrument baseNote and baseOctave settings)
		                	boolean miKeepBaseNoteOctave = mPrefs.getBoolean("multiInstrumentsKeepBaseNoteOctave", false);
		                	String miKeepBaseNoteOctaveStr = null;
		                	if (miKeepBaseNoteOctave) {
		                		miKeepBaseNoteOctaveStr = "true";
		                	} else {
		                		miKeepBaseNoteOctaveStr = "false";
		                	}
		                	
		                	// -- Save the new mapping
		                	String[] attributes_mapping = new String[] {minstrumentStr, multiInstrumentsRangeStr, mbaseNoteStr, mbaseOctaveStr, miKeepBaseNoteOctaveStr};
		                	if (((String)multiInstrumentsSubmit.getTitle()).equalsIgnoreCase(getResources().getString(R.string.add))) {
			                	// Add mode: Append the new mapping and save the new config
			                	error = error | !appendMultiInstrumentsMapping(mPrefs, mPrefsEditor, attributes_mapping);
		                	} else if (((String)multiInstrumentsSubmit.getTitle()).equalsIgnoreCase(getResources().getString(R.string.edit))) {
		                		// Edit mode: Replace the instrument mapping for this mapping id
		                		int id = mPrefs.getInt("multiInstrumentsEditId", -1);
		                		if (id < 0) {
		                			error = true;
		                		} else {
		                			error = error | !updateMultiInstrumentsMapping(mPrefs, mPrefsEditor, id, attributes_mapping);
		                		}
		                	}

		                	// Refresh previous screen (listing current multi instruments mapping)
		                	refreshPreferenceScreenByClick("multiInstrumentsScreen");
		            		// Go back to the previous screen
		                	closePreferenceScreen("multiInstrumentsAddScreen");
		                	// Alternative: programmatically open the previous screen, but this doesn't trim the history of preference screens, thus this nested preference screen will be kept in memory and after 2 or 3 instruments adding, the app will crash because of stack overflow.
		                	// openPreference("multiInstrumentsScreen"); // open multiInstrumentsScreen to update the list of mappings

		                    return !error;
		                }
		            });
		
		// Multi instruments delete button
		Preference multiInstrumentsDelete = findPreference("multiInstrumentsDelete");
		multiInstrumentsDelete.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // Defining delete button action
            @Override
            public boolean onPreferenceClick(Preference multiInstrumentsDelete) {
            	boolean error = false;
            	int id = mPrefs.getInt("multiInstrumentsEditId", -1);
            	if (id < 0) {
            		error = true;
            	} else {
                	deleteMultiInstrumentsMapping(mPrefs, mPrefsEditor, id); // delete current instrument from the mapping
            	}

            	// Refresh previous screen (listing current multi instruments mapping)
            	refreshPreferenceScreenByClick("multiInstrumentsScreen");
        		// Go back to the previous screen
            	closePreferenceScreen("multiInstrumentsAddScreen");
            	// Alternative: programmatically open the previous screen, but this doesn't trim the history of preference screens, thus this nested preference screen will be kept in memory and after 2 or 3 instruments adding, the app will crash because of stack overflow.
            	// openPreference("multiInstrumentsScreen"); // open multiInstrumentsScreen to update the list of mappings

            	return !error;
            }
		});
		
		// Multi instruments submenu show entries (list of instruments)
		multiInstrumentsScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		                @Override
		                public boolean onPreferenceClick(Preference multiInstrumentsScreen) {
		                	// Get the mapping config
		                	String[] mapping = Prefer.getMultiInstrumentsMapping(mPrefs);
		                	// If there is a mapping
		                	if (mapping != null) {
		                		// We will later append Preference entries inside the PreferenceCategory
		                		PreferenceCategory heading_mi = (PreferenceCategory)findPreference("heading_multiInstruments");
		                		heading_mi.removeAll(); // first clean off all previously computed entries
		                		onContentChanged();
		                		System.gc();

		                		// Walk through each mapping and create a Preference entry
		                		for (int i=0;i < mapping.length;i++) {
		                			// Extract attributes of this mapping
		                			HashMap<String, String> mappingattr = Prefer.getMultiInstrumentsMappingForId(i);
		                			
		                			// Setup the Preference entry
		                			//PreferenceScreen instru = getPreferenceManager().createPreferenceScreen(getBaseContext()); // Create a new PreferenceScreen entry
		                			Preference instru = new Preference(getBaseContext()); // Create a new Preference (just a button)
		                			instru.setKey("minstru_"+Integer.toString(i));
		                			instru.setTitle("ID " + Integer.toString(i) + ": " + mappingattr.get("instrument"));
		                			String baseNoteOctaveDesc = mappingattr.get("keepBaseNoteOctave").equals("true") ? "BaseNote and BaseOctave: same as layout" : "BaseNote:" + mappingattr.get("baseNote") + " BaseOctave:" + mappingattr.get("baseOctave");
		                			instru.setSummary(baseNoteOctaveDesc + " KeyRange:" + mappingattr.get("rangeType") + " from " + mappingattr.get("rangeStart") + " to " + mappingattr.get("rangeEnd"));

		                			// On click: edit this entry and open same submenu as multiInstrumentsAddScreen + hidden ID + delete button
		                			instru.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		        		                @Override
		        		                public boolean onPreferenceClick(Preference instru) {
		        		                	// Search Id in menu entry title
		        		                	String instruTitle = (String) instru.getTitle();
		        		                	Pattern pat = Pattern.compile("ID\\s+([0-9]+)", Pattern.CASE_INSENSITIVE);
		        		                	Matcher mat = pat.matcher(instruTitle);
		        		                	if (mat.find()) {
		        		                		// Extract id from menu entry title
			        		                	int id = Integer.parseInt(mat.group(1));

			        		                	// Extract attributes for this mapping
					                			HashMap<String, String> mappingattr = Prefer.getMultiInstrumentsMappingForId(id);

					                			// Update values in shared preferences
					                			mPrefsEditor.putInt("multiInstrumentsEditId", id); // Store the id
			        		                	mPrefsEditor.putString("minstrument", mappingattr.get("instrument"));
			        		                	mPrefsEditor.putString("multiInstrumentsRange", mappingattr.get("range"));
			        		                	mPrefsEditor.putString("mbaseNoteStr", mappingattr.get("baseNote"));
			        		                	mPrefsEditor.putString("mbaseOctaveStr", mappingattr.get("baseOctave"));
			        		                	mPrefsEditor.putBoolean("multiInstrumentsKeepBaseNoteOctave", mappingattr.get("keepBaseNoteOctave").equals("true") ? true : false);
			        		                	mPrefsEditor.commit(); // commit is not enough to update preference value without reloading PreferenceActivity. Maybe try .apply() instead?
			        		                	// Note that this hack will change the preferences settings twice, which will trigger onSharedPreferenceChanged() even if it's unintended since the user didn't change the config, but we did just to configure the interface
			        		                	// First preferences settings change happens here just after commit()
			        		                	
			        		                	// Hack to force update of new preference values without reloading PreferenceActivity
			        		                	// ref: http://liquidlabs.ca/2011/08/25/update-preference-value-without-reloading-preferenceactivity/
			        		                	ListPreference minstrument = (ListPreference)findPreference("minstrument");
					                			EditTextPreference multiInstrumentsRange = (EditTextPreference)findPreference("multiInstrumentsRange");
					                			ListPreference mbaseNote = (ListPreference)findPreference("mbaseNote");
					                			ListPreference mbaseOctave = (ListPreference)findPreference("mbaseOctave");
					                			CheckBoxPreference miKeepBaseNoteOctave = (CheckBoxPreference)findPreference("multiInstrumentsKeepBaseNoteOctave");
					                			minstrument.setValue(mappingattr.get("instrument"));
					                			multiInstrumentsRange.setText(mappingattr.get("range"));
					                			mbaseNote.setValue(mappingattr.get("baseNote"));
					                			mbaseOctave.setValue(mappingattr.get("baseOctave"));
					                			miKeepBaseNoteOctave.setChecked(mappingattr.get("keepBaseNoteOctave").equals("true") ? true : false);
					                			// Second preferences settings change happens here
					                			
					                			// -- Prepare the MultiInstrumentsAddScreen
					                			// Change submit button into edit
					                			Preference multiInstrumentsSubmit = findPreference("multiInstrumentsSubmit");
					                			multiInstrumentsSubmit.setTitle(R.string.edit);
					                			
					                			// Change title
					                			//PreferenceScreen multiInstrumentsAddScreen = (PreferenceScreen)findPreference("multiInstrumentsAddScreen");
					                			//multiInstrumentsAddScreen.setTitle(getResources().getString(R.string.multi_instruments_add_screen_title_edit) + " ID " + Integer.toString(id));
					                			
					                			// Show delete button (recreate it if hidden/removed because Android doesn't let hide preferences but only remove and add them)
					                			PreferenceScreen multiInstrumentsAddScreen = (PreferenceScreen)findPreference("multiInstrumentsAddScreen");
					                			Preference multiInstrumentsDelete = findPreference("multiInstrumentsDelete");
					                			/*
					                			if (multiInstrumentsDelete == null) {
					                				multiInstrumentsDelete = new Preference(getBaseContext()); // Create a new Preference (just a button)
					                				multiInstrumentsDelete.setKey("multiInstrumentsDelete");
					                				multiInstrumentsDelete.setTitle(R.string.delete);
					                				multiInstrumentsDelete.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // Defining delete button action
						        		                @Override
						        		                public boolean onPreferenceClick(Preference multiInstrumentsDelete) {
						        		                	int id = mPrefs.getInt("multiInstrumentsEditId", -1);
						        		                	if (id < 0) {
						        		                		return false;
						        		                	} else {
							        		                	deleteMultiInstrumentsMapping(id); // open multiInstrumentsScreen to update the list of mappings
							        		                	openPreference("multiInstrumentsScreen");
							        		                	return true;
						        		                	}
						        		                }
					                				});
					                			}
					                			*/
					                			multiInstrumentsDelete.setTitle(R.string.delete);
					                			multiInstrumentsDelete.setEnabled(true);
					                			//multiInstrumentsAddScreen.addPreference(multiInstrumentsDelete); // Finally, add the delete button in the screen
					                			multiInstrumentsDelete.setDefaultValue((int)(Math.random()*1000)); onContentChanged(); // force refresh of the delete button
					                			
					                			// Redirect on click to add instrument preference screen (that we above modified a bit to turn into an edit instrument preference screen)
					                			mPrefsEditor.putBoolean("multiInstrumentsEditClick", true); // Trick to know if we are simulating a click (editing) or user really clicked on Add (adding a new instrument)
					                			mPrefsEditor.commit();
					                			openPreference("multiInstrumentsAddScreen"); // open the sub PreferenceScreen
		        		                	}

		        		                	return true;
		        		                }
		        		            });
		                			
		                			// Finally, add this Preference entry
		                			heading_mi.addPreference(instru);
		                		}
		                		
		                	}
		                	return true;
		                }
		});
		
		// MultiInstruments add instrument submenu (resets buttons titles and hide some buttons)
		Preference multiInstrumentsAddScreen = (PreferenceScreen)findPreference("multiInstrumentsAddScreen");
		multiInstrumentsAddScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
            public boolean onPreferenceClick(Preference multiInstrumentsAddScreen) {
				
				Boolean editClick = mPrefs.getBoolean("multiInstrumentsEditClick", false); // Trick to know if we are simulating a click (editing) or user really clicked on Add (adding a new instrument)
				if (editClick) { // Edit mode, we just reset the editClick
					mPrefsEditor.putBoolean("multiInstrumentsEditClick", false); // Reset back to add mode for next time
	    			mPrefsEditor.commit();
				} else { // Not in edit mode, we reset buttons to add mode
					
        			// Change title
        			//multiInstrumentsAddScreen.setTitle(getResources().getString(R.string.multi_instruments_add_screen_title_add));

					// Change submit button to add
					Preference multiInstrumentsSubmit = findPreference("multiInstrumentsSubmit");
	    			multiInstrumentsSubmit.setTitle(R.string.add);
	
	    			// Hide delete button
	    			Preference multiInstrumentsDelete = findPreference("multiInstrumentsDelete");
	    			multiInstrumentsDelete.setTitle("");
	    			multiInstrumentsDelete.setEnabled(false);
	    			/*
	    			if (multiInstrumentsDelete != null) {
	    				multiInstrumentsDelete.setEnabled(false);
	    				((PreferenceScreen) multiInstrumentsAddScreen).removePreference(multiInstrumentsDelete);
	    				multiInstrumentsDelete.setDefaultValue((int)(Math.random()*1000)); onContentChanged(); // force refresh of the delete button
	    			}
	    			*/
				}
				
				return true;
			}
		});
		
		// Multi instruments clear mapping button
		Preference multiInstrumentsClear = findPreference("multiInstrumentsClear");
		multiInstrumentsClear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
            public boolean onPreferenceClick(Preference multiInstrumentsClear) {
				
				clearMultiInstrumentsMapping(mPrefsEditor);
				
				PreferenceCategory heading_mi = (PreferenceCategory)findPreference("heading_multiInstruments");
        		heading_mi.removeAll(); // first clean off all previously computed entries
        		onContentChanged();
        		System.gc();

        		refreshPreferenceScreenByClick("multiInstrumentsScreen");
				//openPreference("multiInstrumentsScreen"); // open multiInstrumentsScreen to update the list of mappings
				
				return true;
			}
		});
		
		// Multi instruments save mapping button
		Preference multiInstrumentsSaveProfile = findPreference("multiInstrumentsSaveProfile");
		multiInstrumentsSaveProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
            public boolean onPreferenceChange(Preference multiInstrumentsSaveProfile, Object newValue)
			{
				if(newValue instanceof String){
		            String mappingTitle = (String)newValue;
		            String miConf = getMultiInstrumentsConf(mPrefs);
				
		            memoAppendMultiInstrumentsMapping(mPrefs, mPrefsEditor, miConf, mappingTitle); // Save the mapping profile
		            memoUpdateLoadMultiInstrumentsMapping(mPrefs); // Refresh the list of saved mappings
				}
				
				return true;
			}
		});
		
		// Multi instruments load mapping button
		Preference multiInstrumentsLoadProfile = findPreference("multiInstrumentsLoadProfile");
		multiInstrumentsLoadProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
            public boolean onPreferenceChange(Preference multiInstrumentsLoadProfile, Object newValue) {
				if(newValue instanceof String){
		            String mappingValue = (String)newValue;
		            if (mappingValue.length() > 0) {
			            mPrefsEditor.putString("multiInstrumentsConfig", mappingValue);
			            if (mPrefsEditor.commit()) refreshPreferenceScreenByClick("multiInstrumentsScreen"); //openPreference("multiInstrumentsScreen"); // force refresh the list of instruments
		            } else {
		            	return false;
		            }
				}
				
				return true;
			}
		});
		
		// Multi instruments delete mapping button
		Preference multiInstrumentsDeleteProfile = findPreference("multiInstrumentsDeleteProfile");
		multiInstrumentsDeleteProfile.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
            public boolean onPreferenceChange(Preference multiInstrumentsDeleteProfile, Object newValue) {
				if(newValue instanceof String){
		            String mappingValue = (String)newValue;
		            if (mappingValue.length() > 0) {
		            	boolean retcode = memoDeleteMultiInstrumentsMapping(mPrefs, mPrefsEditor, mappingValue);
		            	memoUpdateLoadMultiInstrumentsMapping(mPrefs); // Refresh the list of saved mappings
		            	return retcode;
		            } else {
		            	return false;
		            }
				}
				
				return true;
			}
		});

		// Links.
		// FIXME: These links should be in the XML if only I new how.
		// <ugly>
		/*
		Preference donate = findPreference("donate");
		donate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference donate)
				{
					Log.d("Prefer", "onPreferenceClick, donate");
					Uri webpage = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=2UZVTYPPP9AUG&lc=GB&item_name=Hexiano%2Eorg&currency_code=GBP");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		*/
		Preference issue_45 = findPreference("issue-45");
		issue_45.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference issue_45)
				{
					Log.d("Prefer", "onPreferenceClick, issue_45");
					Uri webpage = Uri.parse("https://sourceforge.net/p/isokeys/tickets/45/");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		Preference issue_18812 = findPreference("issue-18812");
		issue_18812.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference issue_18812)
				{
					Log.d("Prefer", "onPreferenceClick, issue_18812");
					Uri webpage = Uri.parse("https://code.google.com/p/android/issues/detail?id=18812");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		Preference issue_30198 = findPreference("issue-30198");
		issue_30198.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference issue_30198)
				{
					Log.d("Prefer", "onPreferenceClick, issue_30198");
					Uri webpage = Uri.parse("https://code.google.com/p/android/issues/detail?id=30198");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		Preference issue_10176 = findPreference("issue-10176");
		issue_10176.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference issue_10176)
				{
					Log.d("Prefer", "onPreferenceClick, issue_10176");
					Uri webpage = Uri.parse("https://code.google.com/p/android/issues/detail?id=10176");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		Preference issue_8201 = findPreference("issue-8201");
		issue_8201.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference issue_8201)
				{
					Log.d("Prefer", "onPreferenceClick, issue_8201");
					Uri webpage = Uri.parse("https://code.google.com/p/android/issues/detail?id=8201");
					Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
					startActivity(webIntent);
					return false;
				}
			}
		);
		// </ugly>
	}
	
	public static String getMultiInstrumentsConf(SharedPreferences mPrefs) {
		return mPrefs.getString("multiInstrumentsConfig", "");
	}
	
	// Fetch all instrument's mappings (global multi-instruments mapping)
	public static String[] getMultiInstrumentsMapping(SharedPreferences mPrefs) {
		// Get the mapping config
    	String miConf = mPrefs.getString("multiInstrumentsConfig", "");
    	// Remove null/empty entries (because join will not skip a null/empty string) and don't forget to escape the separator if necessary!
    	miConf = cleanConfRegexp(miConf);
    	// If there is a mapping
    	if (miConf.length() > 0) {
    		// Extract infos about each mapping
    		String[] mapping = miConf.split("\\"+multiInstrumentsSeparator);

    		return mapping;
    	} else {
    		return null;
    	}
	}

	// Convert a mapping of one instrument from String[] array to HashMap
	// You can here add new attributes easily
	public static HashMap<String, String> mappingArrayToHashmap(String mapping, int id) {
		// Create the hashmap (dictionary) that will store all these attributes (more manageable to rely on string index in case someone later wants to add or remove attributes than relying on integer position in array)
		HashMap<String, String> mappingattr = new HashMap<String, String>();
		// Extract base attributes of this mapping
		String[] attr = mapping.split(multiInstrumentsAttrSeparator);
		mappingattr.put("id", Integer.toString(id));
		mappingattr.put("instrument", attr[0]); // instrument's name (and also instrument's identifier)
		mappingattr.put("range", attr[1]); // instrument's range (keys mapping)
		mappingattr.put("baseNote", attr[2]); // base note
		mappingattr.put("baseOctave", attr[3]); // base octave
		mappingattr.put("keepBaseNoteOctave", attr[4]); // keep layout's base note and octave (skip the multi-instrument settings for baseNote and baseOctave)
		// Additional attributes, mostly metadata or more easier to use attributes extracted from base attributes
		Pattern pat = Pattern.compile("(K|C|R)([0-9]+)-([0-9]+)", Pattern.CASE_INSENSITIVE);
    	Matcher mat = pat.matcher(mappingattr.get("range"));
    	if (mat.find()) {
    		if (mat.group(1).equals("K")) {
    			mappingattr.put("rangeType", "Key");
    		} else if (mat.group(1).equals("C")) {
    			mappingattr.put("rangeType", "Column");
    		} else if (mat.group(1).equals("R")) {
    			mappingattr.put("rangeType", "Row");
    		} else {
    			mappingattr.put("rangeType", mat.group(1));
    		}
			mappingattr.put("rangeStart", mat.group(2));
			mappingattr.put("rangeEnd", mat.group(3));
    	}
		
		return mappingattr;
	}
	
	// Fetch one instrument's mapping given the id
	public static HashMap<String, String> getMultiInstrumentsMappingForId(int id) {
		// Get mappings
		String[] mapping = getMultiInstrumentsMapping(mPrefs);
		// If mapping[id] exists
		if (mapping.length > 0 && id < mapping.length) {
			// Extract attributes of this mapping
			HashMap<String, String> mappingattr = mappingArrayToHashmap(mapping[id], id);
			
			return mappingattr;
		} else {
			return null;
		}
	}
	
	// Fetch all instruments mappings with a nice hashmap (so that attributes are always accessible in the same way, no need to be careful about position in an array)
	public static TreeMap<Integer, HashMap<String, String>> getMultiInstrumentsMappingHashMap(SharedPreferences mPrefs) {
		String[] mapping = getMultiInstrumentsMapping(mPrefs);
		
		if (mapping != null && mapping.length > 0) {
			TreeMap<Integer, HashMap<String, String>> mappingfinal = new TreeMap<Integer, HashMap<String, String>>();
			for (int i=0;i<mapping.length;i++) {
				HashMap<String, String> mappingattr = mappingArrayToHashmap(mapping[i], i);
				mappingfinal.put(i, mappingattr);
			}
			return mappingfinal;
		} else {
			return null;
		}
	}
	
	// Append an instrument's mapping into the global multi-instruments mapping
	// Arguments: you can pass directly the strings instead of a string array
	public static boolean appendMultiInstrumentsMapping (SharedPreferences mPrefs, SharedPreferences.Editor mPrefsEditor, String minstrumentStr, String multiInstrumentsRangeStr, String mbaseNoteStr, String mbaseOctaveStr, String miKeepBaseNoteOctaveStr) {
    	// Concatenate all attributes with another separator and plug them in the string
    	String[] attributes_mapping = new String[] {minstrumentStr, multiInstrumentsRangeStr, mbaseNoteStr, mbaseOctaveStr, miKeepBaseNoteOctaveStr};
    	return appendMultiInstrumentsMapping(mPrefs, mPrefsEditor, attributes_mapping);
	}
	
	// Append an instrument's mapping into the global multi-instruments mapping
	// attributes_mapping = String[] of attributes for ONE instrument
	public static boolean appendMultiInstrumentsMapping (SharedPreferences mPrefs, SharedPreferences.Editor mPrefsEditor, String[] attributes_mapping) {
    	// Fetch the previous config
    	String miConf = mPrefs.getString("multiInstrumentsConfig", "");
    	
    	// Add a separator if there was a previous instrument mapping
    	if (miConf.length() != 0) miConf = miConf + multiInstrumentsSeparator;
    	
    	// Concatenate all attributes with another separator and plug them in the string
    	miConf = miConf + android.text.TextUtils.join(multiInstrumentsAttrSeparator, attributes_mapping);

    	// Save the new mapping config
    	mPrefsEditor.putString("multiInstrumentsConfig", miConf);
    	mPrefsEditor.commit();
    	
    	return true;
	}
	
	// Replace the global multi-instruments mapping given a new one
	public static boolean saveMultiInstrumentsMapping (SharedPreferences.Editor mPrefsEditor, String[] mapping) {
    	// Concatenate all entries in this mapping into a string
    	String miConf = android.text.TextUtils.join(multiInstrumentsSeparator, mapping);
    	
    	// Remove null/empty entries (because join will not skip a null/empty string)
    	miConf = cleanConfRegexp(miConf);

    	// Save the new mapping config
    	mPrefsEditor.putString("multiInstrumentsConfig", miConf);
    	mPrefsEditor.commit();
    	
    	return true;
	}
	
	// Replace an instrument's mapping given the id and a String[] of attributes (attributes mapping)
	public static boolean updateMultiInstrumentsMapping(SharedPreferences mPrefs, SharedPreferences.Editor mPrefsEditor, int id, String[] attributes_mapping) {
		String[] mapping = getMultiInstrumentsMapping(mPrefs);
		if (id < mapping.length) {
			mapping[id] = android.text.TextUtils.join(multiInstrumentsAttrSeparator, attributes_mapping);
			saveMultiInstrumentsMapping(mPrefsEditor, mapping);
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean clearMultiInstrumentsMapping(SharedPreferences.Editor mPrefsEditor) {
		mPrefsEditor.putString("multiInstrumentsConfig", "");
		mPrefsEditor.commit();
		return true;
	}

	// Delete an instrument's mapping given the id
	public static boolean deleteMultiInstrumentsMapping(SharedPreferences mPrefs, SharedPreferences.Editor mPrefsEditor, int id) {
		String[] mapping = getMultiInstrumentsMapping(mPrefs);
		if (id < mapping.length) {
			if (mapping.length == 1) { // only one instrument left, we just clear the whole list
				clearMultiInstrumentsMapping(mPrefsEditor);
				return true;
			} else {
				mapping[id] = ""; // Do NOT put null here, because android.text.TextUtils.join() in saveMultiInstrumentsMapping will convert into the string "null"
				saveMultiInstrumentsMapping(mPrefsEditor, mapping);
				return true;
			}
		} else {
			return false;
		}
	}
	
	// Save a given set of mappings and replace all previous profiles
	public static boolean memoSaveMultiInstrumentsMapping(SharedPreferences.Editor mPrefsEditor, ArrayList<String> newentries, ArrayList<String> newentryvalues) {
		
		saveArray(mPrefsEditor, "multiInstrumentsMemoEntries", newentries.toArray(new String[newentries.size()]));
		saveArray(mPrefsEditor, "multiInstrumentsMemoValues", newentryvalues.toArray(new String[newentryvalues.size()]));

		return true;
	}
	
	// Save a mapping for future usage
	public static boolean memoAppendMultiInstrumentsMapping(SharedPreferences mPrefs, SharedPreferences.Editor mPrefsEditor, String miConf, String miTitle) {
		// store an array here in shared prefs
		ArrayList<String> newentries = loadArrayList(mPrefs, "multiInstrumentsMemoEntries");
		ArrayList<String> newentryvalues = loadArrayList(mPrefs, "multiInstrumentsMemoValues");
		newentries.add(miTitle);
		newentryvalues.add(miConf);
		
		memoSaveMultiInstrumentsMapping(mPrefsEditor, newentries, newentryvalues);

		/* Old method
		// Get old list
		ListPreference miLoad = (ListPreference)findPreference("multiInstrumentsLoadProfile"); // get the ListPreference instrument item
		CharSequence[] preventries = miLoad.getEntries();
		CharSequence[] preventryvalues = miLoad.getEntryValues();
		// Recreate a new dynamic arraylist and add back the old entries
		ArrayList<String> newentries = new ArrayList<String>();
		ArrayList<String> newentryvalues = new ArrayList<String>();
		int i = 0; // used to prepend in the right order (from first static instrument to the last set in config)
		if (preventries != null && preventries.length > 0) {
			for (i=0;i < preventries.length;i++) { // prepend static instruments before external instruments
				newentries.add(i, (String)preventries[i]);
				newentryvalues.add(i, (String)preventryvalues[i]);
			}
			i++; // for the new entries
		}
		// Then add the new entry
		newentries.add(i, miTitle);
		newentryvalues.add(i, miConf);
		
		// convert to a CharSequence (necessary for setEntries() and setEntryValues())
		final CharSequence[] entries = newentries.toArray(new CharSequence[newentries.size()]);
		final CharSequence[] entryValues = newentryvalues.toArray(new CharSequence[newentryvalues.size()]);

		// Finally set them in the preferences
		miLoad.setEntries(entries); // set the human readable (labels) entries
		miLoad.setEntryValues(entryValues); // set the values for these entries (mapping conf)
		*/

		return true;
	}
	
	// Update the multi instruments load mapping list and also the delete mapping list
	public boolean memoUpdateLoadMultiInstrumentsMapping(SharedPreferences mPrefs) {
		ArrayList<String> loadentries = loadArrayList(mPrefs, "multiInstrumentsMemoEntries");
		ArrayList<String> loadentryvalues = loadArrayList(mPrefs, "multiInstrumentsMemoValues");
		
		if (loadentries.size() == 0) {
			return false;
		} else {
			ListPreference miLoad = (ListPreference)findPreference("multiInstrumentsLoadProfile"); // get the ListPreference
			ListPreference miDelete = (ListPreference)findPreference("multiInstrumentsDeleteProfile"); 
			
			// convert to a CharSequence (necessary for setEntries() and setEntryValues())
			final CharSequence[] entries = loadentries.toArray(new CharSequence[loadentries.size()]);
			final CharSequence[] entryValues = loadentryvalues.toArray(new CharSequence[loadentryvalues.size()]);
	
			// Finally set them in the preferences
			miLoad.setEntries(entries); // set the human readable (labels) entries
			miLoad.setEntryValues(entryValues); // set the values for these entries (mapping conf)
			miDelete.setEntries(entries);
			miDelete.setEntryValues(entryValues);
			
			return true;
		}
	}
	
	// Load a previously memorized (saved) multi instruments mapping
	public static boolean memoLoadMultiInstrumentsMapping(SharedPreferences mPrefs, SharedPreferences.Editor mPrefsEditor, String miTitle) {
		ArrayList<String> loadentries = loadArrayList(mPrefs, "multiInstrumentsMemoEntries");
		ArrayList<String> loadentryvalues = loadArrayList(mPrefs, "multiInstrumentsMemoValues");
		
		int index = ListGetIndexOfValue(miTitle, loadentries);
		
		if (loadentries == null || loadentryvalues == null || loadentries.size() == 0 || index == -1 ) {
			return false;
		} else {
			
			mPrefsEditor.putString("multiInstrumentsConfig", loadentryvalues.get(index));
			mPrefsEditor.commit();
			
			return true;
		}
	}
	
	// Delete a memorized mapping
	// Note: we compare by value instead of title for two reasons: first this is the value given onChange event, but it is also more precise since multiple profiles can have the same title, but completely different mappings. And in fine, it's less bad to delete the wrong entry but which was a total duplicate by value (config) than the other way around.
	public static boolean memoDeleteMultiInstrumentsMapping(SharedPreferences mPrefs, SharedPreferences.Editor mPrefsEditor, String miValue) {
		ArrayList<String> loadentries = loadArrayList(mPrefs, "multiInstrumentsMemoEntries");
		ArrayList<String> loadentryvalues = loadArrayList(mPrefs, "multiInstrumentsMemoValues");

		int index = ListGetIndexOfValue(miValue, loadentryvalues);

		if (loadentryvalues == null || loadentryvalues.size() == 0 || index == -1 ) {
			return false;
		} else {
			
			loadentries.remove(index);
			loadentryvalues.remove(index);
			
			return memoSaveMultiInstrumentsMapping(mPrefsEditor, loadentries, loadentryvalues);
		}
	}
	
	// Clean artifacts from a multi instruments config (in concatenated String format, not array)
	public static String cleanConfRegexp (String miConf) {
		// Remove null/empty entries (because join will not skip a null/empty string) and don't forget to escape the separator if necessary!
    	miConf = miConf.replaceAll("\\"+multiInstrumentsSeparator + "\\"+multiInstrumentsSeparator, multiInstrumentsSeparator);
    	miConf = miConf.replaceAll("\\"+multiInstrumentsSeparator+"null"+"\\"+multiInstrumentsSeparator, multiInstrumentsSeparator);
    	miConf = miConf.replaceAll("^\\"+multiInstrumentsSeparator+"+", ""); // remove leading separators
    	miConf = miConf.replaceAll("\\"+multiInstrumentsSeparator+"+$", ""); // remove trailing separators
    	return miConf;
	}
	
	// Ref: http://www.sherif.mobi/2012/05/string-arrays-and-object-arrays-in.html
	public static boolean saveArray(SharedPreferences.Editor mPrefsEditor, String arrayName, String[] arrayValues) {
	    mPrefsEditor.putInt(arrayName +"_size", arrayValues.length);  
	    for(int i=0;i < arrayValues.length;i++)  
	        mPrefsEditor.putString(arrayName + "_" + i, arrayValues[i]);  
	    return mPrefsEditor.commit();  
	} 
	
	public static String[] loadArray(SharedPreferences mPrefs, String arrayName) {
	    int size = mPrefs.getInt(arrayName + "_size", 0);
	    if (size > 0) {
		    String array[] = new String[size];
		    for(int i=0;i<size;i++)
		        array[i] = mPrefs.getString(arrayName + "_" + i, null);
		    return array;
	    } else {
	    	return null;
	    }
	}
	
	public static ArrayList<String> loadArrayList(SharedPreferences mPrefs, String arrayName) {
	    int size = mPrefs.getInt(arrayName + "_size", 0);
	    ArrayList<String> array = new ArrayList<String>();
	    if (size > 0) {
		    for(int i=0;i<size;i++)
		        array.add(i, mPrefs.getString(arrayName + "_" + i, null));
	    }
	    return array;
	}
	
	public static int ListGetIndexOfValue(String value, List<String> list) {

	    int i = 0;
	    for (String entry : list) {
	        if (entry == value) {
	            return i;
	        } 
	        i++;
	    }
	    return -1;
	}

	/*
	public class PreferenceShow extends Preference {

		public PreferenceShow(Context context) {
			super(context);
		}
		
		public void show() {
			this.showDialog(null);
		}
		
	}
	*/
	
	// Open a preference item directly from string name
	// Thank's so much to markus ( http://stackoverflow.com/a/4869034/1121352 )
	// FIXME: getOrder() may not work when there are PreferenceGroups
	private PreferenceScreen findPreferenceScreenForPreference( String key, PreferenceScreen screen ) {
	    if( screen == null ) {
	        screen = getPreferenceScreen();
	    }

	    PreferenceScreen result = null;

	    android.widget.Adapter ada = screen.getRootAdapter();
	    for( int i = 0; i < ada.getCount(); i++ ) {
	        String prefKey = ((Preference)ada.getItem(i)).getKey();
	        if( prefKey != null && prefKey.equals( key ) ) {
	            return screen;
	        }
	        if( ada.getItem(i).getClass().equals(android.preference.PreferenceScreen.class) ) {
	            result = findPreferenceScreenForPreference( key, (PreferenceScreen) ada.getItem(i) );
	            if( result != null ) {
	                return result;
	            }
	        }
	    }

	    return null;
	}

	// Open a preference item directly from string name
	// Thank's so much to markus ( http://stackoverflow.com/a/4869034/1121352 )
	private void openPreference( String key ) {
	    PreferenceScreen screen = findPreferenceScreenForPreference( key, null );
	    if( screen != null ) {
	        screen.onItemClick(null, findPreference(key).getView(null, null), findPreference(key).getOrder(), 0);
	    }
	}
	
	private void openPreferenceAlt( String key, String parent ) {
		// the preference screen your item is in must be known
		PreferenceScreen screen = (PreferenceScreen) findPreference(parent);

		// the position of your item inside the preference screen above
		int pos = findPreference(key).getOrder();

		// simulate a click / call it!!
		screen.onItemClick( null, null, pos, 0 ); 
	}

	// Refresh the content of a PreferenceScreen by simulating a click (thus it will call the refresh code if it's contained inside the click callback)
	private void refreshPreferenceScreenByClick(String prefScreenName) {
		// Refresh preference screen (given by its name) by calling the click callback
		PreferenceScreen prefScreen = (PreferenceScreen) findPreference(prefScreenName);
		Preference.OnPreferenceClickListener click_callback = prefScreen.getOnPreferenceClickListener();
		click_callback.onPreferenceClick(prefScreen);
	}

	// Close the current PreferenceScreen (or any given the name)
	// Useful to go back to the previous PreferenceScreen when constructing dynamically nested submenus.
	private void closePreferenceScreen(String prefScreenName) {
		PreferenceScreen prefScreen = (PreferenceScreen) findPreference(prefScreenName);
    	prefScreen.getDialog().dismiss();
	}
}
