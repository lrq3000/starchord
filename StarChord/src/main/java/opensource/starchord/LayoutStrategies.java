package opensource.starchord;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LayoutStrategies {

    public static LayoutStrategy getStrategy(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String layout = prefs.getString("layout", "Jammer");
        if (layout.equals("Sonome")) return new SonomeStrategy();
        if (layout.equals("Janko")) return new JankoStrategy();
        if (layout.equals("Custom")) return new CustomStrategy();
        return new JammerStrategy();
    }

    private static class JammerStrategy implements LayoutStrategy {
        @Override
        public String getName() { return "Jammer"; }

        @Override
        public Parameters getParameters(Context context, SharedPreferences prefs, String orientation, int rowCount, int columnCount) {
            Parameters.Builder b = new Parameters.Builder();
            if (orientation.equals("Vertical")) {
                b.pitch1(-5).pitch2(-7).pitchPost(-2).pitchPre(0);
            } else {
                b.pitchPre(-(rowCount - 1) * 2).pitch1(-5).pitch2(7).pitchPost(-12);
            }
            return b.build();
        }

        @Override
        public HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum, String orientation) {
            return new JammerKey(context, radius, center, pitch, instru, keyCount);
        }
    }

    private static class SonomeStrategy implements LayoutStrategy {
        @Override
        public String getName() { return "Sonome"; }

        @Override
        public Parameters getParameters(Context context, SharedPreferences prefs, String orientation, int rowCount, int columnCount) {
            Parameters.Builder b = new Parameters.Builder();
            if (orientation.equals("Vertical")) {
                b.pitchPre((rowCount - 1) * 7).pitch1(4).pitch2(-3).pitchPost(-7);
            } else {
                b.pitch1(4).pitch2(3).pitchPost(1);
            }
            return b.build();
        }

        @Override
        public HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum, String orientation) {
            return new SonomeKey(context, radius, center, pitch, instru, keyCount);
        }
    }

    private static class JankoStrategy implements LayoutStrategy {
        @Override
        public String getName() { return "Janko"; }

        @Override
        public Parameters getParameters(Context context, SharedPreferences prefs, String orientation, int rowCount, int columnCount) {
            Parameters.Builder b = new Parameters.Builder();

            // Janko initial pitch offset
            b.initialPitchOffset(-((columnCount - 1) * 2 - 1));

            String groupSizeStr = prefs.getString("jankoRowCount", "4");
            // Sanitize
            groupSizeStr = groupSizeStr.replaceAll("[^0-9]", "");
            if (groupSizeStr.length() == 0) groupSizeStr = "4";
            int groupSize = Integer.parseInt(groupSizeStr);
            b.groupSize(groupSize);

            int groupCount = columnCount / groupSize;
            if (columnCount % groupSize > 0) groupCount++;

            if (orientation.equals("Vertical")) {
                b.pitchPre(-(groupCount - 1) * 12).pitch1(-1).pitch2(1).pitchPost(2).groupJumpPitch(0);
            } else {
                b.pitch1(1).pitch2(1).pitchPost(0).groupJumpPitch(-12);
            }
            return b.build();
        }

        @Override
        public HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum, String orientation) {
            if (orientation != null && orientation.equals("Vertical")) {
                return new JankoKey(context, radius, center, pitch + 12 * groupNum, instru, keyCount, groupNum);
            } else {
                return new JankoKey(context, radius, center, pitch, instru, keyCount, groupNum);
            }
        }
    }

    private static class CustomStrategy implements LayoutStrategy {
        @Override
        public String getName() { return "Custom"; }

        @Override
        public Parameters getParameters(Context context, SharedPreferences prefs, String orientation, int rowCount, int columnCount) {
            Parameters.Builder b = new Parameters.Builder();
            // Defaults
            b.pitch1(1).pitch2(1).pitchPost(1).pitchPre(0);

            try {
                // Same steps regardless of orientation
                b.pitch1(Integer.parseInt(prefs.getString("customStep1", "1")));
                b.pitch2(Integer.parseInt(prefs.getString("customStep2", "1")));
                b.pitchPost(Integer.parseInt(prefs.getString("customStepPost", "1")));

                int groupSize = 0;
                String groupSizeStr = prefs.getString("customGroupSize", "0");
                groupSizeStr = groupSizeStr.replaceAll("[^0-9]", "");
                if (groupSizeStr.length() > 0) {
                    groupSize = Integer.parseInt(groupSizeStr);
                    b.groupSize(groupSize);
                }

                String groupJumpStr = prefs.getString("customGroupJump", "-12");
                // Allow negative
                // groupJumpStr might contain '-'
                try {
                    b.groupJumpPitch(Integer.parseInt(groupJumpStr));
                } catch (NumberFormatException e) {
                    b.groupJumpPitch(-12);
                }

                if (groupSize > 0 && orientation.equals("Vertical")) {
                    int groupCount = columnCount / groupSize;
                    if (columnCount % groupSize > 0) groupCount++;
                    // TODO: consider Janko-like pitchPre adjustment
                    b.pitchPre(0); // Explicitly set to 0 as requested
                }

            } catch (NumberFormatException e) {
                // Ignore, use defaults
            }

            return b.build();
        }

        @Override
        public HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum, String orientation) {
            return new CustomKey(context, radius, center, pitch, instru, keyCount);
        }
    }
}
