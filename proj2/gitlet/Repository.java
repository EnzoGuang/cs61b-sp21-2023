package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**@Command: init
     * @Usage: java gitlet.Main init
     * @Description: Creates a new Gitlet version-control system in the current directory.
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

    /**@Command: add
     * @Usage: java gitlet.Main add [file name]
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

    /**@Command: commit
     * @Usage: java gitlet.Main commit [message]
     * @Description: Saves  a snapshot of tracked files in the current commit and staging area.
     * @param message a log message that describe the changes to the files.
     * @throws IOException
     */
    public static void commit(String message) throws IOException {
        System.out.println("previous commitId: " + getHeadHashCode());
        Commit previousCommit = deSerializeCommit(getHeadHashCode());
        TreeMap<String, String> fileMap = previousCommit.getFileBlob();
        if (STAGED_INDEX.list().length == 0 && REMOVED_INDEX.list().length == 0) {
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
        for (String removedFileName: Utils.plainFilenamesIn(REMOVED_INDEX)) {
            fileMap.remove(removedFileName);
            File toBeDeleted = Utils.join(REMOVED_INDEX, removedFileName);
            toBeDeleted.delete();
        }
        Commit newCommit = new Commit(message, getHeadHashCode(), fileMap);
        updateHeadHashCode(newCommit.getCommitId());
        serializeCommit(newCommit);
    }

    /**@Command: log
     * @Usage: java gitlet.Main log
     * @Description: Starting at the current head commit, display information about each commit
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

    public static void status() throws IOException {
        System.out.println("=== Branches ===");
        for (String branchName: Utils.plainFilenamesIn(REFS)) {
            File file = Utils.join(REFS, branchName);
            String hash = Utils.readContentsAsString(file);
            if (hash.equals(getHeadHashCode())) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String fileStaged: Utils.plainFilenamesIn(STAGED_INDEX)) {
            System.out.println(fileStaged);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String fileRemoved: Utils.plainFilenamesIn(REMOVED_INDEX)) {
            System.out.println(fileRemoved);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit currentCommit = deSerializeCommit(getHeadHashCode());
        TreeMap<String, String> fileBlob = currentCommit.getFileBlob();
        for (String filename: fileBlob.keySet()) {
            File cwdFilePath = Utils.join(CWD, filename);
            if (cwdFilePath.exists()) {
                String fileContent = Utils.readContentsAsString(cwdFilePath);
                String hashOfcwdFile = calcHashOfFile(filename, fileContent);
                if (Utils.plainFilenamesIn(STAGED_INDEX).contains(filename)) {
                    File stagedFilePath = Utils.join(STAGED_INDEX, filename);
                    String stagedFileContent = Utils.readContentsAsString(stagedFilePath);
                    String hashOfStagedFile = calcHashOfFile(filename, stagedFileContent);
                    if (!hashOfStagedFile.equals(hashOfcwdFile)) {
                        System.out.println(filename + " (modified)");
                    }
                } else {
                    if (!hashOfcwdFile.equals(fileBlob.get(filename))) {
                        System.out.println(filename + " (modified)");
                    }
                }
            } else {
                System.out.println(filename + " (deleted)");
            }
        }
        for (String filename: plainFilenamesIn(STAGED_INDEX)) {
            if (!fileBlob.containsKey(filename)) {
                File stagedFilePath = Utils.join(STAGED_INDEX, filename);
                File cwdFilePath = Utils.join(CWD, filename);
                String stagedFileContent = Utils.readContentsAsString(stagedFilePath);
                String cwdFileContent = Utils.readContentsAsString(cwdFilePath);
                String hashOfStaged = sha1(filename + stagedFileContent);
                String hashOfCwd = sha1(filename + cwdFileContent);
                if (!hashOfCwd.equals(hashOfStaged)) {
//                    System.out.println(filename + " (not track in current commit but track " +
//                            "next commit and content is different with staged version");
                    System.out.println(filename);
                }
            }
        }

        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String cwdFile: Utils.plainFilenamesIn(CWD)) {
            boolean stageAreaContain = plainFilenamesIn(STAGED_INDEX).contains(cwdFile);
            if (!fileBlob.containsKey(cwdFile) && !stageAreaContain) {
                System.out.println(cwdFile);
            }
        }
        System.out.println();
        // TODO

    }

    /**@Command: rm
     * @Usage: java gitlet.Main rm [file name]
     * @Description: Unstage the file if it is currently staged for addition.If the file is
     *  tracked in the current commit. Staged it for removal and remove the file
     *  from the cwd.
     * @Failure: If the file is neither staged nor tracked by the head commit, print
     * the error message "No reason to remove the file."
     */
    public static void rm(String filename) throws IOException {
        int failureCase = 0;
        boolean isStaged = Utils.plainFilenamesIn(STAGED_INDEX).contains(filename);
        if (isStaged) {
            failureCase++;
            File filepath = Utils.join(STAGED_INDEX, filename);
            filepath.delete();
        }
        Commit previousCommit = deSerializeCommit(getHeadHashCode());
        TreeMap<String, String> fileBlob = previousCommit.getFileBlob();
        File removePath = Utils.join(REMOVED_INDEX, filename);
        if (fileBlob.containsKey(filename)) {
            failureCase++;
            writeContents(removePath, "");
            File cwdFilePath = Utils.join(CWD, filename);
            if (cwdFilePath.exists()) {
                cwdFilePath.delete();
            }
        }
        if (failureCase == 0) {
            System.out.println("No reason to remove the file.");
        }
    }

    /**@Command: global-log
     * @Usage: java gitlet.Main global-log
     */
    public static void globalLog() throws IOException {
        String[] content = COMMIT_OBJECTS.list();
        Arrays.sort(content);
        for (String parentDir: content) {
            File subFilePath = Utils.join(COMMIT_OBJECTS, parentDir);
            for (String subDir: Utils.plainFilenamesIn(subFilePath)) {
                Commit currentCommit = deSerializeCommit(parentDir + subDir);
                printLogMessage(currentCommit);
            }
        }
    }

    /**@Command: find
     * @Usage: java gitlet.Main find
     * @Description: prints out the hash of all commit that have the given commit
     * message, one per line.
     * @Failure: print the error message "Found no commit with that message."
     * @param message
     * @throws IOException
     */
    public static void find(String message) throws IOException {
        boolean isExists = false;
        for (String parentDir: COMMIT_OBJECTS.list()) {
            File path = Utils.join(COMMIT_OBJECTS, parentDir);
            for (String subDir: Utils.plainFilenamesIn(path)) {
                Commit currentCommit = deSerializeCommit(parentDir + subDir);
                if (currentCommit.getMessage().contains(message)) {
                    isExists = true;
                    System.out.println(currentCommit.getCommitId());
                }
            }
        }
        if (!isExists) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**@Command: checkout
     * @Usage: <br>
     * 1. java gitlet.Main checkout -- [file name] <br>
     * 2. java gitlet.Main checkout [commit id] -- [file name] <br>
     * 3. java gitlet.Main checkout [branch name]
     * @Description:
     * 1. Takes the version of tile as it exists in the head commit,
     * and puts it in the working directory.<br>
     * 2. Takes the version of the file
     * as it exists in the commit with the given id. <br>
     * 3. Takes all files in the commit at the head of the given branch, and puts
     * them in the working directory, overwriting the versions of the files that
     * are already there if they exist.
     * @param flag There are three options<br>
     *             "1" represent the usage 1<br>
     *             "2" represent the usage 2<br>
     *             "3" represent the usage 3<br>
     */
    public static void checkout(String[] args, int flag) throws IOException {
        if (flag == 1) {
            checkoutFile(getHeadHashCode(), args[2]);
        } else if (flag == 2) {
            boolean exists = isCommitExists(args[1]);
            if (!exists) {
                System.out.println("No commit with that id exists");
                System.exit(0);
            }
            checkoutFile(args[1], args[3]);
        } else if (flag == 3) {
            // TODO
        }
    }

    private static void checkoutFile(String commitId, String filename) throws IOException {
        Commit currentCommit = deSerializeCommit(commitId);
        TreeMap<String, String> fileBlob = currentCommit.getFileBlob();
        if (!fileBlob.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
        } else {
            File toBeReplacedFile = Utils.join(CWD, filename);
            String hashOfCheckoutFile = fileBlob.get(filename);
            File checkoutFile = getObjectPath(hashOfCheckoutFile, BLOB_OBJECTS);
            toBeReplacedFile.delete();
            String content = readContentsAsString(checkoutFile);
            Utils.writeContents(toBeReplacedFile, content);
        }
    }

    /** Estimate the commit whether exists. */
    private static boolean isCommitExists(String commitId) {
        File commitPath = Utils.join(COMMIT_OBJECTS, commitId.substring(0, 2));
        if (!commitPath.exists()) {
            return false;
        }
        File subPath = Utils.join(commitPath, commitId.substring(2));
        if (!subPath.exists()) {
            return false;
        }
        return true;
    }
}
