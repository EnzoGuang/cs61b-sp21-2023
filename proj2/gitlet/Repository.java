package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    public static final File REFS = join(GITLET_DIR, "refs");
    public static final File HEAD = join(GITLET_DIR, "head");
    public static final File INDEX = join(GITLET_DIR, "index");
    public static final File MASTER = join(REFS, "master");

    /* TODO: fill in the rest of this class. */
    public static ArrayList<String> branch = new ArrayList<>();

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
        if (!REFS.exists()) {
            REFS.mkdir();
        }
        if (!HEAD.exists()) {
            HEAD.createNewFile();
        }
        if (!INDEX.exists()) {
            INDEX.createNewFile();
        }
        if (!MASTER.exists()) {
            MASTER.createNewFile();
        }
    }

    private static String getHead() {
        return Utils.readContentsAsString(HEAD);
    }

    public static void init() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the" +
                    " current directory");
        } else {
            setUpPersistence();
            branch.add("master");
            Commit initCommit = new Commit("initial commit");
            persistentCommit(initCommit);
        }
    }

    /** Persistent the commit object to the file system. */
    private static void persistentCommit(Commit c) throws IOException {
        String commitId = c.getCommitId();
        Utils.writeContents(HEAD, commitId);
        Utils.writeContents(MASTER, commitId);
        File filePath = createAndGetObjectPath(commitId);
        Utils.writeObject(filePath, c);
    }

    /* Given a hash code, then create relative directory and file */
    private static File createAndGetObjectPath(String hash) throws IOException {
        File subDir = join(OBJECTS, hash.substring(0, 2));
        File file = join(subDir, hash.substring((2)));
        if (!subDir.exists()) {
            subDir.mkdir();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public static void log() throws IOException {
        String head = getHead();
        File file = createAndGetObjectPath(head);
        Commit currentCommit = Utils.readObject(file, Commit.class);
        while (true) {
            printLogMessage(currentCommit);
            ArrayList<String> parent = currentCommit.getParentId();
            if (parent.size() == 0) {
                break;
            } else {
                file = createAndGetObjectPath(parent.get(0));
                currentCommit = Utils.readObject(file, Commit.class);
            }
        }
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
    }

    public static void status() {
        System.out.println("=== Branches ===");
        for (String branchName: Utils.plainFilenamesIn(REFS)) {
            File file = Utils.join(REFS, branchName);
            String hash = Utils.readContentsAsString(file);
            if (hash.equals(getHead())) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.print("\n\n");
        System.out.println("=== Staged Files ===");
    }
}
