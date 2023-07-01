package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author EnzoGuang
 */
public class Repository {
    /**
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
        String branchName = Utils.readContentsAsString(HEAD);
        File branch = Utils.join(REFS, branchName);
        return Utils.readContentsAsString(branch);
    }

    /** Update the content of HEAD and the current active branch */
    private static void updateHeadHashCode(String hash) {
        String activeBranch = getNameOfActiveBranch();
        File activeBranchPath = Utils.join(REFS, activeBranch);
        Utils.writeContents(HEAD, activeBranch);
        Utils.writeContents(activeBranchPath, hash);
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
        Utils.writeContents(HEAD, "master");
        Utils.writeContents(MASTER, commitId);
        File filePath = getObjectPath(commitId, COMMIT_OBJECTS);
        Utils.writeObject(filePath, c);
    }

    /** Serialize the commit object to the file system. */
    private static void serializeCommit(Commit c) throws IOException {
        String commitId = c.getCommitId();
        updateHeadHashCode(commitId);
        File commitPath = getObjectPath(commitId, COMMIT_OBJECTS);
        Utils.writeObject(commitPath, c);
    }

    /** Deserialize the commit object from the file system. */
    private static Commit deSerializeCommit(String hash) throws IOException {
        File commitPath = getObjectPath(hash, COMMIT_OBJECTS);
        return Utils.readObject(commitPath, Commit.class);
    }

    /** Get the name of the branch which is active by checking the HEAD */
    private static String getNameOfActiveBranch() {
        return Utils.readContentsAsString(HEAD);
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
        String contentOfHead = Utils.readContentsAsString(HEAD);
        for (String branchName: Utils.plainFilenamesIn(REFS)) {
            if (contentOfHead.equals(branchName)) {
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
            File commitPath = isCommitExists(args[1]);
            if (!commitPath.exists()) {
                System.out.println("No commit with that id exists");
                System.exit(0);
            }
            String completeHash = extendAbbrHash(args[1]);
            checkoutFile(completeHash, args[3]);
        } else if (flag == 3) {
            String checkoutBranchName = args[1];
            checkoutBranch(checkoutBranchName);
        }
    }

    /** checkout the files of the head commit or files of the specific commit. */
    private static void checkoutFile(String commitId, String filename) throws IOException {
        Commit currentCommit = deSerializeCommit(commitId);
        TreeMap<String, String> fileBlob = currentCommit.getFileBlob();
        if (!fileBlob.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
        } else {
            File toBeReplacedFile = Utils.join(CWD, filename);
            String hashOfCheckoutFile = fileBlob.get(filename);
            File checkoutFile = getObjectPath(hashOfCheckoutFile, BLOB_OBJECTS);
            if (toBeReplacedFile.exists()) {
                toBeReplacedFile.delete();
            }
            String content = readContentsAsString(checkoutFile);
            Utils.writeContents(toBeReplacedFile, content);
        }
    }

    /** Checkout the branch, and update the current active branch. */
    private static void checkoutBranch(String checkoutBranchName) throws IOException {
        checkoutBranchFailureCase(checkoutBranchName);
        checkoutBranchOnlyFiles(checkoutBranchName);
        /** modified the content of the HEAD, now HEAD point to the checkout branch */
        writeContents(HEAD, checkoutBranchName);
    }

    /** Checks everything is correct with the branch name */
    private static void checkoutBranchFailureCase(String checkoutBranchName) {
        if (!plainFilenamesIn(REFS).contains(checkoutBranchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        File checkoutBranPath = Utils.join(REFS, checkoutBranchName);
        if (checkoutBranchName.equals(readContentsAsString(HEAD))) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        File currentBranPath = Utils.join(REFS, getNameOfActiveBranch());
        String hashOfCheckoutBr = Utils.readContentsAsString(checkoutBranPath);
        String hashOfCurrentBr = Utils.readContentsAsString(currentBranPath);
        if (hashOfCurrentBr.equals(hashOfCheckoutBr)) {
            writeContents(HEAD, checkoutBranchName);
            System.exit(0);
        }
    }

    /** checkout the branch, modified the current working directory but not modified the HEAD. */
    private static void checkoutBranchOnlyFiles(String checkoutBranch) throws IOException {
        File checkoutBranPath = Utils.join(REFS, checkoutBranch);
        String commitId = readContentsAsString(checkoutBranPath);
        checkoutWithCommitId(commitId);
    }

    /** checkout the commit with the given id. */
    private static void checkoutWithCommitId(String givenCommitId) throws IOException {
        Commit currentCommit = deSerializeCommit(getHeadHashCode());
        TreeMap<String, String> fileBlobCurr = currentCommit.getFileBlob();
        Commit checkoutCommit = deSerializeCommit(givenCommitId);
        TreeMap<String, String> fileBlobCheckout = checkoutCommit.getFileBlob();
        for (String file: fileBlobCheckout.keySet()) {
            File cwdFile = Utils.join(CWD, file);
            if (!fileBlobCurr.containsKey(file) && cwdFile.exists()) {
                System.out.println("There is an untracked file in the way; delete it"
                        + ", or add and commit it first.");
                System.exit(0);
            }
        }
        /** Any files that are tracked in the current branch but are not present
         * in the checked-out branch are deleted.
         */
        for (String filename: fileBlobCurr.keySet()) {
            if (!fileBlobCheckout.containsKey(filename)) {
                File toBeDelete = Utils.join(CWD, filename);
                if (toBeDelete.exists()) {
                    toBeDelete.delete();
                }
            }
        }
        for (String filename: fileBlobCheckout.keySet()) {
            File cwdFilePath = Utils.join(CWD, filename);
            if (cwdFilePath.exists()) {
                cwdFilePath.delete();
            }
            String replacedFileHash = fileBlobCheckout.get(filename);
            File toBeRepacedFile = getObjectPath(replacedFileHash, BLOB_OBJECTS);
            String replacedContent = readContentsAsString(toBeRepacedFile);
            writeContents(cwdFilePath, replacedContent);
        }
        for (String file: Utils.plainFilenamesIn(STAGED_INDEX)) {
            File toBeDelete = Utils.join(STAGED_INDEX, file);
            if (toBeDelete.exists()) {
                toBeDelete.delete();
            }
        }
    }

    /**@Usage: java gitlet.Main reset [commit id]
     * @Description: Checks out all files by the given commit. The command is
     * essentially checkout of an arbitrary commit that also changes the current
     * branch head.
     * @param commitId
     * @throws IOException
     */
    public static void reset(String commitId) throws IOException {
        File commitPath = isCommitExists(commitId);
        if (!commitPath.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        String completeCommitId = extendAbbrHash(commitId);
        checkoutWithCommitId(completeCommitId);
        String currentBranch = Utils.readContentsAsString(HEAD);
        File branchPath = Utils.join(REFS, currentBranch);
        Utils.writeContents(branchPath, completeCommitId);
    }

    /** Estimate the given commitId whether exists. */
    private static File isCommitExists(String commitId) {
        File resultPath = Utils.join(COMMIT_OBJECTS, commitId.substring(0, 2));
        if (commitId.length() != 40) {
            if (resultPath.exists()) {
                String restSubCommitId = commitId.substring(2);
                for (String temp: plainFilenamesIn(resultPath)) {
                    if (temp.contains(restSubCommitId)) {
                        resultPath = Utils.join(resultPath, temp);
                        break;
                    }
                }
            }
        } else {
            resultPath = Utils.join(resultPath, commitId.substring(2));
        }
        return resultPath;
    }

    /** Extend the abbreviates bits of the commitId. */
    private static String extendAbbrHash(String commitId) {
        if (commitId.length() != 40) {
            File path = Utils.join(COMMIT_OBJECTS, commitId.substring(0, 2));
            String restSubCommitId = commitId.substring(2);
            for (String temp: plainFilenamesIn(path)) {
                if (temp.contains(restSubCommitId)) {
                    return commitId.substring(0, 2) + temp;
                }
            }
        }
        return commitId;
    }

    /**@Command branch
     * @Usage: java gitlet.Main branch [branch name]
     * @Description: Create a new branch with the given name, and points it at the
     * current head commit.
     */
    public static void branch(String branchName) {
        for (String name: Utils.plainFilenamesIn(REFS)) {
            if (branchName.equals(name)) {
                System.out.println("A branch with that name already exists.");
                System.exit(0);
            }
        }
        File newBranch = Utils.join(REFS, branchName);
        writeContents(newBranch, getHeadHashCode());
    }

    /**@Command rm-branch
     * @Usage: java gitlet.Main rm-branch [branch name]
     */
    public static void rm_branch(String branchName) {
        if (!Utils.plainFilenamesIn(REFS).contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (Utils.readContentsAsString(HEAD).equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        File branchPath = Utils.join(REFS, branchName);
        if (branchPath.exists()) {
            branchPath.delete();
        }
    }

    /** Get the all commit of the given branch name. */
    private static ArrayList<String> getCommitFromBranch(String branchName) throws IOException {
        if (!Utils.plainFilenamesIn(REFS).contains(branchName)) {
            System.exit(0);
        }
        File branchPath = Utils.join(REFS, branchName);
        String commitId = Utils.readContentsAsString(branchPath);
        Commit commit = deSerializeCommit(commitId);
        ArrayList<String> list = new ArrayList<>();
        while (commit.getParentId().size() != 0) {
            list.add(commit.getCommitId());
            String parentId = commit.getParentId().get(0);
            commit = deSerializeCommit(parentId);
        }
        list.add(commit.getCommitId());
        return list;
    }

    /** Generate a graph that all the vertices are consist of the given branch's
     * commit
     */
    private static Graph generateGraph(String branchName) throws IOException {
        ArrayList<String> list = getCommitFromBranch(branchName);
        Graph graph = new Graph(list);
        for (int i = 0; i < list.size() - 1; i++) {
            graph.addEdge(list.get(i), list.get(i + 1));
        }
        return graph;
    }

    /** Using Breadth First Search Algorithm to calculate the distance from every
     * vertex to the start vertex which is the current branch's head.
     */
    private static TreeMap<String, Integer> getDistanceMap(String branchName) throws IOException {
        Graph graph = generateGraph(branchName);
        File branch = Utils.join(REFS, branchName);
        String content = Utils.readContentsAsString(branch);
        BreadthFirstSearch bfsPath = new BreadthFirstSearch(graph, content);
        bfsPath.bfs(graph, content);
        TreeMap<String, Integer> dist = bfsPath.getDistTo();
        System.out.println(branchName + ": " + dist);
        return dist;
    }

    /** Get the split point's hashcode */
    public static String getSplitPoint(String currentBranch, String mergeBranch) throws IOException {
        TreeMap<String, Integer> currBranMap = getDistanceMap(currentBranch);
        TreeMap<String, Integer> mergeBranMap = getDistanceMap(mergeBranch);
        String commitId = null;
        int minDist = 1000000;
        for (String id: currBranMap.keySet()) {
            if (mergeBranMap.containsKey(id)) {
                if (currBranMap.get(id) < minDist) {
                    minDist = currBranMap.get(id);
                    commitId = id;
                }
            }
        }
        return commitId;
    }
}
