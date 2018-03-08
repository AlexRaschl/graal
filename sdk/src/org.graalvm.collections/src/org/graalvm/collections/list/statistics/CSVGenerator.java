package org.graalvm.collections.list.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Class to generate CSV files containing the informations gathered by the StatisticTrackers
 *
 * @author Alex R.
 *
 */
public class CSVGenerator {

    private final static String dirPath = "/home/urzidil/Programming/CSV/";
    private final static File mainDir;

    private final static String NAME_ALL = "all.csv";
    private final static String NAME_GBL = "gbl.csv";
    private final static String NAME_PREFIX = "TR";

    private final static boolean APPEND_MODE = false;

    private static boolean initialized = false;

    // For CSV file creation
    private final static char DATA_SEPARATOR = ';';
    private final static char LINE_SEPARATOR = '\n';

    static {
        mainDir = new File(dirPath);
        if (mainDir.exists() && mainDir.isDirectory())
            initialized = true;
    }

    public static void createFileOfTracker(final int ID) {
        if (!initialized)
            return;

        StatisticTracker tracker = Statistics.getTrackerByID(ID);
        if (tracker == null)
            throw new NoSuchElementException();

        String[] opLines = tracker.getOpDataLines(DATA_SEPARATOR);
        File file = createFile(NAME_PREFIX + ID + ".csv");
        if (file == null)
            return; // TODO Exception
        writeToFile(file, "TRACKER_" + ID + ": Operation Occurrences" + DATA_SEPARATOR + "Num" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, opLines, true);
        writeToFile(file, LINE_SEPARATOR + "", true);
    }

    public static void createFileOfGlobalInfo() {
        if (!initialized)
            return;
        String[] opLines = Statistics.getOpDataLines(DATA_SEPARATOR);
        String[] typeLines = Statistics.getTypeDataLines(DATA_SEPARATOR);
        String[] lfLines = Statistics.getLoadFactorDataLines(DATA_SEPARATOR);

        File file = createFile(NAME_GBL);
        if (file == null)
            return; // TODO Exception

        writeToFile(file, "Operaton Occurrences" + DATA_SEPARATOR + "Num" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, opLines, true);
        writeToFile(file, "Type Occurrences" + DATA_SEPARATOR + "Num" + LINE_SEPARATOR, true);
        writeToFile(file, typeLines, true);
        writeToFile(file, "Load Factor Occurrences" + DATA_SEPARATOR + "Num" + LINE_SEPARATOR, true);
        writeToFile(file, lfLines, true);
        writeToFile(file, LINE_SEPARATOR + "", true);

    }

    public static void createFileOfEverything() {
        if (!initialized)
            return;

        String[] opLines = Statistics.getOpDataLines(DATA_SEPARATOR);
        String[] typeLines = Statistics.getTypeDataLines(DATA_SEPARATOR);
        String[] lfLines = Statistics.getLoadFactorDataLines(DATA_SEPARATOR);

        File file = createFile(NAME_ALL);
        if (file == null)
            return; // TODO Exception

        writeToFile(file, "Operaton Occurrences" + DATA_SEPARATOR + "Num" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, opLines, true);
        writeToFile(file, "Type Occurrences" + DATA_SEPARATOR + "Num" + LINE_SEPARATOR, true);
        writeToFile(file, typeLines, true);
        writeToFile(file, "Load Factor Occurrences" + DATA_SEPARATOR + "Num" + LINE_SEPARATOR, true);
        writeToFile(file, lfLines, true);

        int length = StatisticTrackerImpl.getNextID();
        String[] trackerInfo;
        for (int i = 0; i < length; i++) {
            trackerInfo = Statistics.getTrackerByID(i).getOpDataLines(DATA_SEPARATOR);
            writeToFile(file, "TRACKER_" + i + ": Operation Occurrences" + DATA_SEPARATOR + "Num" + LINE_SEPARATOR, true);
            writeToFile(file, trackerInfo, true);
            writeToFile(file, LINE_SEPARATOR + "", true);
        }
    }

    private static File createFile(String name) {
        File file = null;
        try {
            file = new File(mainDir, name);

            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;

    }

    private static void writeToFile(File file, String[] lines, boolean append) {

        try {
            FileOutputStream w = new FileOutputStream(file, append);

            for (String s : lines) {
                w.write(s.getBytes());
                w.write(LINE_SEPARATOR);
            }
            w.write(LINE_SEPARATOR);
            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(File file, String string, boolean append) {
        try {
            FileOutputStream w = new FileOutputStream(file, append);
            w.write(string.getBytes());
            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
