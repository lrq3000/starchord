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
        public HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum) {
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
        public HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum) {
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
        public HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum) {
            // Vertical mode logic relies on adding 12 * groupNum here.
            // Horizontal mode logic relies on pitch being shifted by 12, so groupNum impact here?
            // Existing HexKeyboard logic for Horizontal:
            //   key = new JankoKey(..., jpitch, ..., ogn);
            //   where jpitch was decremented by 12.
            //   So createKey should pass pitch as is.
            // But JankoKey uses groupNum for Coloring.
            // AND for Vertical, pitch is NOT shifted in the loop, but key adds 12*groupNum.
            //
            // We can detect orientation inside here via getKeyOrientation?
            // Or assume the pitch passed in is the base pitch, and we modify it if needed?
            //
            // Vertical Loop passes 'ipitch' which is 'pitch'. 'pitch' is not jumped.
            // So we need to add 12*groupNum.
            //
            // Horizontal Loop passes 'jpitch' which IS jumped.
            // So we should NOT add 12*groupNum?
            // But JankoKey constructor:
            //   super(..., midiNoteNumber, ...);
            // It doesn't modify midiNoteNumber.
            // Wait, look at HexKeyboard.java again.

            /*
            Vertical:
                key = new JankoKey(..., ipitch + 12 * octaveGroupNumber, ...);
            Horizontal:
                key = new JankoKey(..., jpitch, ...);
            */

            String orient = HexKey.getKeyOrientation(context); // Note: this uses static prefs lookup which handles Janko
            if (orient.equals("Vertical")) {
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
                if (orientation.equals("Vertical")) {
                    p.pitch1 = Integer.parseInt(prefs.getString("customStepV1", "1"));
                    p.pitch2 = Integer.parseInt(prefs.getString("customStepV2", "1"));
                    p.pitchPost = Integer.parseInt(prefs.getString("customStepVPost", "1"));
                } else {
                    p.pitch1 = Integer.parseInt(prefs.getString("customStepH1", "1"));
                    p.pitch2 = Integer.parseInt(prefs.getString("customStepH2", "1"));
                    p.pitchPost = Integer.parseInt(prefs.getString("customStepHPost", "1"));
                }

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
                    // Should we assume Janko-like logic for pitchPre?
                    // "Generalize options". If groupSize is used, pitchPre usually needs adjustment to center or start correct?
                    // Janko logic: pitchPre = -(groupCount - 1) * 12.
                    // Custom user sets steps. They might not expect this automatic offset.
                    // But if they use grouping, they probably want multiple octaves side-by-side.
                    // Let's NOT touch pitchPre automatically for Custom unless requested.
                    // User can set p.pitchPre if we expose it? We didn't expose it.
                    // Janko calculates it.
                    // Maybe add `customPitchPre`?
                    // But that's getting complicated.
                    // Let's assume for now pitchPre is 0 for Custom.
                }

            } catch (NumberFormatException e) {
                // Ignore, use defaults
            }

            return p;
        }

        @Override
        public HexKey createKey(Context context, int radius, Point center, int pitch, Instrument instru, int keyCount, int groupNum) {
            return new CustomKey(context, radius, center, pitch, instru, keyCount);
        }
    }
}
