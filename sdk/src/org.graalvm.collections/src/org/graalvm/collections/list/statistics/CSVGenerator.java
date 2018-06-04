package org.graalvm.collections.list.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Class to generate CSV files containing the informations gathered by the StatisticTrackers
 *
 * @author Alex R.
 *
 */

import static org.graalvm.collections.list.statistics.StatisticConfigs.*;

public class CSVGenerator {

    private final static File mainDir;
    private final static File FOLDER;

    // Some Constants for writing
    private final static int BUF_SIZE = 512000;

    private static boolean initialized = false;

    static {
        mainDir = new File(MAIN_DIR_PATH);
        FOLDER = new File(MAIN_DIR_PATH + FOLDER_NAME);
        if (!mainDir.exists()) {
            mainDir.mkdir();
        }
        if (mainDir.exists() && mainDir.isDirectory()) {
            if (FOLDER.exists() && FOLDER.isDirectory()) {
                initialized = true;
            } else if (!FOLDER.exists()) {
                FOLDER.mkdir();
                initialized = true;
            }
        }

    }

    public static synchronized void createFileOfAllocationSites(String namePrefix) {
        final String[] allocSites = Statistics.getAllocSiteLines(DATA_SEPARATOR);
        final File file = createFile(namePrefix + ALLOC_SITE);
        if (file == null)
            return;
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Allocation Sites" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, allocSites, true);
    }

    public static synchronized void createFileOfTrackerTypes(String namePrefix) {
        final String[] types = Statistics.getTypesForAllTrackers(DATA_SEPARATOR);
        final File file = createFile(namePrefix + MAIN_TYPES);
        if (file == null)
            return;
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Main Type" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, types, true);
    }

    public static synchronized void createFileOfTrackerSizes(String namePrefix) {
        final String[] sizes = Statistics.getCapacityAndSizes(DATA_SEPARATOR);
        final File file = createFile(namePrefix + SIZE_N_CAPS);
        if (file == null)
            return;
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Size" + DATA_SEPARATOR + "CAPACITY" + LINE_SEPARATOR, APPEND_MODE);
        writeToFile(file, sizes, true);
    }

    public static synchronized void createFileOfOperationDistributions(String namePrefix) {
        final String[] opLines = Statistics.getOpDataLines(DATA_SEPARATOR);
        File file = createFile(namePrefix + NAME_OP_DISTR);
        if (file == null)
            return; // TODO Exception
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Operation" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, APPEND_MODE);
        // writeToFile(file, opLines, true);

        int length = Statistics.trackers.size();
        ;
        List<String[]> arrays = new ArrayList<>(10002);
        arrays.add(opLines);

        for (int i = 0; i < length; i++) {

            arrays.add(Statistics.trackers.get(i).getOpDataLines(DATA_SEPARATOR));
            if (i % 25000 == 0) {// TODO REMOVE
                writeToFile(file, arrays, true);
                arrays = new ArrayList<>(25005);
                System.out.println("working... " + i);
            }
        }
        writeToFile(file, arrays, true);
    }

    public static synchronized void createFileOfTypeOperationDistributions(String namePrefix) {
        File file = createFile(namePrefix + NAME_TYPE_OP_DISTR);
        if (file == null)
            return; // TODO Exception
        writeToFile(file, "Tracker" + DATA_SEPARATOR + "Operation on" + DATA_SEPARATOR + "Type" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, APPEND_MODE);
        int length = Statistics.trackers.size();
        String[] trackerInfo;

        for (int i = 0; i < length; i++) {
            trackerInfo = Statistics.trackers.get(i).getTypeOpDataLines(DATA_SEPARATOR);
            writeToFile(file, trackerInfo, true);
        }
    }

    public static synchronized void createFileOfTracker(final int ID, String namePrefix) {
        if (!initialized)
            return;

        final StatisticTracker tracker = Statistics.getTrackerByID(ID);
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

    @Deprecated
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
        for (int i = 1; i < length; i++) { // TODO Fix Bottleneck
            trackerInfo = Statistics.getTrackerByID(i).getOpDataLines(DATA_SEPARATOR);
            writeToFile(file, "TRACKER_" + i + DATA_SEPARATOR + "Operation" + DATA_SEPARATOR + "Occurrences" + LINE_SEPARATOR, true);
            writeToFile(file, trackerInfo, true);
            writeToFile(file, LINE_SEPARATOR + "", true);
        }
    }

    private static File createFile(String name) {
        File file = null;
        try {
            file = new File(FOLDER, name);

            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private static void writeToFile(File file, List<String[]> arrays, boolean append) {

        final byte[] bytes = arrays.stream().flatMap(arr -> Arrays.stream(arr)).map(string -> string.concat("\n")).reduce("", (a, b) -> a.concat(b)).getBytes();
        // final byte[] bytes = arrays.stream().flatMap(arr -> Arrays.stream(arr)).reduce("", (a, b) ->
        // a.concat(b)).getBytes();
        try {
            Files.write(Paths.get(file.getPath()), bytes, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static synchronized void writeToFile(File file, String[] lines, boolean append) {

        // TODO change to Try with resources
        try {
            final BufferedWriter out = new BufferedWriter(new FileWriter(file, append), BUF_SIZE);
            // final PrintWriter w = new PrintWriter(new FileOutputStream(file, append), false);

            int i = 0;
            for (String s : lines) {
                out.write(s);
                out.write(LINE_SEPARATOR);
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void writeToFile(File file, String string, boolean append) {
        try {
            final BufferedWriter out = new BufferedWriter(new FileWriter(file, append));
            out.write(string);
            out.close();
            // FileOutputStream w = new FileOutputStream(file, append);
            // w.write(string.getBytes());
            // w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
