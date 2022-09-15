package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.TimeZone;
import static gitlet.Files.*;
import static gitlet.Utils.*;

/** Commit object class. This class creates commit instances,
 * and stores any static files or directories used for them.
 *  @author Karl Meissner
 */
class Commit implements Serializable {

    /** Initializes a commit with just the MESSAGE parameter. Initializes
     * the message and birth instance variables, changing the timestamp
     * instance variable in the process.
     */
    Commit(String message) {
        _message = message;
        _birth = new Date(0L);
        _timestamp = setTimestamp();
    }

    /** Initializes a commit with the MESSAGE and PARENT parameters.
     * Initializes the message, parent, birth, files, and filesDir
     * instance variables, changing the timestamp instance variable
     * in the process. The instance directory is created as a copy
     * of the parent commit's directory, assuming the parent exists,
     * and is stored it the gilet directory.
     */
    Commit(String message, String parent) throws IOException {
        _message = message;
        _parent = findCommit(parent);
        _birth = new Date(System.currentTimeMillis());
        _timestamp = setTimestamp();
        _allParents.add(parent.substring(0, 7));
        _files = _parent._files;
        _filesDir = join(GITLET, getPath());
        _filesDir.mkdir();
        copyDir();
    }

    /** Initializes a commit with MESSAGE and two parents,
     * PARENT1 and PARENT2, as parameters. Follows most of
     * the initiation process of the second constructor.
     */
    Commit(String message, Commit parent1, Commit parent2) {
        _message = message;
        _parent = parent1;
        _birth = new Date(System.currentTimeMillis());
        _timestamp = setTimestamp();
        _allParents.add(parent1.getID().substring(0, 7));
        _allParents.add(parent2.getID().substring(0, 7));
        _filesDir = join(GITLET, getPath());
        _filesDir.mkdir();
    }

    /** Returns this commit's message. */
    String message() {
        return _message;
    }

    /** Returns this commit's timestamp. */
    String timestamp() {
        return _timestamp;
    }

    /** Returns this commit's parent. */
    Commit parent() {
        return _parent;
    }

    /** Returns a list of this commit's parents. */
    LinkedList<String> allParents() {
        return _allParents;
    }

    /** Returns a list of this commit's files. */
    LinkedList<File> files() {
        return _files;
    }

    /** Returns this commit's directory. */
    File dir() {
        return _filesDir;
    }

    /** Returns this commit's ID (SHA-1 value). */
    String getID() {
        return sha1(_message, _timestamp);
    }

    /** Returns the timestamp as a string by utitizing a DateFormat object. */
    private String setTimestamp() {
        String form = "HH:mm:ss yyyy Z";
        DateFormat goodDate = new SimpleDateFormat("'Date:' EEE LLL d " + form);
        goodDate.setTimeZone(TimeZone.getTimeZone("PST"));
        return goodDate.format(_birth);
    }

    /** Copies this commit's parent's directory's contents into its own. */
    private void copyDir() throws IOException {
        if (_parent != null && _parent._filesDir != null) {
            copyContents(_parent._filesDir, _filesDir);
        }
    }

    /** Returns the file directory's gitlet pathway using MESSAGE and PARENT. */
    private String getPath() {
        return "Commit #" + COMMITS.list().length;
    }

    /** This commit's log message. */
    private final String _message;

    /** This commit's initialization date as a String. */
    private final String _timestamp;

    /** This commit's initial parent commit. */
    private Commit _parent;

    /** The list of this commit's parents' IDs. */
    private final LinkedList<String> _allParents = new LinkedList<>();

    /** A List of this commit's files. */
    private LinkedList<File> _files = new LinkedList<>();

    /** This commit's file directory. */
    private File _filesDir;

    /** This commit's time of initiation as a Date object. */
    private final Date _birth;

    /** The directory holding all gitlet commits. */
    static final File COMMITS = join(GITLET, "commits");
}
