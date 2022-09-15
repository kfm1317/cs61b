package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.HashMap;
import static gitlet.Files.*;
import static gitlet.Commit.*;
import static gitlet.Utils.*;
import static gitlet.ErrorCheck.*;

/** This utility class executes most parts of any command given to Main. It
 * also contains the static boolean variable indicating merge conflicts.
 *  @author Karl Meissner
 */
class Command {

    /** Initializes the gitlet system. Creates all directories and files,
     * as well as the initial commit. Writes the initial commit into a commit
     * directory file and the master branch's file. ARGS contains only a command
     * keyword.
     */
    static void init(String... args) throws IOException {
        initCheck(args);
        CWD.mkdir();
        GITLET.mkdir();
        ADD.mkdir();
        REMOVAL.mkdir();
        BRANCHES.mkdir();
        MASTER.createNewFile();
        HEAD.createNewFile();
        COMMITS.mkdir();
        Commit c = new Commit("initial commit");
        File first = join(COMMITS, sha1(serialize(c)));
        first.createNewFile();
        writeObject(first, c);
        writeObject(MASTER, c);
        writeContents(HEAD, MASTER.getName());
    }

    /** Stages a file whose name is in ARGS for addition. Removes the file
     * from the removal stage if it is already there. Overrides any file
     * with the same name on the add stage with the CWD version's contents.
     */
    static void add(String... args) throws IOException {
        gitletCheck();
        addCheck1(args);
        File now = join(CWD, args[1]);
        addCheck2(now);
        File toAdd = join(ADD, args[1]);
        File toRem = join(REMOVAL, args[1]);
        boolean committed = isCommitted(now);
        if (toRem.exists()) {
            toRem.delete();
        }
        if (committed) {
            if (toAdd.exists()) {
                toAdd.delete();
            }
        } else {
            if (!toAdd.exists()) {
                toAdd.createNewFile();
            }
            if (!fileID(toAdd).equals(fileID(now))) {
                writeContents(toAdd, readContentsAsString(now));
            }
        }
    }

    /** Creates a new commit object using info from the commit stored in the
     * current branch's head and the ARGS input. Updates the commit's files
     * according to the staging area's contents. Writes the commit into a new
     * file in the commit directory and the current branchs heads file. Clears
     * the staging area at the end.
     */
    static void commit(String... args) throws IOException {
        gitletCheck();
        commitCheck(args);
        if (ADD.list().length == 0 && REMOVAL.list().length == 0) {
            exit("No changes added to the commit.");
        }
        String curBr = readContentsAsString(HEAD);
        Commit prevCom = readObject(join(BRANCHES, curBr), Commit.class);
        Commit newCom = new Commit(args[1], prevCom.getID());
        updateCommit(newCom);
        File newCommit = join(COMMITS, sha1(serialize(newCom)));
        newCommit.createNewFile();
        writeObject(newCommit, newCom);
        writeObject(join(BRANCHES, curBr), newCom);
        clearStagingArea();
    }

    /** Adds a file named in ARGS to the removal stage. If a version of
     * this file is currently on the add stage, it is removed. Deletes
     * the CWD version of this file (if it exists) if the commit stored
     * in the current branch's head contains a version of the file.
     */
    static void rm(String... args) throws IOException {
        gitletCheck();
        rmCheck1(args);
        String name = args[1];
        rmCheck2(name);
        if (filenames(ADD).contains(name)) {
            join(ADD, name).delete();
        }
        String curBr = readContentsAsString(HEAD);
        Commit hd = readObject(join(BRANCHES, curBr), Commit.class);
        if (hd.dir() != null && filenames(hd.dir()).contains(name)) {
            File remove = join(REMOVAL, name), curr = join(CWD, name);
            remove.createNewFile();
            if (curr.exists()) {
                curr.delete();
            }
        }
    }

    /** Prints the current branch head's commits' metadata in
     * order of the most recent. ARGS contains only a command keyword.
     */
    static void log(String... args) {
        gitletCheck();
        logCheck(args);
        Formatter show = new Formatter();
        String curBr = readContentsAsString(HEAD);
        Commit here = readObject(join(BRANCHES, curBr), Commit.class);
        while (here != null) {
            formatLog(show, here);
            if (!here.message().equals("initial commit")) {
                show.format("\n\n");
            }
            here = here.parent();
        }
        System.out.println(show);
    }

    /** Prints every commit's metadata. ARGS contains only a command keyword. */
    static void globalLog(String... args) {
        gitletCheck();
        logCheck(args);
        Formatter printer = new Formatter();
        List<String> commits = filenames(COMMITS);
        int index = 1;
        for (String s : commits) {
            formatLog(printer, readObject(join(COMMITS, s), Commit.class));
            if (index != commits.size()) {
                printer.format("\n\n");
            }
            index++;
        }
        System.out.println(printer);
    }

    /** Prints the ID of any commits with the log message given in ARGS. */
    static void find(String... args) {
        gitletCheck();
        findCheck1(args);
        List<String> commits = filenames(COMMITS);
        boolean found = false;
        for (String s : commits) {
            Commit c = readObject(join(COMMITS, s), Commit.class);
            if (c.message().equals(args[1])) {
                System.out.println(c.getID());
                found = true;
            }
        }
        findCheck2(found);
    }

    /** Prints out a list of gitlet's branches, staged files, removed
     * files, modified files, and untracked files. ARGS contains only
     * a command keyword.
     */
    static void status(String... args) {
        gitletCheck();
        statusCheck(args);
        Formatter stat = new Formatter();
        int index1 = 1, index2 = 1, index3 = 1;
        stat.format("=== Branches ===\n");
        List<String> branches = filenames(BRANCHES);
        String curBr = readContentsAsString(HEAD);
        for (String brn : branches) {
            if (curBr.equals(brn)) {
                stat.format("*");
            }
            stat.format(brn + "\n");
            if (index1 == branches.size()) {
                stat.format("\n");
            }
            index1++;
        }
        List<String> ad = filenames(ADD), rm = filenames(REMOVAL);
        stat.format("=== Staged Files ===\n");
        if (ad != null && ad.size() > 0) {
            for (String added : ad) {
                stat.format(added + "\n");
                if (index2 == ad.size()) {
                    stat.format("\n");
                }
                index2++;
            }
        } else {
            stat.format("\n");
        }
        stat.format("=== Removed Files ===\n");
        if (rm != null && rm.size() > 0) {
            for (String remed : rm) {
                stat.format(remed + "\n");
                if (index3 == rm.size()) {
                    stat.format("\n");
                }
                index3++;
            }
        } else {
            stat.format("\n");
        }
        finishStatus(stat, 1, 1);
        System.out.println(stat);
    }

    /** Replaces the CWR version of FILENAME with the version
     * in the commit with ID COMMITID.
     */
    static void checkout(String filename, String commitID) {
        Commit c = findCommit(fullID(commitID));
        replaceCurrentFile(filename, c);
    }

    /** Copies all files in the commit stored in the BRANCHCOMM's
     * head into the CWD, overriding any that share a name. Deletes
     * any CWD files that do not have an equivalently named file in
     * said commit. Assigns the current branch to BRANCHCOMM, which
     * is located in DIR.
     */
    static void checkoutReset(String branchComm, File dir) throws IOException {
        Commit gvHd;
        if (dir.equals(BRANCHES)) {
            gvHd = readObject(join(dir, branchComm), Commit.class);
        } else {
            gvHd = findCommit(fullID(branchComm));
        }
        List<String> cwd = filenames(CWD);
        List<String> branCom = new LinkedList<>();
        if (gvHd.dir() != null) {
            branCom = filenames(gvHd.dir());
            checkoutCheck3(cwd, gvHd.dir());
        }
        for (String s : cwd) {
            if (branCom.contains(s)) {
                replaceCurrentFile(s, gvHd);
            } else {
                join(CWD, s).delete();
            }
        }
        if (gvHd.dir() != null) {
            copyContents(gvHd.dir(), CWD);
        }
        clearStagingArea();
        if (dir.equals(BRANCHES)) {
            writeContents(HEAD, branchComm);
        } else {
            String curBr = readContentsAsString(HEAD);
            writeObject(join(BRANCHES, curBr), gvHd);
        }
    }

    /** Creates a new branch named in ARGS, writing the commit
     * stored in the current branch's head into its own file.
     */
    static void branch(String... args) throws IOException {
        gitletCheck();
        branchCheck1(args);
        branchCheck2(args[1]);
        File newBranch = join(BRANCHES, args[1]);
        newBranch.createNewFile();
        String curBr = readContentsAsString(HEAD);
        Commit curHd = readObject(join(BRANCHES, curBr), Commit.class);
        writeObject(newBranch, curHd);
    }

    /** Deletes the branch named in ARGS, if it exists. */
    static void rmBranch(String... args) {
        gitletCheck();
        rmBranchCheck1(args);
        rmBranchCheck2(args[1]);
        join(BRANCHES, args[1]).delete();
    }

    /** Merges the branch whose name is given in ARGS with the current branch.
     * Creates a new commit with the heads of the two branches as parents.
     */
    static void merge(String... args) throws IOException {
        gitletCheck();
        mergeCheck1(args);
        String currBr = readContentsAsString(HEAD);
        Commit hdCom = readObject(join(BRANCHES, currBr), Commit.class);
        Commit gvCom = readObject(join(BRANCHES, args[1]), Commit.class);
        File hdDir = hdCom.dir(), gvDir = gvCom.dir();
        List<String> cfm = getfam(hdCom, gvCom, new LinkedList<>(), args[1], 0);
        List<String> gfm = getfam(gvCom, hdCom, new LinkedList<>(), args[1], 1);
        List<String> nxtComC = new LinkedList<>();
        HashMap<Integer, Commit> splts = getSplts(hdCom, cfm, gfm,
                new HashMap<>(), 0);
        Commit splt = splts.get(Collections.min(splts.keySet()));
        List<String> spltFl = new LinkedList<>(), gvFl = filenames(gvDir);
        if (splt.dir() != null) {
            spltFl = filenames(splt.dir());
        }
        mergeCommits(gvFl, spltFl, nxtComC, hdDir, gvDir, splt, gvCom);
        if (filenames(ADD).isEmpty() && filenames(REMOVAL).isEmpty()
                && nxtComC.equals(filenames(hdCom.dir()))) {
            exit("No changes added to the commit.");
        }
        Commit dubs = new Commit("Merged " + args[1] + " into "
                + currBr + ".", hdCom, gvCom);
        updateCommit(dubs, nxtComC);
        updateCommit(dubs);
        clearStagingArea();
        if (_yCONFL) {
            System.out.println("Encountered a merge conflict.");
        }
        File dubsFile = join(COMMITS, sha1(serialize(dubs)));
        writeObject(dubsFile, dubs);
        writeObject(join(BRANCHES, currBr), dubs);
        _yCONFL = false;
    }

    /** Aborts the program and prints MESSAGE. */
    public static void exit(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(0);
    }

    /** Boolean variable that tracks whether merge conflict exists. */
    protected static boolean _yCONFL = false;
}
