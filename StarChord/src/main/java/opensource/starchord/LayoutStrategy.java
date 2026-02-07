package opensource.starchord;

import android.content.Context;
import android.content.SharedPreferences;

public interface LayoutStrategy {
    public static class Parameters {
        public int pitchPre = 0;
        public int pitch1 = 0;
        public int pitch2 = 0;
        public int pitchPost = 0;
        public int groupSize = 0; // 0 or 1 means no grouping, >1 means grouping enabled
        public int initialPitchOffset = 0;
        public int groupJumpPitch = 0; // Pitch change when wrapping a group (e.g. Janko Horizontal)
    }

    String getName();
    Parameters getParameters(Context context, SharedPreferences prefs, String orientation, int rowCount, int columnCount);
    HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum, String orientation);
}
