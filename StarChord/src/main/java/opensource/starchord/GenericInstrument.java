/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                   *
 *   Copyleft  @ 2013 Stephen Larroque                                     *
 *                                                                         *
 *   FILE: GenericInstrument.java                                          *
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
 *   NOTE: The sounds used for this instrument were derived from           *
 *   the acoustic-piano soundfont created by Roberto Gordo Saez.
 *   Here is its license, which we will display more prominently
 *   in the application and our web site, as soon as we get 
 *   organized:
 *   
Acoustic grand piano soundfont (Yamaha Disklavier Pro), release 2008-09-10
116 samples, 44100Hz, 16bit.

The acoustic grand piano soundfont is free. It is built from the Zenph
Studios Yamaha Disklavier Pro Piano Multisamples for OLPC.

The soundfont itself and all modifications made to the original
samples by Roberto Gordo Saez, published under a Creative Commons
Attribution 3.0 license.

Copyright 2008, Roberto Gordo Saez roberto.gordo@gmail.com Creative 
Commons Attribution 3.0 license http://creativecommons.org/licenses/by/3.0/

Zenph Studios Yamaha Disklavier Pro Piano Multisamples for OLPC:

A collection of Grand Piano samples played by a Yamaha Disklavier
Pro. Performed by computer and specifically recorded for OLPC by
Dr. Mikhail Krishtal, Director of Music Research and Production, and
his team at Zenph Studios. They are included in the OLPC sound
sample library.

How is it being done: "The Disklavier Pro has an internal
electronically-controlled mechanism that allows it to play sounds
with very precise specifications. It has its own file format known
as XP MIDI, an extension of standard midi. I Mikhail Krishtal
prepare the files for it to play -- in this case, representing notes
of different registers, durations, and dynamic levels."

http://csounds.com/olpc/pianoSamplesMikhail/pianoMikhail.html
Produced by Zenph Studios in Chapel Hill, North Carolina. The main
studio location is in Raleigh, North Carolina.

http://zenph.com/
Samples from the OLPC sound sample library:

This huge collection of new and original samples have been donated
to Dr. Richard Boulanger @ cSounds.com specifically to support the
OLPC developers, students, XO users, and computer and electronic
musicians everywhere. They are FREE and are offered under a CC-BY
license.

http://wiki.laptop.org/go/Sound_samples http://csounds.com/boulanger

Creative Commons Attribution 3.0 license
http://creativecommons.org/licenses/by/3.0/

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package opensource.hexiano;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class GenericInstrument extends Instrument
{

	public GenericInstrument(Context context, String instrument) throws IllegalAccessException
	{
		super(context);
		
		this.mExternal = true;
		this.mInstrumentName = instrument;

		Pattern pat = Pattern.compile("m([0-9]+)(v([0-9]+))?.*\\.[^\\.]*$"); // Pattern: anythingyouwant_mxxvyy.ext where xx is the midi note, and yy the velocity (velocity is optional)
		File[] files = listExternalFiles(instrument+"/"); // Get the list of all files for this instrument (folder)
		sounds_to_load = new TreeMap<Integer, List<ArrayList>>();
		// For each file in this folder/instrument
		for (File file : files)
		{
		    String fileName = file.getName();
		    Log.d("GenericInstrument", "Found file: "+fileName);
		    // If we find a midi note (matching the regexp)
			int midiNoteNumber;
			Matcher mat = pat.matcher(fileName);
			if (mat.find())
			{
				// Parse the filename string to get the midi note
				String midiNoteNumberStr = mat.group(1);
				midiNoteNumber = Integer.parseInt(midiNoteNumberStr);
				String filePath = file.getAbsolutePath();
        		int velocity = 127;
        		if (mat.groupCount() > 2 && mat.group(2) != null) {
        			velocity = Integer.parseInt(mat.group(3));
        		}
				Log.d("GenericInstrument", "Found midi note: "+midiNoteNumberStr + " velocity " + Integer.toString(velocity) + " filepath " + filePath);
				// And store it inside the array sounds with the midiNoteNumber being the id and filePath the resource to load
				ArrayList tuple = new ArrayList(); // Use a generic tuple arraylist because here we will store the filepath string to the sound to be loaded for this note (SoundPool accepts filepath as an argument)
				tuple.add(midiNoteNumber);
				tuple.add(velocity);
				tuple.add(filePath);
				if (sounds_to_load.containsKey(midiNoteNumber)) {
					List<ArrayList> temp = sounds_to_load.get(midiNoteNumber);
					temp.add(tuple);
					sounds_to_load.put(midiNoteNumber, temp);
				} else {
					List<ArrayList> temp = new ArrayList<ArrayList>();
					temp.add(tuple);
					sounds_to_load.put(midiNoteNumber, temp);
				}
				// Also set this note as a root note (Root Notes are notes we have files for, from which we will extrapolate other notes that are missing if any)
				mRootNotes.put(midiNoteNumber, midiNoteNumber);
				// Rate to play the file with, default is always used for root notes, we only change the rate on other notes (where we don't have a file available) to interpolate the frequency and thus the note
				mRates.put(midiNoteNumber, 1.0f);
			}
		}
		
		// No sounds found? Show an error message then quit
		if (sounds_to_load.size() == 0) {
			// TODO: a better error dialog with nice OK button
			Toast.makeText(context, mInstrumentName + ": " + R.string.error_no_soundfiles, Toast.LENGTH_LONG).show();
			return;
		}

		// Extrapolate missing notes (for which we have no sound file) from available sound files
		extrapolateSoundNotes();
	}

	// List all external (eg: sd card) instruments (just the name of the subfolders)
	public static ArrayList<String> listExternalInstruments() {
		return GenericInstrument.listExternalInstruments("/hexiano/");
	}

	// List all external instruments (just the name of the subfolders)
	public static ArrayList<String> listExternalInstruments(String path) {
		ArrayList<String> Directories = new ArrayList<String>();
		if (path.length() != 0) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				path = Environment.getExternalStorageDirectory().toString()+path;
				Log.d("GenericInstrument::listExternalInstruments", "GenericInstrument: list external instruments (directories) from path: "+path);

			    File f = new File(path);
			    File[] files = f.listFiles();
			    if (files != null && files.length > 0) {
				    for (File file : files) {
				        if (file.isDirectory()) { // is directory
				        	Directories.add(file.getName());
				        }
				    }
			    }
			}
		}
	    return Directories;
	}

	// List all files in the external instrument's folder
	public static File[] listExternalFiles(String path) {
		File[] Files = null;
		if (path.length() != 0) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				path = Environment.getExternalStorageDirectory().toString()+"/hexiano/"+path;
				Log.d("GenericInstrument::listExternalFiles", "GenericInstrument: list external files from path: "+path);

			    File f = new File(path);
			    Files = f.listFiles();
			}
		}
		return Files;
	}

}
