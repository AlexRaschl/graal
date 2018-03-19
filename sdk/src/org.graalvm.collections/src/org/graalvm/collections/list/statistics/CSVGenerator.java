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
    private final static String TR_PREFIX = "TR";
    private final static String NAME_OP_DISTR = "OpDistr.csv";
    private final static String NAME_TYPE_OP_DISTR = "TypeOpDistr.csv";
    private final static String ALLOC_SITE = "AllocSites.csv";

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

    public static synchronized void createFileOfAllocationSites(String namePrefix) {
        final String[] allocSites = Statistics.getAllocSiteLines(DATA_SEPARATOR);
        final File file = createFile(namePrefix + ALLOC_SITE);
        if (file == null)
            return;
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Allocation Sites" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, allocSites, true);
    }

    public static synchronized void createFileOfOperationDistributions(String namePrefix) {
        final String[] opLines = Statistics.getOpDataLines(DATA_SEPARATOR);
        File file = createFile(namePrefix + NAME_OP_DISTR);
        if (file == null)
            return; // TODO Exception
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Operation" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, opLines, true);

        int length = StatisticTrackerImpl.getNextID();
        String[] trackerInfo;
        for (int i = 1; i < length; i++) {
            trackerInfo = Statistics.getTrackerByID(i).getOpDataLines(DATA_SEPARATOR);
            writeToFile(file, trackerInfo, true);
        }
    }

    public static synchronized void createFileOfTypeOperationDistributions(String namePrefix) {
        File file = createFile(namePrefix + NAME_TYPE_OP_DISTR);
        if (file == null)
            return; // TODO Exception
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Operation on" + DATA_SEPARATOR + "Type" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, APPEND_MODE);
        int length = StatisticTrackerImpl.getNextID();
        String[] trackerInfo;

        for (int i = 1; i < length; i++) {
            trackerInfo = Statistics.getTrackerByID(i).getTypeOpDataLines(DATA_SEPARATOR);
            writeToFile(file, trackerInfo, true);
        }
    }

    public static synchronized void createFileOfTracker(final int ID, String namePrefix) {
        if (!initialized)
            return;

        StatisticTracker tracker = Statistics.getTrackerByID(ID);
        if (tracker == null)
            throw new NoSuchElementException();

        String[] opLines = tracker.getOpDataLines(DATA_SEPARATOR);
        File file = createFile(namePrefix + TR_PREFIX + ID + ".csv");
        if (file == null)
            return;
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Operation" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, opLines, true);
        writeToFile(file, LINE_SEPARATOR + "", true);
    }

    public static synchronized void createFileOfGlobalInfo(String namePrefix) {
        if (!initialized)
            return;
        String[] opLines = Statistics.getOpDataLines(DATA_SEPARATOR);
        String[] typeLines = Statistics.getTypeDataLines(DATA_SEPARATOR);
        String[] lfLines = Statistics.getLoadFactorDataLines(DATA_SEPARATOR);

        File file = createFile(namePrefix + NAME_GBL);
        if (file == null)
            return; // TODO Exception

        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Operaton" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, opLines, true);
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Type" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, true);
        writeToFile(file, typeLines, true);
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Load Factor" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, true);
        writeToFile(file, lfLines, true);
        writeToFile(file, LINE_SEPARATOR + "", true);

    }

    public static synchronized void createFileOfEverything(String namePrefix) {
        if (!initialized)
            return;

        String[] opLines = Statistics.getOpDataLines(DATA_SEPARATOR);
        String[] typeLines = Statistics.getTypeDataLines(DATA_SEPARATOR);
        String[] lfLines = Statistics.getLoadFactorDataLines(DATA_SEPARATOR);

        File file = createFile(namePrefix + NAME_ALL);
        if (file == null)
            return; // TODO Exception

        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Operaton" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, opLines, true);
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Type" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, true);
        writeToFile(file, typeLines, true);
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Load Factor" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, true);
        writeToFile(file, lfLines, true);

        int length = StatisticTrackerImpl.getNextID();
        String[] trackerInfo;
        for (int i = 1; i < length; i++) {
            trackerInfo = Statistics.getTrackerByID(i).getOpDataLines(DATA_SEPARATOR);
            writeToFile(file, "TRACKER_" + i + DATA_SEPARATOR + "Operation" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, true);
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

    private static synchronized void writeToFile(File file, String[] lines, boolean append) {

        try {
            FileOutputStream w = new FileOutputStream(file, append);

            for (String s : lines) {
                w.write(s.getBytes());
                w.write(LINE_SEPARATOR);
            }
            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void writeToFile(File file, String string, boolean append) {
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
