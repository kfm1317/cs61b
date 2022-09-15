package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import static gitlet.Command.*;
import static gitlet.Utils.*;
import static gitlet.Commit.*;

/** This utility class provides any methods that perform operations
 * on file objects, i.e. files and directories, that are not in the
 * Utils.java class. This includes over the course of any given gitlet
 * command. It also stores gitlet-specific files/directories.
 *  @author Karl Meissner
 */
class Files {

    /** Clears the staging area. */
    static void clearStagingArea() {
        if (filenames(ADD) != null) {
            List<String> adds = filenames(ADD);
            for (String s : adds) {
                join(ADD, s).delete();
            }
        }
        if (filenames(REMOVAL) != null) {
            List<String> rems = filenames(REMOVAL);
            for (String s : rems) {
                join(REMOVAL, s).delete();
            }
        }
    }

    /** Copies contents of files in DIR1 into files in DIR2. */
    static void copyContents(File dir1, File dir2) throws IOException {
        List<String> filenames = filenames(dir1);
        for (String s : filenames) {
            File original = join(dir1, s), copy = join(dir2, s);
            if (!copy.exists()) {
                copy.createNewFile();
            }
            if (!matchingFiles(original, copy)) {
                writeContents(copy, readContentsAsString(original));
            }
        }
    }

    /** Removes files from DIR if they are in the removal stage. */
    static void removeFiles(File dir) {
        List<String> filenames = filenames(REMOVAL);
        for (String s : filenames) {
            File duplicate = join(dir, s);
            if (duplicate.exists()) {
                duplicate.delete();
            }
        }
    }

    /** Returns the commit with the given ID COMMITID from the commit directory.
     * If no commit with that ID exists, null is returned.
     */
    static Commit findCommit(String commitID) {
        List<String> comF = filenames(COMMITS);
        Commit wanted = null;
        for (String s : comF) {
            Commit c = readObject(join(COMMITS, s), Commit.class);
            if (c.getID().equals(commitID)) {
                wanted = c;
                break;
            }
        }
        return wanted;
    }

    /** Modifies commit C using add and removal directories. */
    static void updateCommit(Commit c) throws IOException {
        File dir = c.dir();
        if (ADD.list().length != 0) {
            copyContents(ADD, dir);
        }
        if (REMOVAL.list().length != 0) {
            removeFiles(dir);
        }
        c.files().clear();
        if (filenames(c.dir()) != null) {
            c.files().addAll(Arrays.asList(dir.listFiles()));
        }
    }

    /** Modifies commit NW using current branch head's file list CURLS. */
    static void updateCommit(Commit nw, List<String> curLs) throws IOException {
        for (String curF : curLs) {
            String curBr = readContentsAsString(HEAD);
            Commit curHdCom = readObject(join(BRANCHES, curBr), Commit.class);
            File ad = join(nw.dir(), curF), cur = join(curHdCom.dir(), curF);
            ad.createNewFile();
            writeContents(ad, readContentsAsString(cur));
        }
        nw.files().addAll(Arrays.asList(nw.dir().listFiles()));
    }

    /** Returns the String SHA-1 value based on FILE's name and contents. */
    static String fileID(File file) {
        return sha1(file.getName(), readContents(file));
    }

    /** Returns full commit ID represented by shortened form SEQ. */
    static String fullID(String seq) {
        String full = null;
        List<String> commits = filenames(COMMITS);
        for (String str : commits) {
            Commit c = readObject(join(COMMITS, str), Commit.class);
            if (c.getID().startsWith(seq)) {
                full = c.getID();
                break;
            }
        }
        return full;
    }

    /** Returns TRUE if files F1 and F2 have same fileID. */
    static boolean matchingFiles(File f1, File f2) {
        return f1.exists() && f2.exists() && fileID(f1).equals(fileID(f2));
    }

    /** Puts COMMIT's version of NAME into the CWD. */
    public static void replaceCurrentFile(String name, Commit commit) {
        File curFile = join(CWD, name);
        File comFile = join(commit.dir(), name);
        writeContents(curFile, readContentsAsString(comFile));
    }

    /** Returns TRUE if commit stored in current branch's head contains
     * a matching copy of FILE in CWD.
     */
    static boolean isCommitted(File file) {
        String curBr = readContentsAsString(HEAD);
        Commit c = readObject(join(BRANCHES, curBr), Commit.class);
        if (c.files().size() != 0) {
            List<String> headFiles = filenames(c.dir());
            for (String s : headFiles) {
                if (s.equals(file.getName())) {
                    if (fileID(join(c.dir(), s)).equals(fileID(file))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Formats commit C's metadata for log command using formatter FORM. */
    static void formatLog(Formatter form, Commit c) {
        form.format("===\n" + "commit " + c.getID() + "\n");
        if (c.allParents().size() > 1) {
            formatMergeInfo(c, form);
        }
        form.format(c.timestamp() + "\n");
        form.format(c.message());
    }

    /** Performs operations for last two status inquiries using formatter
     * F and final two indices INDEX4 and INDEX5.
     */
    static void finishStatus(Formatter f, int index4, int index5) {
        f.format("=== Modifications Not Staged For Commit ===\n");
        List<String> curr = null, cwd = filenames(CWD);
        String headBr = readContentsAsString(HEAD);
        Commit hd = readObject(join(BRANCHES, headBr), Commit.class);
        if (hd.dir() != null) {
            curr = filenames(hd.dir());
        }
        if (curr != null) {
            for (String mods : curr) {
                File cFile = join(CWD, mods), aFile = join(ADD, mods);
                boolean yCWD = cFile.exists(), yADD = aFile.exists();
                boolean chngNoStgd = yCWD && !yADD && !isCommitted(cFile);
                boolean difStgd = yCWD && yADD && !matchingFiles(cFile, aFile);
                boolean stgNoCWD = !yCWD && yADD;
                boolean trkDeltd = !yCWD && !join(REMOVAL, mods).exists();
                if (chngNoStgd || difStgd || stgNoCWD || trkDeltd) {
                    f.format(mods);
                    if (chngNoStgd || difStgd) {
                        f.format(" (modified)\n");
                    } else {
                        f.format(" (deleted)\n");
                    }
                }
                if (index4 == curr.size()) {
                    f.format("\n");
                }
                index4++;
            }
        } else {
            f.format("\n");
        }
        f.format("=== Untracked Files ===\n");
        if (cwd != null) {
            for (String untrcked : cwd) {
                File adF = join(ADD, untrcked), hdF = null;
                if (hd.dir() != null) {
                    hdF = join(hd.dir(), untrcked);
                }
                if (!adF.exists() && (hd.dir() == null || !hdF.exists())) {
                    f.format(untrcked + "\n");
                }
                if (index5 == cwd.size()) {
                    f.format("\n");
                }
            }
        }
    }

     /** Returns a conflicted merge file's new contents with the help
      * of the current branch head's file CUR and given branch head's
      * file GVN.
      */
    static String makeConflictedContents(File cur, File gvn) {
        String contents = "<<<<<<< HEAD\n";
        if (cur.exists()) {
            contents = contents.concat(readContentsAsString(cur));
        } else {
            contents = contents.concat("\n");
        }
        contents = contents.concat("=======\n");
        if (gvn.exists()) {
            contents = contents.concat(readContentsAsString(gvn));
        }
        contents = contents.concat(">>>>>>>\n");
        return contents;
    }

    /** Formats merge metadata for commit C using formatter F. */
    static void formatMergeInfo(Commit c, Formatter f) {
        f.format("Merge: ");
        for (int i = 0; i < c.allParents().size(); i++) {
            f.format(c.allParents().get(i));
            if (i < c.allParents().size() - 1) {
                f.format(" ");
            } else {
                f.format("\n");
            }
        }
    }

    /** Prints the last error message for checkout and merge commands. */
    static void printUntrackedMessage() {
        exit("There is an untracked file in the way;"
                + " delete it, or add and commit it first.");
    }

    /** Returns the built-up ancestry list CURANS of the branch starting
     * at Commit C1, using C2 for reference. Throws an error if one commit
     * is an ancestor of the other, with S and SIDE providing direction on
     * which one to throw.
     */
    static List<String> getfam(Commit c1, Commit c2, List<String> curAns,
                               String s, int side) throws IOException {
        if (c1.getID().equals(c2.getID())) {
            if (side == 0) {
                exit("Given branch is an ancestor of the current branch.");
            } else {
                checkoutReset(s, BRANCHES);
                exit("Current branch fast-forwarded.");
            }
        } else if (c1.parent() != null) {
            for (String parent : c1.allParents()) {
                Commit cPar = findCommit(fullID(parent));
                if (!curAns.contains(cPar.getID())) {
                    curAns.add(cPar.getID());
                }
                List<String> npar = getfam(cPar, c2, curAns, s, side);
                for (String str : npar) {
                    if (!curAns.contains(str)) {
                        curAns.add(str);
                    }
                }
            }
        }
        return curAns;
    }

    /** Returns an updated HashMap MAP that contains all split points of
     * branches CUR and GV, which are found by checking ancestry lists L1
     * and L2. The distance from the current branch head is tracked by INDEX.
     */
    static HashMap<Integer, Commit> getSplts(Commit cur, List<String> L1,
                     List<String> L2, HashMap<Integer, Commit> map, int index) {
        if (L1.contains(cur.getID()) && L2.contains(cur.getID())
                && !map.containsKey(index)) {
            map.put(index, cur);
        }
        for (String str : cur.allParents()) {
            Commit nxt = findCommit(fullID(str));
            map.putAll(getSplts(nxt, L1, L2, map, index + 1));
        }
        return map;
    }

    /** Uses the given, split, and next commit lists GVFL, SPLTFL, and
     * NXTCOMC, as well as current and given branch heads' directories
     * HDDIR and GVDIR, and split point and given branch head commits
     * SPLT and GVCOM to perform file operations necessary to merge the
     * two branches.
     */
    static void mergeCommits(List<String> gvFl, List<String> spltFl,
                  List<String> nxtComC, File hdDir, File gvDir, Commit splt,
                  Commit gvCom) throws IOException {
        String curBr = readContentsAsString(HEAD);
        Commit c = readObject(join(BRANCHES, curBr), Commit.class);
        List<String> curFl = filenames(c.dir());
        for (int i = 0; i < 3; i++) {
            checkTracking(spltFl, curFl, gvFl, hdDir, gvDir, i);
        }
        for (String s : spltFl) {
            File spt = join(splt.dir(), s), cur = join(hdDir, s);
            File gv = join(gvDir, s), nw = join(CWD, s);
            if (matchingFiles(spt, cur)) {
                if (gv.exists() && !matchingFiles(spt, gv)) {
                    checkout(s, gvCom.getID());
                    add("add", s);
                } else if (!gv.exists()) {
                    rm("rm", s);
                }
            } else if (cur.exists() && !matchingFiles(spt, cur)) {
                if (matchingFiles(spt, gv) || matchingFiles(cur, gv)) {
                    nxtComC.add(s);
                } else if ((gv.exists() && !matchingFiles(cur, gv))
                        || !gv.exists()) {
                    writeConflictContents(cur, gv, nw, s);
                }
            } else if (gv.exists() && !matchingFiles(spt, gv)) {
                writeConflictContents(cur, gv, nw, s);
            }
        }
        for (String s : curFl) {
            if (!spltFl.contains(s)) {
                File cur = join(hdDir, s), gv = join(gvDir, s);
                File nw = join(CWD, s);
                if (!gv.exists()) {
                    nxtComC.add(s);
                } else if (!matchingFiles(cur, gv)) {
                    writeConflictContents(cur, gv, nw, s);
                }
            }
        }
        for (String s : gvFl) {
            if (!spltFl.contains(s) && !curFl.contains(s)) {
                File rNow = join(CWD, s);
                if (!rNow.exists()) {
                    rNow.createNewFile();
                    checkout(s, gvCom.getID());
                    add("add", s);
                }
            }
        }
    }

    /** Writes a conflict file's contents to CWD version of said file
     * using files CUR, GV, and NW, and string S. */
    static void writeConflictContents(File cur, File gv, File nw,
                        String s) throws IOException {
        String conf = makeConflictedContents(cur, gv);
        writeContents(nw, conf);
        add("add", s);
        _yCONFL = true;
    }

    /** Throws an error if there are any untracked files in the way of
     * a merge. Split point commit, current branch head, and given branch
     * head file lists SPLTL, CURL, and GVL are all examined, the current
     * and given branch head commits' directories CURDIR and GVDIR helps
     * file checking, and the INDICATOR integer dictates which is being checked.
     */
    static void checkTracking(List<String> spltL, List<String> curL,
                     List<String> gvL, File curDir, File gvDir, int indicator) {
        if (indicator == 0) {
            for (String s: spltL) {
                File cur = join(curDir, s), nw = join(CWD, s);
                if (nw.exists() && !matchingFiles(cur, nw)) {
                    printUntrackedMessage();
                }
            }
        } else if (indicator == 1) {
            for (String s : curL) {
                if (!spltL.contains(s)) {
                    File cur = join(curDir, s), nw = join(CWD, s);
                    if (nw.exists() && !matchingFiles(cur, nw)) {
                        printUntrackedMessage();
                    }
                }
            }
        } else {
            for (String s : gvL) {
                if (!spltL.contains(s) && !curL.contains(s)) {
                    File rNow = join(CWD, s), gv = join(gvDir, s);
                    if (rNow.exists() && !matchingFiles(gv, rNow)) {
                        printUntrackedMessage();
                    }
                }
            }
        }
    }

    /** The current working directory. */
    static final File CWD = new File(".");

    /** The gitlet directory. */
    static final File GITLET = join(CWD, ".gitlet");

    /** The add stage directory. */
    static final File ADD = join(GITLET, "add_stage");

    /** The removal stage directory. */
    static final File REMOVAL = join(GITLET, "removal_stage");

    /** The branch directory. */
    static final File BRANCHES = join(GITLET, "branches");

    /** The directory storing the head of the master branch. */
    static final File MASTER = join(BRANCHES, "master");

    /** A file that stores the current head pointer. */
    static final File HEAD = join(GITLET, "head");
}
