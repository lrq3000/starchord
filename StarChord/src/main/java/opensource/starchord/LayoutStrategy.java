package opensource.starchord;

import android.content.Context;
import android.content.SharedPreferences;

public interface LayoutStrategy {
    public static class Parameters {
        public final int pitchPre;
        public final int pitch1;
        public final int pitch2;
        public final int pitchPost;
        public final int groupSize; // 0 or 1 means no grouping, >1 means grouping enabled
        public final int initialPitchOffset;
        public final int groupJumpPitch; // Pitch change when wrapping a group (e.g. Janko Horizontal)

        public Parameters(int pitchPre, int pitch1, int pitch2, int pitchPost, int groupSize, int initialPitchOffset, int groupJumpPitch) {
            this.pitchPre = pitchPre;
            this.pitch1 = pitch1;
            this.pitch2 = pitch2;
            this.pitchPost = pitchPost;
            this.groupSize = groupSize;
            this.initialPitchOffset = initialPitchOffset;
            this.groupJumpPitch = groupJumpPitch;
        }

        // Convenience builder or constructor for defaults could be added, but a single full constructor is fine for internal use.
        // Or a Builder to make it cleaner given many ints.
        public static class Builder {
            private int pitchPre = 0;
            private int pitch1 = 0;
            private int pitch2 = 0;
            private int pitchPost = 0;
            private int groupSize = 0;
            private int initialPitchOffset = 0;
            private int groupJumpPitch = 0;

            public Builder pitchPre(int v) { pitchPre = v; return this; }
            public Builder pitch1(int v) { pitch1 = v; return this; }
            public Builder pitch2(int v) { pitch2 = v; return this; }
            public Builder pitchPost(int v) { pitchPost = v; return this; }
            public Builder groupSize(int v) { groupSize = v; return this; }
            public Builder initialPitchOffset(int v) { initialPitchOffset = v; return this; }
            public Builder groupJumpPitch(int v) { groupJumpPitch = v; return this; }

            public Parameters build() {
                return new Parameters(pitchPre, pitch1, pitch2, pitchPost, groupSize, initialPitchOffset, groupJumpPitch);
            }
        }
    }

    String getName();
    Parameters getParameters(Context context, SharedPreferences prefs, String orientation, int rowCount, int columnCount);
    HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum, String orientation);
}
