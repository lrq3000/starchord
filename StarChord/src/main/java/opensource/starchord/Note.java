/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                   *
 *   Copyleft  @ 2013 Stephen Larroque                                     *
 *   Copyright © 2012 James Haigh                                          *
 *   Copyright © 2011, 2012 David A. Randolph                              *
 *                                                                         *
 *   FILE: Note.java                                                       *
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

import java.util.HashMap;

public class Note
{
	protected int mOctave;
	protected String mFlatName;
    protected String mSharpName;
	protected int mMidiNoteNumber;
	protected int mKeyNumber; // Just for reference, can be shown as a label on the key, but useless otherwise for the Note
	
	static final HashMap<String, String> mToGerman;
	static
	{
		mToGerman = new HashMap<String, String>();
		mToGerman.put("A", "A");
		mToGerman.put("A#", "ais");
		mToGerman.put("Bb", "B");
		mToGerman.put("B", "H");
		mToGerman.put("C", "C");
		mToGerman.put("C#", "cis");
		mToGerman.put("Db", "des");
		mToGerman.put("D", "D");
		mToGerman.put("D#", "dis");
		mToGerman.put("Eb", "es");
		mToGerman.put("E", "E");
		mToGerman.put("F", "F");
		mToGerman.put("F#", "fis");
		mToGerman.put("Gb", "ges");
		mToGerman.put("G", "G");
		mToGerman.put("G#", "gis");
		mToGerman.put("Ab", "as");
	}

	// We should use \u266F for the sharp symbol, but this has a lot of
	// extra space around it for some reason. So, for now, we will just
	// use the # character.
	static final HashMap<String, String> mToSolfege;
	static
	{
		mToSolfege = new HashMap<String, String>();
		mToSolfege.put("A", "La");
		mToSolfege.put("A#", "La#");
		mToSolfege.put("Bb", "Si\u266D");
		mToSolfege.put("B", "Si");
		mToSolfege.put("C", "Do");
		mToSolfege.put("C#", "Do#");
		mToSolfege.put("Db", "Re\u266D");
		mToSolfege.put("D", "Re");
		mToSolfege.put("D#", "Re#");
		mToSolfege.put("Eb", "Mi\u266D");
		mToSolfege.put("E", "Mi");
		mToSolfege.put("F", "Fa");
		mToSolfege.put("F#", "Fa#");
		mToSolfege.put("Gb", "Sol\u266D");
		mToSolfege.put("G", "Sol");
		mToSolfege.put("G#", "Sol#");
		mToSolfege.put("Ab", "La\u266D");
	}
	
	static final HashMap<String, String> mToEnglish;
	static
	{
		mToEnglish = new HashMap<String, String>();
		mToEnglish.put("A", "A");
		mToEnglish.put("A#", "A#");
		mToEnglish.put("Bb", "B\u266D");
		mToEnglish.put("B", "B");
		mToEnglish.put("C", "C");
		mToEnglish.put("C#", "C#");
		mToEnglish.put("Db", "D\u266D");
		mToEnglish.put("D", "D");
		mToEnglish.put("D#", "D#");
		mToEnglish.put("Eb", "E\u266D");
		mToEnglish.put("E", "E");
		mToEnglish.put("F", "F");
		mToEnglish.put("F#", "F#");
		mToEnglish.put("Gb", "G\u266D");
		mToEnglish.put("G", "G");
		mToEnglish.put("G#", "G#");
		mToEnglish.put("Ab", "A\u266D");
	}
	
	public Note(int midiNumber, int keyNumber)
	{
	    mMidiNoteNumber = midiNumber;
	    mKeyNumber = keyNumber;
	    mFlatName = this.getFlatNameForNoteNumber(mMidiNoteNumber);
	    mSharpName = this.getSharpNameForNoteNumber(mMidiNoteNumber);
	    mOctave = this.getOctaveForNoteNumber(mMidiNoteNumber);
	}
	
	public String getFlatName()
	{
		return mFlatName + mOctave;
	}
	
	public String getSharpName()
	{
		return mSharpName + mOctave;
	}

	public int getMidiNoteNumber()
	{
		return mMidiNoteNumber;
	}

	public String getDisplayString(String labelType, boolean showOctave)
	{
		String noteStr = "?";
	
	    if (labelType.equals("None"))
	    {
	    	return "";
	    }
	    else if (labelType.equals("Key Number (DEV)"))
	    {
	    	return("" + mKeyNumber);
	    }
	    else if (labelType.equals("MIDI Note Number"))
	    {
	    	return("" + mMidiNoteNumber);
	    }
	    else if (labelType.equals("Whole Tone Number"))
	    {
	    	noteStr = "" + mMidiNoteNumber/2;
	    	if (mMidiNoteNumber % 2 == 1)
	    	{
	    		noteStr += ".5";
	    	}
	    	
	    	return(noteStr);
	    }
	    else if (labelType.equals("Deutsch"))
		{
		    noteStr = mToGerman.get(mSharpName);
		}
		else if (labelType.equals("English"))
		{
		    noteStr = mToEnglish.get(mSharpName);
		}
		else if (labelType.equals("Solfege"))
		{
		    noteStr = mToSolfege.get(mSharpName);
		}
	  
		if (showOctave)
		{
			noteStr += mOctave;
		}
	    
	    return(noteStr);
	}
	
	static final HashMap<Integer, String> mFlatForNumber;
	static
	{
	    mFlatForNumber = new HashMap<Integer, String>();
	    mFlatForNumber.put(0, "C");
	    mFlatForNumber.put(1, "Db");
	    mFlatForNumber.put(2, "D");
	    mFlatForNumber.put(3, "Eb");
	    mFlatForNumber.put(4, "E");
	    mFlatForNumber.put(5, "F");
	    mFlatForNumber.put(6, "Gb");
	    mFlatForNumber.put(7, "G");
	    mFlatForNumber.put(8, "Ab");
	    mFlatForNumber.put(9, "A");
	    mFlatForNumber.put(10, "Bb");
	    mFlatForNumber.put(11, "B");
	}

	static final HashMap<Integer, String> mSharpForNumber;
	static
	{
	    mSharpForNumber = new HashMap<Integer, String>();
	    mSharpForNumber.put(0, "C");
	    mSharpForNumber.put(1, "C#");
	    mSharpForNumber.put(2, "D");
	    mSharpForNumber.put(3, "D#");
	    mSharpForNumber.put(4, "E");
	    mSharpForNumber.put(5, "F");
	    mSharpForNumber.put(6, "F#");
	    mSharpForNumber.put(7, "G");
	    mSharpForNumber.put(8, "G#");
	    mSharpForNumber.put(9, "A");
	    mSharpForNumber.put(10, "A#");
	    mSharpForNumber.put(11, "B");
	}

	static final HashMap<String, Integer> mNumberForSharp;
	static
	{
	    mNumberForSharp = new HashMap<String, Integer>();
	    mNumberForSharp.put("C", 0);
	    mNumberForSharp.put("C#", 1);
	    mNumberForSharp.put("D", 2);
	    mNumberForSharp.put("D#", 3);
	    mNumberForSharp.put("E", 4);
	    mNumberForSharp.put("F", 5);
	    mNumberForSharp.put("F#", 6);
	    mNumberForSharp.put("G", 7);
	    mNumberForSharp.put("G#", 8);
	    mNumberForSharp.put("A", 9);
	    mNumberForSharp.put("A#", 10);
	    mNumberForSharp.put("B", 11);
	}

	public static int getNoteNumber(String sharpName, int octave)
	{
		int noteNumber = octave * 12 + mNumberForSharp.get(sharpName) + 12;
		return noteNumber;
	}
	
	public String getModifierNameForNoteNumber(int midiNoteNumber)
	{		
		return mFlatForNumber.get(midiNoteNumber);
	}
	
	public String getFlatNameForNoteNumber(int midiNoteNumber)
	{		
		int flatNumber = midiNoteNumber % 12;
		return mFlatForNumber.get(flatNumber);
	}
	
	public String getSharpNameForNoteNumber(int midiNoteNumber)
	{		
		int flatNumber = midiNoteNumber % 12;
		return mSharpForNumber.get(flatNumber);
	}
	
	public int getOctaveForNoteNumber(int midiNoteNumber)
	{
		int octavePlusOne = midiNoteNumber/12;
		return octavePlusOne - 1;
	}
}
