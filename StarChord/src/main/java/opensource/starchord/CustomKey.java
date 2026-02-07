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
        super.getPrefs();
	}

	@Override
	public int getColor()
	{
		String sharpName = mNote.getSharpName();
		int color = mWhiteColor;
		if (sharpName.contains("#"))
		{
			color = mBlackColor;
			if (sharpName.contains("G"))
			{
				// Standard piano coloring for now
			}
		}
		else if (sharpName.contains("C"))
		{
			color = mWhiteHighlightColor;
		}

		return color;
	}
}
