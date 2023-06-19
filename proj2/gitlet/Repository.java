package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author EnzoGuang
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECTS = join(GITLET_DIR, "objects");
    public static final File BLOB_OBJECTS = join(OBJECTS, "blob");
    public static final File COMMIT_OBJECTS = join(OBJECTS, "commit");
    public static final File REFS = join(GITLET_DIR, "refs");
    public static final File HEAD = join(GITLET_DIR, "head");
    public static final File INDEX = join(GITLET_DIR, "index");
    public static final File STAGED_INDEX = join(INDEX, "staged");
    public static final File REMOVED_INDEX = join(INDEX, "removed");
    public static final File MASTER = join(REFS, "master");

    /* TODO: fill in the rest of this class. */
    private static ArrayList<String> branch = new ArrayList<>();


    /** Create persistence hierarchy
     *  .gitlet/ -- top level folder for all persistent data
     *      - objects/ -- folder containing all the commit and the tree object
     *      - HEAD -- file containing the current commit
     *      - index -- file containing the file next to be committed
     */
    private static void setUpPersistence() throws IOException {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }
        if (!OBJECTS.exists()) {
            OBJECTS.mkdir();
        }
        if (!BLOB_OBJECTS.exists()) {
            BLOB_OBJECTS.mkdir();
        }
        if (!COMMIT_OBJECTS.exists()) {
            COMMIT_OBJECTS.mkdir();
        }
        if (!REFS.exists()) {
            REFS.mkdir();
        }
        if (!HEAD.exists()) {
            HEAD.createNewFile();
        }
        if (!INDEX.exists()) {
            INDEX.mkdir();
        }
        if (!STAGED_INDEX.exists()) {
            STAGED_INDEX.mkdir();
        }
        if (!REMOVED_INDEX.exists()) {
            REMOVED_INDEX.mkdir();
        }
        if (!MASTER.exists()) {
            MASTER.createNewFile();
        }
    }

    /** Get the HEAD hash code that means one of the branches is active */
    private static String getHeadHashCode() {
        return Utils.readContentsAsString(HEAD);
    }

    /** Update the content of HEAD and the current active branch */
    private static void updateHeadHashCode(String hash) {
        File activeBranch = getPathOfActiveBranch();
        Utils.writeContents(HEAD, hash);
        Utils.writeContents(activeBranch, hash);
    }

    /** command init
     *  Usage: java gitlet.Main init
     *  Creates a new Gitlet version-control system in the current directory.
     * @throws IOException
     */
    public static void init() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the"
                    + " current directory");
        } else {
            setUpPersistence();
            branch.add("master");
            Commit initCommit = new Commit("initial commit");
            serializeInitCommit(initCommit);
        }
    }

    /** Serialize the initial commit object to the file system. */
    private static void serializeInitCommit(Commit c) throws IOException {
        String commitId = c.getCommitId();
        Utils.writeContents(HEAD, commitId);
        Utils.writeContents(MASTER, commitId);
        File filePath = getObjectPath(commitId, COMMIT_OBJECTS);
        Utils.writeObject(filePath, c);
    }

    /** Serialize the commit object to the file system. */
    private static void serializeCommit(Commit c) throws IOException {
        String commitId = c.getCommitId();
        Utils.writeContents(HEAD, commitId);
        File currBranch = getPathOfActiveBranch();
        Utils.writeContents(currBranch, commitId);
        File commitPath = getObjectPath(commitId, COMMIT_OBJECTS);
        Utils.writeObject(commitPath, c);
    }

    /** Deserialize the commit object from the file system. */
    private static Commit deSerializeCommit(String hash) throws IOException {
        File commitPath = getObjectPath(hash, COMMIT_OBJECTS);
        return Utils.readObject(commitPath, Commit.class);
    }

    /** Get the filepath of the branch which is active by checking the HEAD */
    private static File getPathOfActiveBranch() {
        String currentHeadHashCode = getHeadHashCode();
        String resultBranch = "";
        for (String branch: Utils.plainFilenamesIn(REFS)) {
            File branchpath = Utils.join(REFS, branch);
            String content = Utils.readContentsAsString(branchpath);
            if (currentHeadHashCode.equals(content)) {
                resultBranch = branch;
                break;
            }
        }
        return Utils.join(REFS, resultBranch);
    }

    /** Given a hash code, then create relative directory and file
     * @param typeOfObject There is two options of typeOfObject:
     *                     1. COMMIT_OBJECTS
     *                     2. BLOB_OBJECTS
     */
    private static File getObjectPath(String hash, File typeOfObject) throws IOException {
        File subDir = Utils.join(typeOfObject, hash.substring(0, 2));
        File file = Utils.join(subDir, hash.substring((2)));
        if (!subDir.exists()) {
            subDir.mkdir();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /** command add
     *  Usage: java gitlet.Main add [file name]
     *  Adds a copy of file as it currently exists to the staging area.
     * @param filename
     */
    public static void add(String filename) throws IOException {
        File sourceFilePath = Utils.join(CWD, filename);
        if (!sourceFilePath.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        addFileToStaged(sourceFilePath, filename);
    }

    /** Add a copy of file to the staged area.
     * @param cwdFilePath file path of the current working directory
     * @param filename the name of the file
     */
    private static void addFileToStaged(File cwdFilePath, String filename) throws IOException {
        Commit previousCommit = deSerializeCommit(getHeadHashCode());
        TreeMap<String, String> fileBlob  = previousCommit.getFileBlob();
        String cwdFileContent = Utils.readContentsAsString(cwdFilePath);
        String hashOfcwdFile = calcHashOfFile(filename, cwdFileContent);
        File toAddStage = Utils.join(STAGED_INDEX, filename);
        /** check last commit whether track this file */
        if (fileBlob.containsKey(filename)) {
            /** estimate the hash, in fact estimate the content of the file whether
             *  been modified.
             */
            if (!hashOfcwdFile.equals(fileBlob.get(filename))) {
                if (toAddStage.exists()) {
                    toAddStage.delete();
                }
                writeContents(toAddStage, cwdFileContent);
            }
        } else if (toAddStage.exists()) {
            String hashOfStageFileOutdate = calcHashOfFile(toAddStage, filename);
            if (!hashOfcwdFile.equals(hashOfStageFileOutdate)) {
                toAddStage.delete();
                writeContents(toAddStage, cwdFileContent);
            }
        } else {
            writeContents(toAddStage, cwdFileContent);
        }
    }

    /** Calculate the hash of the specific file */
    private static String calcHashOfFile(String filename, String fileContent) {
        return Utils.sha1(filename, fileContent);
    }

    /** Calculat ethe hash of the specific file */
    private static String calcHashOfFile(File filePath, String fileName) {
        String content = readContentsAsString(filePath);
        return sha1(fileName + content);
    }

    /** command commit
     *  Usage: java gitlet.Main commit [message]
     *  Saves  a snapshot of tracked files in the current commit and staging area.
     * @param message a log message that describe the changes to the files.
     * @throws IOException
     */
    public static void commit(String message) throws IOException {
        System.out.println("previous commitId: " + getHeadHashCode());
        Commit previousCommit = deSerializeCommit(getHeadHashCode());
        TreeMap<String, String> fileMap = previousCommit.getFileBlob();
        if (STAGED_INDEX.list().length == 0) {
            System.out.println("No changes added to the commit");
            System.exit(0);
        }
        for (String stagedFileName: Utils.plainFilenamesIn(STAGED_INDEX)) {
            File path = Utils.join(STAGED_INDEX, stagedFileName);
            String content = readContentsAsString(path);
            String hash = calcHashOfFile(stagedFileName, content);
            fileMap.put(stagedFileName, hash);
            File storagePath = getObjectPath(hash, BLOB_OBJECTS);
            Utils.writeContents(storagePath, content);
            //TODO 删除暂存区的文件
            File toBeDeleted = Utils.join(STAGED_INDEX, stagedFileName);
            toBeDeleted.delete();
        }
        Commit newCommit = new Commit(message, getHeadHashCode(), fileMap);
        updateHeadHashCode(newCommit.getCommitId());
        serializeCommit(newCommit);
    }

    /** command log
     *  Usage: java gitlet.Main log
     *  Starting at the current head commit, display information about each commit
     *  backwards along the commit tree.
     * @throws IOException
     */
    public static void log() throws IOException {
        String head = getHeadHashCode();
        File commitPath = getObjectPath(head, COMMIT_OBJECTS);
        Commit currentCommit = Utils.readObject(commitPath, Commit.class);
        while (currentCommit.getParentId().size() != 0) {
            printLogMessage(currentCommit);
            String previousCommit = currentCommit.getParentId().get(0);
            commitPath = getObjectPath(previousCommit, COMMIT_OBJECTS);
            currentCommit = Utils.readObject(commitPath, Commit.class);
        }
        printLogMessage(currentCommit);
    }

    /** Print the log message of current commit c. */
    private static void printLogMessage(Commit c) {
        System.out.println("===");
        System.out.println("commit " + c.getCommitId());
        if (c.getParentId().size() == 2) {
            System.out.println("Merge: " + c.getParentId().get(0).substring(0, 7)
                + " " + c.getParentId().get(1).substring(0, 7));
        }
        System.out.println("Date: " + c.getTimestamp());
        System.out.println(c.getMessage());
        System.out.println();
    }

    public static void status() {
        System.out.println("=== Branches ===");
        for (String branchName: Utils.plainFilenamesIn(REFS)) {
            File file = Utils.join(REFS, branchName);
            String hash = Utils.readContentsAsString(file);
            if (hash.equals(getHeadHashCode())) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.print("\n\n");
        System.out.println("=== Staged Files ===");
        for (String fileStaged: Utils.plainFilenamesIn(STAGED_INDEX)) {
            System.out.println(fileStaged);
        }
        System.out.print("\n\n");
        // TODO

    }
}
