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
            Parameters p = new Parameters();
            if (orientation.equals("Vertical")) {
                p.pitch1 = -5;
                p.pitch2 = -7;
                p.pitchPost = -2;
                p.pitchPre = 0;
            } else {
                p.pitchPre = -(rowCount - 1) * 2;
                p.pitch1 = -5;
                p.pitch2 = 7;
                p.pitchPost = -12;
            }
            return p;
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
            Parameters p = new Parameters();
            if (orientation.equals("Vertical")) {
                p.pitchPre = (rowCount - 1) * 7;
                p.pitch1 = 4;
                p.pitch2 = -3;
                p.pitchPost = -7;
            } else {
                p.pitch1 = 4;
                p.pitch2 = 3;
                p.pitchPost = 1;
            }
            return p;
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
            Parameters p = new Parameters();

            // Janko initial pitch offset
            p.initialPitchOffset = -((columnCount - 1) * 2 - 1);

            String groupSizeStr = prefs.getString("jankoRowCount", "4");
            // Sanitize
            groupSizeStr = groupSizeStr.replaceAll("[^0-9]", "");
            if (groupSizeStr.length() == 0) groupSizeStr = "4";
            int groupSize = Integer.parseInt(groupSizeStr);
            p.groupSize = groupSize;

            int groupCount = columnCount / groupSize;
            if (columnCount % groupSize > 0) groupCount++;

            if (orientation.equals("Vertical")) {
                p.pitchPre = -(groupCount - 1) * 12;
                p.pitch1 = -1;
                p.pitch2 = 1;
                p.pitchPost = 2;
                p.groupJumpPitch = 0;
            } else {
                p.pitch1 = 1;
                p.pitch2 = 1;
                p.pitchPost = 0;
                p.groupJumpPitch = -12;
            }
            return p;
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
            Parameters p = new Parameters();
            // Defaults
            p.pitch1 = 1;
            p.pitch2 = 1;
            p.pitchPost = 1;
            p.pitchPre = 0;

            try {
                // Same steps regardless of orientation
                p.pitch1 = Integer.parseInt(prefs.getString("customStep1", "1"));
                p.pitch2 = Integer.parseInt(prefs.getString("customStep2", "1"));
                p.pitchPost = Integer.parseInt(prefs.getString("customStepPost", "1"));

                String groupSizeStr = prefs.getString("customGroupSize", "0");
                groupSizeStr = groupSizeStr.replaceAll("[^0-9]", "");
                if (groupSizeStr.length() > 0) {
                    p.groupSize = Integer.parseInt(groupSizeStr);
                }

                String groupJumpStr = prefs.getString("customGroupJump", "-12");
                // Allow negative
                // groupJumpStr might contain '-'
                try {
                    p.groupJumpPitch = Integer.parseInt(groupJumpStr);
                } catch (NumberFormatException e) {
                    p.groupJumpPitch = -12;
                }

                if (p.groupSize > 0 && orientation.equals("Vertical")) {
                    int groupCount = columnCount / p.groupSize;
                    if (columnCount % p.groupSize > 0) groupCount++;
                    // Custom logic for pitchPre could be added here if needed, but omitted for now as per plan
                }

            } catch (NumberFormatException e) {
                // Ignore, use defaults
            }

            return p;
        }

        @Override
        public HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum, String orientation) {
            return new CustomKey(context, radius, center, pitch, instru, keyCount);
        }
    }
}
