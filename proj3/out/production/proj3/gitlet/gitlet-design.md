# Gitlet Design Document

**Karl Meissner**:

## Classes and Data Structures
Main:
Driver class for Gitlet.

Fields
1. File CWD: The current working directory.
2. File GITLET: The .gitlet directory within CWD.
3. File ADD: The addition staging area in gitlet.
4. File REMOVAL: The removal staging area in gitlet.

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

## Algorithms
Main Class:
1. exitWithError(String message): Aborts program and prints error message.
2. getFileContents(String name, File dir): Returns contents of file "name", which is located in dir.
3. isCommitted(File file): Returns true if head commit contains copy of CWD's version of this file.
4. clearStages(): Empties add and removal directories.
5. replaceCurrent(String name, Commit c): Puts c's version of a file into the CWD.

Commit Class
1. Commit(String message, LinkedList<File> files, Commit parent, String parentID): The class constructor. Assigns specified instance variables. Adds parent's ID to the list of its parents' IDs and creates a date for the timestamp. Creates instance directory and assigns head pointer.
2. getMessage(): Returns commit's log message.
3. getTimestamp(): Returns commit's timestamp (a string).
4. getParent(): Returns commit's original parent.
5. getAllParents(): Returns a list of commit's parents' SHA-1 IDs.
6. getFiles(): Returns a list of commit's files.
7. getDir(): Returns instance directory.
8. getTree(): Returns the static commit tree.
9. getID(): Returns SHA-1 value of commit's timestamp and message.
10. getInfo(): Returns a LinkedHashMap of commit's files paired with their SHA-1 IDs.
11. copyDir(): Puts copies of all of parent's directory files into its own.
12. getFileNames(): Returns a list of commit's filenames.
13. update(): Modifies current commit files as needed. Clears staging area afterwards.

## Persistence
To persist the state of gitlet, a few steps will be taken:

1. init will create the CWD, GITLET, ADD, REMOVE, and COMMITS directories. A blank commit file will be added to COMMITS.
2. add will update the staging area directories as needed.
3. commit will create a new commit object identical to its parent, with its parent's instance part of its construction.
4. commit will then internally update the commit's files based on ADD/REMOVE contents.
5. commit will then be able to generate its files' SHA-1 IDs and map them to their serialized contents.

The above system is so effective at persistence because not only does each commit instance store its parent commit as an instance variable, but it also stores its contents in multiple ways, such as having a File list, a method that maps the files' SHA-1's to their contents, and even its own directory for copying files from ADD so that said files aren't deleted from the commit when ADD gets cleared. The key is to make COPIES of the files in question, not just move them around. It is also safe because other classes cannot modify a commit's file contents, they can only access them!