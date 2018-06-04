package org.graalvm.collections.list.statistics;

import java.util.EnumSet;
import java.util.HashSet;

import org.graalvm.collections.list.statistics.Statistics.Operation;

public class StatisticConfigs {

    // For CSVGenerator
    public static final String MAIN_DIR_PATH = "/home/urzidil/Programming/CSV/";
    public static final String FOLDER_NAME = "TEST";
    //
    public static final String NAME_ALL = "_ALL.csv";
    public static final String NAME_GBL = "_GLOBAL.csv";
    public static final String TR_PREFIX = "_TR";
    public static final String NAME_OP_DISTR = "_OP_DISTR.csv";
    public static final String NAME_TYPE_OP_DISTR = "_TYPE_OP_DISTR.csv";
    public static final String ALLOC_SITE = "_ALLOC_SITES.csv";
    public static final String MAIN_TYPES = "_MAIN_TYPES.csv";
    public static final String SIZE_N_CAPS = "_SIZE_CAP.csv";

    // For CSV file creation
    public static final char DATA_SEPARATOR = ';';
    public static final char LINE_SEPARATOR = '\n';

    // Append to old files with same name
    public static final boolean APPEND_MODE = false;

    // Use the allocSite Tracker which is faster
    public static final boolean USE_ALLOC_SITE_TRACKING = true;
    public static final boolean AGGREGATE_SAME_CLASSES = false;

    // Enable Tracking
    public static final boolean TRACKING_ENABLED = true;

    // Track all Allocation Sites or only the Sites listed in trackedSites HashSet
    public static final boolean TRACKS_ALL = true;
    public static final boolean INIT_ZERO = true;

    // Specify Sites to track if TRACKS_ALL is disabled
    public static final HashSet<String> TRACKED_SITES = new HashSet<>(10);

    /** Static block to set up Tracked Classes */
    static {
        TRACKED_SITES.add("org.graalvm.collections.test.list.statistics.StatisticsSimpleTest");
        TRACKED_SITES.add("org.graalvm.collections.test.list.statistics.ReplacementTest");
        //
        TRACKED_SITES.add("org.graalvm.compiler.asm.Label");
        TRACKED_SITES.add("org.graalvm.compiler.core.gen.NodeLIRBuilder");
        TRACKED_SITES.add("org.graalvm.compiler.core.common.FieldsScanner");
        //
        TRACKED_SITES.add("org.graalvm.compiler.nodes.IfNode");
        TRACKED_SITES.add("org.graalvm.compiler.nodes.InliningLog");
    }

    // Operations that are tracked more precisely for each Type in the list
    public static final EnumSet<Statistics.Operation> SPECIAL_OPS = EnumSet.of(Operation.ADD_OBJ, Operation.REMOVE_OBJ, Operation.GET_INDEXED, Operation.SET_INDEXED);
    public static final EnumSet<Statistics.Operation> INIT_ZERO_SET = EnumSet.of(Operation.ADD_OBJ, Operation.REMOVE_OBJ, Operation.GROW, Operation.SET_INDEXED, Operation.CLEAR);
}
