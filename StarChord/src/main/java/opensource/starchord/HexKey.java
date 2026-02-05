/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                         *
 *   Hexiano, an isomorphic musical keyboard for Android                   *
 *   Copyleft  @ 2013 Stephen Larroque                                     *
 *   Copyright © 2012 James Haigh                                          *
 *   Copyright © 2011 David A. Randolph                                    *
 *                                                                         *
 *   FILE: HexKey.java                                                     *
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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Log;

public abstract class HexKey
{
	static SharedPreferences mPrefs;
	static int mBlankColor;
	static int mBlackColor;
	static int mBlackHighlightColor;
	static int mWhiteColor;
	static int mWhiteHighlightColor;
	static int mTextColor;
	static int mOutlineColor;
	static int mPressedColor;
	static int mSpecialColor = 0; // For special keys (eg: Sustain)
	Point mCenter;
	Point mTop;
	Point mBottom;
	Point mUpperLeft;
	Point mUpperRight;
	Point mLowerLeft;
	Point mLowerRight;
	Point mMiddleLeft;
	Point mMiddleRight;
	Paint mPaint = new Paint();
	Paint mPressPaint = new Paint();
	Paint mOverlayPaint = new Paint();
	Paint mTextPaint = new Paint();
	Paint mBlankPaint = new Paint();
	static String mKeyOrientation = null;
	static boolean mKeyOverlap = false;
	static int mKeyCount = 0;
	protected int mKeyNumber;
	static int mRadius;
	int[] mStreamId;
    private boolean mPressed; // If key is pressed or not
    private boolean mDirty; // Used to check if a key state has changed, and if so to paint the new state on screen (functional code like play and stop are called on touch events in HexKeyboard)
	private boolean sound_loaded = false;
	protected boolean mNoSound = false;
    
    protected Instrument mInstrument;
    protected int mMidiNoteNumber;
    protected Note mNote;
    protected CC mCC;
    
	public HexKey(Context context, int radius, Point center, int midiNoteNumber, Instrument instrument, int keyNumber)
	{
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		getPrefs();

		setColors();

		mInstrument = instrument;
		mNote = new Note(midiNoteNumber, keyNumber); // keyNumber is just for reference to show as a label on the key, useless otherwise
		mMidiNoteNumber = mNote.getMidiNoteNumber();
		mKeyNumber = keyNumber;
		mStreamId = new int[] {-1};
		mPressed = false;
		mDirty = true;
		mRadius = radius;
		mCenter = center;
		
		setCriticalPoints();

		mPressPaint.setColor(mPressedColor);
        mPressPaint.setAntiAlias(true);
        mPressPaint.setStyle(Paint.Style.FILL);
        mPressPaint.setStrokeWidth(2);
        
		mPaint.setColor(getColor());
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2);
       
		mOverlayPaint.setColor(mOutlineColor);
        mOverlayPaint.setAntiAlias(true);
        mOverlayPaint.setStyle(Paint.Style.STROKE);
        mOverlayPaint.setStrokeWidth(2);
        
		mTextPaint.setColor(mTextColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(20);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        
		mBlankPaint.setColor(mBlankColor);
        mBlankPaint.setStyle(Paint.Style.FILL);
		
		mKeyCount++;
	}

	protected void getPrefs()
	{
	}

	protected void setColors()
	{
		// Colours have been left from the historically used AndroidWorld library.
		// Format: int beginning with 0xFF and then a RGB HTML/hexadecimal code (3 layers, such as FF9900 with FF for red, 99 for green and 00 for blue)
		String colorPref = mPrefs.getString("colorScheme", null);
		if (colorPref.equals("Khaki"))
		{
			mBlankColor = 0xFF000000; // Black.
			mBlackColor = 0xFF843C24; // Brown.
			mBlackHighlightColor = 0xFFD2691E; // Chocolate.
			//mBlackHighlightColor = 0xFFA0522D; // Sienna.
			mWhiteColor = 0xFFF0E68C; // Khaki.
			mWhiteHighlightColor = 0xFFBDB76B; // Dark khaki.
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = 0xFF000000; // Black.
			mPressedColor = 0xFFA9A9A9; // Dark grey.
			mSpecialColor = 0xFFFF9900; // Golden yellow.
		}
		else if (colorPref.equals("Azure"))
		{
			mBlankColor = 0xFF000000; // Black.
			mBlackColor = 0xFF4682B4; // Steel blue.
			mBlackHighlightColor = 0xFF5F9EA0; // Cadet blue.
			mWhiteColor = 0xFFF0FFFF; // Azure.
			mWhiteHighlightColor = 0xFFAFEEEE; // Pale turquoise.
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = 0xFF000000; // Black.
			mPressedColor = 0xFFA9A9A9; // Dark grey.
			mSpecialColor = 0xFF8B70B3; // Purple.
		}
		else if (colorPref.equals("White") || colorPref.equals("Black & White") ||/*...renamed to...*/ colorPref.equals("Slate"))
		// TODO: Remove 'White' preference when appropriate.
		{
			mBlankColor = 0xFF000000; // Black.
			mBlackColor = 0xFF2F4F4F; // Dark slate grey.
			mBlackHighlightColor = 0xFF708090; // Slate grey.
			mWhiteColor = 0xFFFFFFFF; // White.
			mWhiteHighlightColor = 0xFFC0C0C0; // Silver.
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = 0xFF000000; // Black.
			mPressedColor = 0xFFA9A9A9; // Dark grey.
			mSpecialColor = 0xFF336699; // Light blue.
		}
		else if (colorPref.equals("Silhouette"))
		{
			mBlankColor = 0xFFFFFFFF; // White.
			mBlackColor = 0xFF000000; // Black.
			mBlackHighlightColor = 0xFF696969; // Dim grey.
			mWhiteColor = 0xFFA9A9A9; // Dark grey.
			mWhiteHighlightColor = 0xFFD3D3D3; // Light grey.
			mOutlineColor = 0xFFFFFFFF; // White.
			mTextColor = 0xFFFFFFFF; // White.
			mPressedColor = 0xFFFFFFFF; // White.
			mSpecialColor = 0xFF336699; // Light blue.
		}
		else if (colorPref.equals("Grey & White"))
		{
			mBlankColor = 0xFF000000; // Black.
			mBlackColor = 0xFF555555;
			mBlackHighlightColor = 0xFF666666;
			mWhiteColor = 0xFFFFFFFF; // White.
			mWhiteHighlightColor = 0xFFCCCCCC;
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = mOutlineColor;
			mPressedColor = 0xFFA9A9A9; // Dark grey.
			mSpecialColor = 0xFF336699; // Light blue.
		}
		else if (colorPref.equals("Ebony & Ivory"))
		{
			mBlankColor = 0xFF432620; // WP:Piano case sample.
			//mBlankColor = 0xFF673a31; // WP:Piano case sample 2.
			//mBlackColor = 0xFF162632; // WT:Ebony.
			mBlackColor = 0xFF382c25; // WP:Ebony sample.
			//mBlackColor = 0xFF544238; // Lightened WP:Ebony sample.
			mBlackHighlightColor = mBlackColor;
			mWhiteColor = 0xFFFFFFEE; // Ivory.
			mWhiteHighlightColor = mWhiteColor;
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = 0xFF666666;
			mPressedColor = 0xFFA9A9A9; // Dark grey.
			mSpecialColor = 0xFFFF9900; // Golden yellow.
		}
		else if (colorPref.equals("Blank"))
		{
			mBlankColor = 0xFFFFFFFF; // White.
			mBlackColor = mBlankColor;
			mBlackHighlightColor = mBlankColor;
			mWhiteColor = mBlankColor;
			mWhiteHighlightColor = mBlankColor;
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = mOutlineColor;
			mPressedColor = 0xFFA9A9A9; // Dark grey.
			mSpecialColor = 0xFF336699; // Light blue.
		}
		else // Default: Azure.
		// Fail to default colour scheme if saved preference doesn't have a match.
		{
			mBlankColor = 0xFF000000; // Black.
			mBlackColor = 0xFF4682B4; // Steel blue.
			mBlackHighlightColor = 0xFF5F9EA0; // Cadet blue.
			mWhiteColor = 0xFFF0FFFF; // Azure.
			mWhiteHighlightColor = 0xFFAFEEEE; // Pale turquoise.
			mOutlineColor = 0xFF000000; // Black.
			mTextColor = 0xFF000000; // Black.
			mPressedColor = 0xFFA9A9A9; // Dark grey.
			mSpecialColor = 0xFF8B70B3; // Purple.
		}
	}
	
	abstract public int getColor();
	
	static public String getKeyOrientation(Context context)
	{
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String layoutPref = mPrefs.getString("layout", null);
		if (layoutPref.equals("Sonome")) // Sonome
		{
			mKeyOverlap = false;
			return mPrefs.getString("sonomeKeyOrientation", null);
		}
		else if (layoutPref.equals("Janko")) // Janko
		{
			mKeyOverlap = false;
			return mPrefs.getString("jankoKeyOrientation", null);
		}
		else // Jammer
		{
			return mPrefs.getString("jammerKeyOrientation", null);
		}
	}
	
	protected Path getHexagonPath()
	{
		if (mKeyOrientation.equals("Horizontal"))
		{
			return getHorizontalHexagonPath();
		}
		return getVerticalHexagonPath();
	}
	
    protected Path getVerticalHexagonPath()
    {
        Path hexy = new Path();
        double angle = Math.PI / 3;
        double increment = Math.PI / 3;
        hexy.moveTo((int)(mRadius * Math.cos(angle)),
                    (int)(mRadius * Math.sin(angle)));
        for(int i = 1; i < 6; i++)
        {
            hexy.lineTo((int)(mRadius * Math.cos(angle + i * increment)),
                        (int)(mRadius * Math.sin(angle + i * increment)));
        }
        
        hexy.close();
        
        return hexy;
    }
	
    protected Path getHorizontalHexagonPath()
    {
        Path hexy = new Path();
        double angle = Math.PI / 2;
        double increment = Math.PI / 3;
        hexy.moveTo((int)(mRadius * Math.cos(angle)),
                    (int)(mRadius * Math.sin(angle)));
        for(int i = 1; i < 6; i++)
        {
            hexy.lineTo((int)(mRadius * Math.cos(angle + i * increment)),
                        (int)(mRadius * Math.sin(angle + i * increment)));
        }
        
        hexy.close();
        
        return hexy;
    }

    // Check if the key is correctly initialized, else we will show it as unloaded (pressed) in paint()
    public boolean checkInit() {
    	return !this.sound_not_loaded(); // Sets mDirty if just loaded.
    }
   
    /** Paint this Polygon into the given graphics */
    public void paint(Canvas canvas)
    {
    	this.checkInit(); // Sets mDirty if just loaded.
    	
    	// Check if something has changed for this key using mDirty: if nothing, then we don't need to repaint
    	if (! mDirty)
    	{
    		return;	
    	}
    
  		Path hexPath = getHexagonPath();

  		// If the key is void (neither a note nor a CC/Modifier) then just don't paint it (will be all-black on screen)
		if (this.mMidiNoteNumber < 1) // (this.mMidiNoteNumber < 21 || this.mMidiNoteNumber > 108)
		{
			// Key shaping/painting
    		hexPath.offset(mCenter.x, mCenter.y);
    		canvas.drawPath(hexPath, mBlankPaint); // all blank (black)
		}
		// Else if the key is pressed OR not yet initialized, show a greyed space
		else if (mPressed || !this.checkInit())
    	{
			// Key shaping/painting
    		hexPath.offset(mCenter.x, mCenter.y);
    		canvas.drawPath(hexPath, mPressPaint); // Background (greyed) color
    		canvas.drawPath(hexPath, mOverlayPaint); // Contour
    	}
		// Else the key is released and is either a note or CC/Modifier, we paint it with a label and the corresponding color
    	else
    	{
    		String labelPref  = mPrefs.getString("labelType", null);
    		String label = "";
    		if (mCC != null) {
    			label = mCC.getDisplayString(labelPref, true);
    		} else {
    			label = mNote.getDisplayString(labelPref, true);
    		}
    		// If the note exists but there's no sound available, append an (X) to the label
    		if (mNoSound && mNote != null) {
    			label += " (X)";
    		}

    		// Key shaping/painting
    		hexPath.offset(mCenter.x, mCenter.y);
    		canvas.drawPath(hexPath, mPaint); // Background (normal) color
    		canvas.drawPath(hexPath, mOverlayPaint); // Contour
    		
    		// Label printing
    		Rect bounds = new Rect();
    		mTextPaint.getTextBounds(label, 0, label.length(), bounds); // get label size
    		int labelHeight = bounds.bottom - bounds.top; // place the label (depending on the size)
    		int x = mCenter.x;
    		int y = mCenter.y + Math.abs(labelHeight/2);
    		canvas.drawText(label, x, y, mTextPaint); // print the label on the key
    	}
    	
    	mDirty = false;
    }
    
	public boolean sound_not_loaded() {
		if (sound_loaded == true) {
			return false;
		} else {
			// Load sound only if it's a note (CC keys won't load any sound)
			if (this.mNote != null && mInstrument != null && mInstrument.mRootNotes.containsKey(mMidiNoteNumber)) {
				int index = mInstrument.mRootNotes.get(mMidiNoteNumber);
				sound_loaded = mInstrument.mSounds.containsKey(index);

				// Set mDirty if just loaded (to force refresh the painting of the key next time)
				if (sound_loaded == true) {
					mNoSound = false;
					mDirty = true;
				} else {
					mNoSound = true; // If the sound cannot be loaded, maybe the sound file is not available for this note
				}
			// Else we just tell the sound is OK (even if there's no sound for this key, it may be a Modifier/CC key)
			} else {
				mNoSound = true; // In any other case, we know this key will have no sound (either because it's a Modifier/CC key or because this a note but Instrument.mRootNotes does not contain the sound for this note)
				sound_loaded = true;
			}
			return !sound_loaded;
		}
	}

    public boolean getPressed()
    {
    	return mPressed;
    }
   
    public void setPressed(boolean state)
    {
    	if (state != mPressed)
    	{
    	    mPressed = state;
    	    mDirty = true;
    	}
    }
    
	@Override
	public String toString()
	{
		String str = new String("HexKey: ");
		str += "Center: (" + mCenter.x + ", " + mCenter.y + ")";
		return str;
	}

	// Set the touch area for the keys
	private void setCriticalPoints()
	{
		if (mKeyOrientation.equals("Horizontal") || mKeyOverlap)
		{
			setHorizontalCriticalPoints();
		}
		else
		{
			setVerticalCriticalPoints();
		}
	}

	// Set the touch area for the key when disposition is horizontal (hexagon is vertical)
	// This is a regular hexagon, composed of 6 points: the vertex of the center rectangle + two tips
	private void setHorizontalCriticalPoints()
	{
		// Compute a scaled-down touch area if mTouchScale is set (to create gaps between keys to avoid false triggers)
		int radius = mRadius * HexKeyboard.mTouchScale / 100;

		// Compute the tips of the hexagon (since it's vertical, the tips are at the top and bottom)
		mTop = new Point(mCenter.x, mCenter.y - radius);
		mBottom = new Point(mCenter.x, mCenter.y + radius);

		// Compute the center rectangle's vertexes 
		double angle = Math.PI / 6;
		mUpperRight = new Point((int)(mCenter.x + radius * Math.cos(angle)),
				(int)(mCenter.y - radius * Math.sin(angle)));
		mLowerRight = new Point((int)(mCenter.x + radius * Math.cos(angle)),
				(int)(mCenter.y + radius * Math.sin(angle)));
		mLowerLeft = new Point((int)(mCenter.x - radius * Math.cos(angle)),
				(int)(mCenter.y + radius * Math.sin(angle)));
		mUpperLeft = new Point((int)(mCenter.x - radius * Math.cos(angle)),
				(int)(mCenter.y - radius * Math.sin(angle)));

		// Debug message
		Log.d("setHorizontalCriticalPoints", 
				"Center: " + mCenter.toString() +
				" Radius: " + mRadius +
				"Critical points: " +
				mUpperRight.toString() +
				mLowerRight.toString() +
				mBottom.toString() + 
				mLowerLeft.toString() +
				mUpperLeft.toString() +
				mTop.toString()
				); // Coordinates will be given clockwise
	}
	
	// Set the touch area for the key when disposition is vertical (hexagon is horizontal)
	// This is a regular hexagon, composed of 6 points: the vertex of the center rectangle + two tips
	private void setVerticalCriticalPoints()
	{
		// Compute a scaled-down touch area if mTouchScale is set (to create gaps between keys to avoid false triggers)
		int radius = mRadius * HexKeyboard.mTouchScale / 100;

		// Compute the tips of the hexagon (since it's horizontal, the tips are at the middle left and middle right)
		mMiddleLeft = new Point(mCenter.x - radius, mCenter.y);
		mMiddleRight = new Point(mCenter.x + radius, mCenter.y);

		// Compute the center rectangle's vertexes
		mLowerLeft = new Point(mCenter.x - radius/2, 
				mCenter.y + (int)(Math.round(Math.sqrt(3.0) * radius)/2));
		mLowerRight = new Point(mCenter.x + radius/2, 
				mCenter.y + (int)(Math.round(Math.sqrt(3.0) * radius)/2));
		mUpperLeft = new Point(mCenter.x - radius/2, 
				mCenter.y - (int)(Math.round(Math.sqrt(3.0) * radius)/2));
		mUpperRight = new Point(mCenter.x + radius/2, 
				mCenter.y - (int)(Math.round(Math.sqrt(3.0) * radius)/2));

		// Debug message
		Log.d("setVerticalCriticalPoints", 
				"Center: " + mCenter.toString() +
				" Radius: " + mRadius +
				"Critical points: " +
				mUpperRight.toString() +
				mMiddleRight.toString() +
				mLowerRight.toString() +
				mLowerLeft.toString() +
				mMiddleLeft.toString() +
				mUpperLeft.toString()
				); // Coordinates will be given clockwise
	}
	
	// Check if a (touch) point should trigger this key (contained inside the critical points)
	public boolean contains(Point pos)
	{
		return this.contains(pos.x, pos.y);
	}

	// Check if a (touch) point should trigger this key (contained inside the critical points)
	public boolean contains(int x, int y)
	{
		if (mKeyOverlap)
		{
			return overlapContains(x, y);
		}
		else if (mKeyOrientation.equals("Horizontal"))
		{
			return horizontalContains(x, y);
		}
		else
		{
			return verticalContains(x, y);
		}
	}

	// Special functionality to trigger two nearby keys with one touch point
	public boolean overlapContains(int x, int y)
	{
		Log.e("HexKey::overlapContains", "Not supported by layout!");
		return false;
	}

	// Check if a (touch) point should trigger this key (contained inside the critical points)
	public boolean horizontalContains(int x, int y)
	{
		/*
		 * 
		 * We split the hexagon into three areas to determine if the specified coordinates
		 * are included. These are a "center-cut" rectangle, where most positive examples are
		 * expected, and a top and bottom triangle:
		 * 
		      /\
		     /  \    <--- Top triangle
		    /    \
		   |------|
		   |      |  <--- Center cut
		   |      |
		   |------|
		    \    /
		     \  /    <--- Bottom triangle
		      \/
		  
		  */
		if (x >= mLowerLeft.x && x <= mLowerRight.x &&
			y >= mUpperLeft.y && y <= mLowerLeft.y)
		{
			Log.d("HexKey::horizontalContains", "Center cut");
			return true; // Center cut.
		}
		if (x < mUpperLeft.x || x > mUpperRight.x ||
			y < mTop.y || y > mBottom.y)
		{
			return false; // Air ball.
		}
		if (y <= mUpperLeft.y) // Could be in top "triangle."
		{
			if (x <= mTop.x)
			{
				// We are in left half of the top triangle if the
				// slope formed by the line from the (x, y) to the top
				// vertex is >= the slope from the upper-left vertex to the
				// top vertex. We take the negative because the y-coordinate's
				// sign is reversed.
				double sideSlope = (-1.0) *
						(mTop.y - mUpperLeft.y)/(mTop.x - mUpperLeft.x);
				double pointSlope = (-1.0) * (mTop.y - y)/(mTop.x - x);
				
				Log.d("HexKey::horizontalContains", "Upper-left side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Upper-left point slope: " + pointSlope);
				
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
			else
			{
				// We are in right half of the top triangle if the
				// slope formed by the line from the (x, y) to the top
				// vertex is <= (more negative than) the slope from the
				// upper-left vertex to the top vertex. We take the
				// negative because the y-coordinate's sign is reversed.
				double sideSlope = (-1.0) *
						(mTop.y - mUpperRight.y)/(mTop.x - mUpperRight.x);
				double pointSlope = (-1.0) * (mTop.y - y)/(mTop.x - x);
				Log.d("HexKey::horizontalContains", "Lower-left side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Lower-left point slope: " + pointSlope);
				if (pointSlope <= sideSlope)
				{
					return true;
				}
			}
		}
		else // Could be in bottom triangle
		{
			if (x <= mBottom.x)
			{
				// We are in left half of the lower triangle if the
				// slope formed by the line from the (x, y) to the bottom
				// vertex is <= (more negative than) the slope from the
				// lower-left vertex to the bottom vertex. We take the
				// negative because the y-coordinate's sign is reversed.
				double sideSlope = (-1.0) *
						(mLowerLeft.y - mBottom.y)/(mLowerLeft.x - mBottom.x);
				double pointSlope = (-1.0) * (y - mBottom.y)/(x - mBottom.x);
				Log.d("HexKey::horizontalContains", "Lower-left side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Lower-left point slope: " + pointSlope);
				if (pointSlope <= sideSlope)
				{
					return true;
				}
			}
			else // Check right half
			{
				// We are in right half of the lower triangle if the
				// slope formed by the line from the (x, y) to the bottom
				// vertex is >= the slope from the lower-left vertex to 
				// the bottom vertex. We take the negative because the 
				// y-coordinate's sign is reversed.
				double sideSlope = (-1.0) *
						(mLowerRight.y - mBottom.y)/(mLowerRight.x - mBottom.x);
				double pointSlope = (-1.0) * (y - mBottom.y)/(x - mBottom.x);
				Log.d("HexKey::horizontalContains", "Lower-right side slope: " + sideSlope);
				Log.d("HexKey::horizontalContains", "Lower-right point slope: " + pointSlope);
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
		}
		
		return false;
	}

	// Check if a (touch) point should trigger this key (contained inside the critical points)
	public boolean verticalContains(int x, int y)
	{
		/*
		 * 
		 * We split the hexagon into three areas to determine if the specified coordinates
		 * are included. These are a "center-cut" rectangle, where most positive examples are
		 * expected, and a right and left triangle:
		        ______
               /|    |\
		      / |    | \
		      \ |    | /
		       \|____|/
		  
		  */
		if (x >= mLowerLeft.x && x <= mLowerRight.x &&
			y >= mUpperLeft.y && y <= mLowerLeft.y)
		{
			Log.d("HexKey::verticalContains", "Center cut");
			return true; // Center cut.
		}
		if (x < mMiddleLeft.x || x > mMiddleRight.x ||
			y < mUpperLeft.y || y > mLowerLeft.y)
		{
			return false; // Air ball.
		}
		if (x <= mUpperLeft.x) // Could be in left "triangle."
		{
			if (y <= mMiddleLeft.y)
			{
				// We are in upper half of the left triangle if the
				// slope formed by the line from the (x, y) to the upper-left
				// vertex is >= the slope from the middle-left vertex to the
				// upper-left vertex. We take the negative because the y-coordinate's
				// sign is reversed.
				double sideSlope = (-1.0) *
						(mUpperLeft.y - mMiddleLeft.y)/(mUpperLeft.x - mMiddleLeft.x);
				double pointSlope = (-1.0) * (mUpperLeft.y - y)/(mUpperLeft.x - x);
				
				Log.d("HexKey::verticalContains", "Upper-left side slope: " + sideSlope);
				Log.d("HexKey::verticalContains", "Upper-left point slope: " + pointSlope);
				
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
			else
			{
				// We may be in the lower half of the left triangle.
				double sideSlope = (-1.0) *
						(mLowerLeft.y - mMiddleLeft.y)/(mLowerLeft.x - mMiddleLeft.x);
				double pointSlope = (-1.0) * (mMiddleLeft.y - y)/(mMiddleLeft.x - x);
				Log.d("HexKey::verticalContains", "Lower-left side slope: " + sideSlope);
				Log.d("HexKey::verticalContains", "Lower-left point slope: " + pointSlope);
				if (pointSlope >= sideSlope)
				{
					return true;
				}
			}
		}
		else // Could be in right triangle
		{
			if (y <= mMiddleRight.y)
			{
				// We are in upper half of the right triangle if the
				// slope formed by the line from the (x, y) to the upper-right
				// vertex is <= the slope from the middle-right
				// vertex to the upper-upper vertex. We take the negative because 
				// the y-coordinate's sign is reversed.
				double sideSlope = (-1.0) *
						(mUpperRight.y - mMiddleRight.y)/(mUpperRight.x - mMiddleRight.x);
				double pointSlope = (-1.0) *(mUpperRight.y - y)/(mUpperRight.x - x);
				Log.d("HexKey::verticalContains", "Upper-right side slope: " + sideSlope);
				Log.d("HexKey::verticalContains", "Upper-right point slope: " + pointSlope);
				if (pointSlope <= sideSlope)
				{
					return true;
				}
			}
			else
			{
				double sideSlope = (-1.0) *
						(mLowerRight.y - mMiddleRight.y)/(mLowerRight.x - mMiddleRight.x);
				double pointSlope = (-1.0) *(mMiddleRight.y - y)/(mMiddleRight.x - x);
				Log.d("HexKey::verticalContains", "Lower-right side slope: " + sideSlope);
				Log.d("HexKey::verticalContains", "Lower-right point slope: " + pointSlope);
				if (pointSlope <= sideSlope)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	// Check if a point is visible onscreen
	protected boolean isPointVisible(Point P) {
		if (P.x >= 0 && P.y >= 0 && P.x < HexKeyboard.mDisplayWidth && P.y < HexKeyboard.mDisplayHeight) {
			return true;
		} else {
			return false;
		}
	}
	
	// Check if a key is visible onscreen by computing approximate boundaries
	// TODO: try to find a more precise way to compute the boundaries? Some kittyCornerKey may be missing (but didn't witness such a case in my tests)
	public boolean isKeyVisible() {
		Point LeftmostTop;
		Point LeftmostBottom;
		Point RightmostTop;
		Point RightmostBottom;
		Point HighestTop;
		Point LowestBottom;
		
		// Computing the lower and higher bound in x and y dimensions
		if (mMiddleLeft != null && mMiddleLeft.x < mLowerLeft.x) {
			LeftmostTop = mMiddleLeft;
			LeftmostBottom = mMiddleLeft;
		} else {
			LeftmostTop = mUpperLeft;
			LeftmostBottom = mLowerLeft;
		}
		
		if (mMiddleRight != null && mMiddleRight.x > mLowerRight.x) {
			RightmostTop = mMiddleRight;
			RightmostBottom = mMiddleRight;
		} else {
			RightmostTop = mUpperRight;
			RightmostBottom = mLowerRight;
		}
		
		if (mBottom != null && mBottom.y < mLowerLeft.y) {
			LowestBottom = mBottom;
		} else {
			LowestBottom = mLowerLeft;
		}
		
		if (mTop != null && mTop.y > mUpperLeft.y) {
			HighestTop = mTop;
		} else {
			HighestTop = mUpperLeft;
		}
		
		// DEBUG: Print the computed boundaries
		// Log.d("HexKey::isKeyVisible", "HexKey boundaries: "+Integer.toString(mKeyNumber)+" DW:"+Integer.toString(HexKeyboard.mDisplayWidth)+" DH:"+Integer.toString(HexKeyboard.mDisplayHeight)+" T:"+Integer.toString(HighestTop.x)+";"+Integer.toString(HighestTop.y)+" B:"+Integer.toString(LowestBottom.x)+";"+Integer.toString(LowestBottom.y)+" LT:"+Integer.toString(LeftmostTop.x)+";"+Integer.toString(LeftmostTop.y)+" LB:"+Integer.toString(LeftmostBottom.x)+";"+Integer.toString(LeftmostBottom.y)+" RT:"+Integer.toString(RightmostTop.x)+";"+Integer.toString(RightmostTop.y)+" RB:"+Integer.toString(RightmostBottom.x)+";"+Integer.toString(RightmostBottom.y));
		
		// Computing visibility: if the coordinates of at least one of the lowest/highest bound point is inside the screen resolution, then the key is visible
		// Note: we need to check the coordinates of Points, not just x and y (that's why we store the point and not just the x or y coordinate), else the computation will be flawed (a point may have an x coordinate in the correct range, but not y, which would place the point off-screen, below the screen)
		if ( this.isPointVisible(LeftmostTop)
				|| this.isPointVisible(LeftmostBottom)
				|| this.isPointVisible(RightmostTop)
				|| this.isPointVisible(RightmostBottom)
				|| this.isPointVisible(HighestTop)
				|| this.isPointVisible(LowestBottom)
				) {
			return true;
		} else {
			return false;
		}
	}
	
	public void play() {
		// By default without argument, play a key with the maximum pressure
		this.play(HexKeyboard.mMaxPressure);
	}
	
	public void play(float pressure)
	{
		// Play the new stream sound first before stopping the old one, avoids clipping (noticeable gap in sound between two consecutive press on the same note)
		// Note about sound clipping when first stopping previous stream then playing new stream: it probably happens because there's some delay with SoundPool commands of about 100ms, which means that the sound player has a small gap of time where there is absolutely no sound (if only one same key is pressed several times), thus the sound manager stops the sound driver, and then quickly reopens it to play the new sound, which produces the sound clipping/popping/clicking. The solution: start the new sound first and then stop the old one. Only one drawback: it consumes a thread for nothing (may stop another note when we reach the maximum number in the pool).
		int[] newStreamId = mInstrument.play(mMidiNoteNumber, pressure);
		if (newStreamId[0] == -1) {return;} // May not yet be loaded.

		// Stop the previous stream sound
		if (mStreamId[0] != -1) {
			// If sustain, we want to force stop the previous sound of this key (else it will be a kind of reverb, plus we will get weird stuffs like disabling sustain won't stop all sounds since we will loose the streamId for the keys we pressed twice!)
			if (HexKeyboard.mSustain == true || HexKeyboard.mSustainAlwaysOn == true) {
				// TODO: since the previous sound is stopped just before playing, an new bug appeared: sometimes when a key is pressed twice quickly, a clearly audible sound clipping happens!
				this.stop(true);
			// Else we don't stop the sound, just drop the Id (the sound will just play until it ends then the stream will be closed) - useful for a future Reverb!
			} else {
				this.stop(); // better always stop if there is already a stream playing, no matter the reason
				//Log.e("HexKey::play", mMidiNoteNumber + ": Already playing and no sustain! Should not happen!");
			}
			// Else else, without Sustain, stop() will be called automatically upon release of the key
		}

		// Update old stream with the new one
		mStreamId = newStreamId;
		// Change the state and drawing of the key
		String pitchStr = String.valueOf(mMidiNoteNumber);
		Log.d("HexKey::play", pitchStr);
		this.setPressed(true);
		return;
	}
	
	public void stop() {
		this.stop(false);
	}

	// Function called everytime a key press is released (and also called by play() to stop previous streams, particularly if sustain is enabled)
	public void stop(boolean force)
	{
		if (mStreamId[0] == -1) {return;} // May not have been loaded when played.
		// Force stop the sound (don't just show the unpressed state drawing) if either we provide the force argument, or if sustained is disabled
		if (force == true | (HexKeyboard.mSustain == false && HexKeyboard.mSustainAlwaysOn == false)) {
			mInstrument.stop(mStreamId);
			mStreamId = new int[] {-1};
		} // Else, we will just change the drawing of the key (useful for ModifierKeys since they don't have any sound stream to stop, just the visual state of their key)
		// Change the state and drawing of the key
		String pitchStr = String.valueOf(mMidiNoteNumber);
		Log.d("HexKey::stop", pitchStr);
		this.setPressed(false);
		return;
	}
}
