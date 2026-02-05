/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                   *
 *   Copyleft 2013 Stephen Larroque                                        *
 *                                                                         *
 *   FILE: CC.java                                                         *
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

public class CC
{
	protected int mOctave;
	protected String mCCName;
	protected int mMidiCCNumber;
	protected int mKeyNumber; // Just for reference, can be shown as a label on the key, but useless otherwise for the Note
	
	public CC(int midiNumber, int keyNumber)
	{
	    mMidiCCNumber = midiNumber;
	    mKeyNumber = keyNumber;
	    mCCName = this.getModifierNameForNoteNumber(mMidiCCNumber);
		mOctave = 1;
	}
	
	static final HashMap<Integer, String> mModifierForNumber;
	static
	{
	    mModifierForNumber = new HashMap<Integer, String>();
	    mModifierForNumber.put(1, "Mod");
	    mModifierForNumber.put(7, "Volume");
	    mModifierForNumber.put(10, "Pan");
	    mModifierForNumber.put(11, "Expression");
	    mModifierForNumber.put(64, "Sustain");
	}
	
	static final HashMap<String, Integer> mNumberForModifier;
	static
	{
	    mNumberForModifier = new HashMap<String, Integer>();
	    mNumberForModifier.put("Mod", 1);
	    mNumberForModifier.put("Volume", 7);
	    mNumberForModifier.put("Pan", 10);
	    mNumberForModifier.put("Expression", 11);
	    mNumberForModifier.put("Sustain", 64);
	}
	
	public String getCCName()
	{
		return mCCName + mOctave;
	}

	public int getMidiCCNumber()
	{
		return mMidiCCNumber;
	}

	public String getDisplayString(String labelType, boolean showOctave)
	{
		String noteStr = "CC?";
	
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
	    	return("CC" + mMidiCCNumber);
	    }
	    else
	    {
	    	String name = getModifierNameForNoteNumber(mMidiCCNumber);
	    	if (name.length() > 0) {
	    		return name;
	    	} else {
	    		return(noteStr);
	    	}
	    }
	}

	public static int getNoteNumber(String modifierName)
	{
		int CCNumber = mNumberForModifier.get(modifierName);
		return CCNumber;
	}
	
	public String getModifierNameForNoteNumber(int midiNoteNumber)
	{		
		return mModifierForNumber.get(midiNoteNumber);
	}
	
	public String getCCNameForNoteNumber(int midiNoteNumber)
	{		
		int flatNumber = midiNoteNumber % 12;
		return mModifierForNumber.get(flatNumber);
	}
}
