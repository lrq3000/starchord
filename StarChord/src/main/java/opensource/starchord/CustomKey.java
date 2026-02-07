package opensource.starchord;

import android.content.Context;

public class CustomKey extends HexKey
{
	public CustomKey(Context context, int radius, Point center,
			int midiNoteNumber, Instrument instrument, int keyNumber)
	{
		super(context, radius, center, midiNoteNumber, instrument, keyNumber);
	}

	@Override
	protected void getPrefs()
	{
        // Base implementation handles global keyOrientation and keyOverlap
        // Explicitly ensuring no NPE if super fails or if prefs are weird
        if (mPrefs != null) {
            String orientation = mPrefs.getString("keyOrientation", "Horizontal");
            if (orientation == null) {
                mKeyOrientation = "Horizontal";
            } else {
                mKeyOrientation = orientation;
            }
            // Ensure keyOverlap is also set safely if needed
            mKeyOverlap = mPrefs.getBoolean("keyOverlap", false);
        } else {
            // Should not happen as mPrefs is static and init in constructor
            mKeyOrientation = "Horizontal";
        }
	}

	@Override
	public int getColor()
	{
		String sharpName = mNote.getSharpName();
		int color = mWhiteColor;
		if (sharpName.contains("#"))
		{
			color = mBlackColor;
			// G# highlighting removed as requested (dead code)
		}
		else if (sharpName.contains("C"))
		{
			color = mWhiteHighlightColor;
		}

		return color;
	}
}
