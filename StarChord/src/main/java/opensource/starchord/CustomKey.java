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
		mKeyOrientation = mPrefs.getString("customKeyOrientation", null);
        // We could add customKeyOverlap if needed
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
				// Mimic Jammer highlighting for now, or just standard black
                // Maybe allow customization? For now, standard piano colors (Black/White).
                // Jammer uses G# as Highlight.
                // Standard piano has no highlight on black keys usually.
                // But let's stick to Black/White logic.
                // If I want to be safe, just standard.
			}
            // Some specific highlighting for roots (C)?
		}
		else if (sharpName.contains("C"))
		{
			color = mWhiteHighlightColor;
		}

		return color;
	}
}
