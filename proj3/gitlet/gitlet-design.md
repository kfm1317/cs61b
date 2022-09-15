# Gitlet Design Document

**Karl Meissner**:

## Classes and Data Structures
Main:
Driver class for Gitlet.

Fields
None

Commit:
Class that creates objects that hold a commit's metadata and files. Utilizes methods that grant other classes access to said info while minimizing extraction of files from directories. Also stores the branch pointers.

Fields
1. String _message: A commit's log message.
2. String _timestamp: A commit's initialization date and time.
3. Commit _parent: A commit's original parent.
4. LinkedList<String> _allParents: All of a commit's parents' SHA-1 IDs.
5. LinkedList<File> _files: A commit's list of files.
6. File _filesDir: A commit's diretory. Primarily for preserving files after clearing ADD.
7. LinkedHashMap<String, Commit> COMMITS: A map pairing commit IDs to their instances.
8. Commit master: The master branch's pointer.
9. Commit head: The current branch's pointer.
10. File COMMITS: The directory containing all commits.

## Algorithms
Main Class:
1. exitWithError(String message): Aborts program and prints error message.
2. isCommitted(File file): Returns true if head commit contains copy of CWD's version of this file.
3. replaceCurrent(String name, Commit c): Puts c's version of a file into the CWD.
4. clearStages(): Empties add and removal directories.
5. fileID(): Returns SHA-1 value of file.
6. setPointers(): Assigns the master and head pointers. 

Commit Class
1. Commit(String message, LinkedList<File> files, Commit parent, String parentID): The class constructor. Assigns specified instance variables. Adds parent's ID to the list of its parents' IDs and creates a date for the timestamp. Creates instance directory and assigns head pointer.
2. getMessage(): Returns commit's log message.
3. getTimestamp(): Returns commit's timestamp (a string).
4. getParent(): Returns commit's original parent.
5. getAllParents(): Returns a list of commit's parents' SHA-1 IDs.
6. getFiles(): Returns a list of commit's files.
7. getDir(): Returns commit's directory.
8. getID(): Returns SHA-1 value of commit's timestamp and message.
9. copyDir(): Puts copies of all of parent's directory files into its own.
10. getPath(): Creates SHA-1 value based on commit's timestamp for the commit directory to use as a unique pathname in the gitlet directory.
11. update(): Modifies current commit files as needed. Clears staging area afterwards.

## Persistence
To persist the state of gitlet, a few steps will be taken:

1. init will create permanent CWD, GITLET, ADD, REMOVAL, MASTER_HEAD, CURR_HEAD, and COMMITS directories.
2. add will update the staging area directories.
3. commit will create new commit objects initially matching its parent's files content.
4. commit will internally update the new commit's files based on ADD/REMOVE contents as needed.
5. commit will then save the new commit's data in the COMMITS directory and CURR_HEAD file for future reference.

The above system is so effective at persistence because not only does each commit instance store its parent commit info, but it also creates its own personal directory in gitlet so that deleting files in the staging directories does not affect the commit's files. The key is the program puts copies of the same files and places them in INDEPENDENT DIRECTORIES. It also keeps direct access to a commit's files private, protecting them from interference.