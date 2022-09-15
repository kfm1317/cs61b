package gitlet;

import java.io.File;
import java.util.List;

import static gitlet.Command.*;
import static gitlet.Commit.*;
import static gitlet.Files.*;
import static gitlet.Utils.*;

/** This utility class performs error checks for some gitlet commands.
 * @author Karl Meissner
 */
class ErrorCheck {

    /** Throws an error if ARGS has more than one entry. */
    static void noOperandCheck(String... args) {
        if (args.length > 1) {
            exit("Incorrect Operands.");
        }
    }

    /** Throws an error if ARGS does not have two entries. */
    static void singleOperandCheck(String... args) {
        if (args.length != 2) {
            exit("Incorrect Operands.");
        }
    }

    /** Throws an error if a command requiring gitlet to exist is run
     * before gitlet has been initialized.
     */
    static void gitletCheck() {
        if (!GITLET.exists()) {
            exit("Not in an initialized Gitlet directory.");
        }
    }

    /** Throws an error if commit C does not contain the file FILENAME. */
    static void containsFile(String filename, Commit c) {
        if (!filenames(c.dir()).contains(filename)) {
            exit("File does not exist in that commit.");
        }
    }

    /** Throws an error if ARGS has more than one entry or the gitlet
     * directory already exists.
     */
    static void initCheck(String... args) {
        noOperandCheck(args);
        if (GITLET.exists()) {
            exit("Gitlet version-control system already"
                    + " exists in the current directory.");
        }
    }

    /** Performs a single-operands chech on ARGS. */
    static void addCheck1(String... args) {
        singleOperandCheck(args);
    }

    /** Throws an error if no file exists at the pathway denoted by FILE. */
    static void addCheck2(File file) {
        if (!file.exists()) {
            exit("File does not exist.");
        }
    }

    /** Throws an error if ARGS does not have 2 entries or the
     * staging area is empty.
     */
    static void commitCheck(String... args) {
        if (args.length > 2) {
            exit("Incorrect operands.");
        } else if (ADD.list() == null && REMOVAL.list() == null) {
            exit("No changes added to the commit.");
        } else if (args.length == 1 || args[1].equals("")) {
            exit("Please enter a commit message.");
        }
    }

    /** Performs a single-operand check on ARGS. */
    static void rmCheck1(String... args) {
        singleOperandCheck(args);
    }

    /** Throws an error if the file FILENAME is not tracked. */
    static void rmCheck2(String filename) {
        List<String> ad = filenames(ADD);
        String headBr = readContentsAsString(HEAD);
        Commit curHd = readObject(join(BRANCHES, headBr), Commit.class);
        if (!ad.contains(filename) && (curHd.dir() == null
                || !filenames(curHd.dir()).contains(filename))) {
            exit("No reason to remove the file.");
        }
    }

    /** Performs a no-operand check on ARGS. */
    static void logCheck(String... args) {
        noOperandCheck(args);
    }

    /** Performs a single-operand check on ARGS. */
    static void findCheck1(String... args) {
        singleOperandCheck(args);
    }

    /** Throws an error if no commits with a given log message exist,
     * as indicated by BOOL.
     * */
    static void findCheck2(boolean bool) {
        if (!bool) {
            exit("Found no commit with that message.");
        }
    }

    /** Performs a no-operand check on ARGS. */
    static void statusCheck(String... args) {
        noOperandCheck(args);
    }

    /** Throws an error if ARGS does not have valid entries for checkout. */
    static void checkoutCheck1(String... args) {
        int len = args.length;
        if (len < 2 || len > 4 || len == 4 && !args[2].equals("--")) {
            exit("Incorrect operands.");
        }
    }

    /** Throws an error if the file denoted in ARGS doesn't exist in a
     * given commit, the checked-out branch is the current branch, no
     * commit with the ID denoted in ARGS exists, or no branch with the
     * name denoted in ARGS exists.
     */
    static void checkoutCheck2(String... args) {
        int len = args.length;
        String cr = readContentsAsString(HEAD);
        if (len == 3) {
            containsFile(args[2], readObject(join(BRANCHES, cr), Commit.class));
        } else if (len == 2) {
            if (!filenames(BRANCHES).contains(args[1])) {
                exit("No such branch exists.");
            } else if (args[1].equals(cr)) {
                exit("No need to checkout the current branch.");
            }
        } else {
            String full = fullID(args[1]);
            if (full == null) {
                exit("No commit with that id exists.");
            }
            Commit c = findCommit(full);
            containsFile(args[3], c);
        }
    }

    /** Throws an error if for each file specified in the CWD file list cwd,
     * directory BRDIR contains a non-matching version, and CURDIR does not
     * contain a matching version, meaning the CWD version of said file is in
     * danger of an unsaved overwrite.
     */
    static void checkoutCheck3(List<String> cwd, File brDir) {
        String curBr = readContentsAsString(HEAD);
        for (String s : cwd) {
            Commit curHd = readObject(join(BRANCHES, curBr), Commit.class);
            File comF = join(brDir, s), wk = join(CWD, s), hd = null;
            if (curHd.dir() != null) {
                hd = join(curHd.dir(), s);
            }
            if (comF.exists() && !matchingFiles(comF, wk)
                    && (curHd.dir() == null || !matchingFiles(hd, wk))) {
                printUntrackedMessage();
            }
        }
    }

    /** Performs a single-operand check on ARGS. */
    static void branchCheck1(String... args) {
        singleOperandCheck(args);
    }

    /** Throws an error if a branch with the name BRANCH already exists. */
    static void branchCheck2(String branch) {
        if (filenames(BRANCHES).contains(branch)) {
            exit("A branch with that name already exists.");
        }
    }

    /** Performs a single-operand check on ARGS. */
    static void rmBranchCheck1(String... args) {
        singleOperandCheck(args);
    }

    /** Throws an error if no branch with the name BRANCH exists or BRANCH
     * is the current branch.
     */
    static void rmBranchCheck2(String branch) {
        if (!filenames(BRANCHES).contains(branch)) {
            exit("A branch with that name does not exist.");
        } else if (readContentsAsString(HEAD).equals(branch)) {
            exit("Cannot remove the current branch.");
        }
    }

    /** Performs a single-operand check on ARGS and throws an error if
     * no commit with the ID given in ARGS exists.
     */
    static void resetCheck(String... args) {
        singleOperandCheck(args);
        if (findCommit(fullID(args[1])) == null) {
            exit("No commit with that id exists.");
        }
    }

    /** Performs a single-operand check on ARGS. */
    static void mergeCheck1(String... args) {
        singleOperandCheck(args);
        if (args[1].equals(readContentsAsString(HEAD))) {
            exit("Cannot merge a branch with itself.");
        } else if (!filenames(BRANCHES).contains(args[1])) {
            exit("A branch with that name does not exist.");
        } else if (ADD.list().length != 0 || REMOVAL.list().length != 0) {
            exit("You have uncommitted changes.");
        }
    }
}
