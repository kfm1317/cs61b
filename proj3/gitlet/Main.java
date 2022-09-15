package gitlet;

import java.io.IOException;
import static gitlet.Files.*;
import static gitlet.Command.*;
import static gitlet.Commit.*;
import static gitlet.ErrorCheck.*;
import static gitlet.Utils.*;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Karl Meissner
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            exit("Please enter a command.");
        }
        switch (args[0]) {
        case "init":
            init(args);
            break;
        case "add":
            add(args);
            break;
        case "commit":
            commit(args);
            break;
        case "rm":
            rm(args);
            break;
        case "log":
            log(args);
            break;
        case "global-log":
            globalLog(args);
            break;
        case "find":
            find(args);
            break;
        case "status":
            status(args);
            break;
        case "checkout":
            gitletCheck();
            checkoutCheck1(args);
            checkoutCheck2(args);
            if (args.length == 3) {
                checkout(args[2], readObject(join(BRANCHES,
                        readContentsAsString(HEAD)), Commit.class).getID());
            } else if (args.length == 4) {
                checkout(args[3], args[1]);
            } else {
                checkoutReset(args[1], BRANCHES);
            }
            break;
        case "branch":
            branch(args);
            break;
        case "rm-branch":
            rmBranch(args);
            break;
        case "reset":
            resetCheck(args);
            checkoutReset(args[1], COMMITS);
            break;
        case "merge":
            merge(args);
            break;
        default:
            exit("No command with that name exists.");
        }
    }
}
