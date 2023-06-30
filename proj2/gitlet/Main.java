package gitlet;

import java.io.IOException;
import static gitlet.Repository.GITLET_DIR;
/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author EnzoGuang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        try {
            switch (firstArg) {
                case "init":
                    validateNumArgs(args, 1);
                    Repository.init();
                    break;
                case "add":
                    isGitletRepo();
                    validateNumArgs(args, 2);
                    Repository.add(args[1]);
                    break;
                case "commit":
                    isGitletRepo();
                    validateCommit(args);
                    Repository.commit(args[1]);
                    break;
                case "rm":
                    isGitletRepo();
                    validateNumArgs(args, 2);
                    Repository.rm(args[1]);
                    break;
                case "log":
                    isGitletRepo();
                    validateNumArgs(args, 1);
                    Repository.log();
                    break;
                case "global-log":
                    isGitletRepo();
                    validateNumArgs(args, 1);
                    Repository.globalLog();
                    break;
                case "find":
                    isGitletRepo();
                    validateNumArgs(args, 2);
                    Repository.find(args[1]);
                    break;
                case "status":
                    isGitletRepo();
                    validateNumArgs(args, 1);
                    Repository.status();
                    break;
                case "checkout":
                    isGitletRepo();
                    int flag = validateCheckout(args);
                    Repository.checkout(args, flag);
                    break;
                case "branch":
                    isGitletRepo();
                    validateNumArgs(args, 2);
                    Repository.branch(args[1]);
                    break;
                case "rm-branch":
                    isGitletRepo();
                    validateNumArgs(args, 2);
                    Repository.rm_branch(args[1]);
                    break;
                case "reset":
                    isGitletRepo();
                    validateNumArgs(args, 2);
                    Repository.reset(args[1]);
                    break;
                case "merge":
                    isGitletRepo();
                    validateNumArgs(args, 2);
                    break;
                default:
                    System.out.println("No command with that exists.");
                    System.exit(0);
            }
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    /** Checks the number of arguments versus the expected number,
     *  print Incorrect operands and exit.
     * @param args Argument array from command line.
     * @param n Number of expected operand arguments
     */
    private static void validateNumArgs(String[] args, int n) {
        if (n != args.length) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Check the number of arguments of different usage of checkout.
     * @usage:<br>
     *  1.  java gitlet.Main checkout -- [file name]<br>
     *  2.  java gitlet.Main checkout [commit id] -- [fil name]<br>
     *  3.  java gitlet.Main chekcout [branch name]<br>
     * @param args
     */
    private static int validateCheckout(String[] args) {
        if (args.length > 2 && args[1].equals("--")) {
            validateNumArgs(args, 3);
            return 1;
        } else if (args.length > 3 && args[2].equals("--")) {
            validateNumArgs(args, 4);
            return 2;
        } else {
            validateNumArgs(args, 2);
            return 3;
        }
    }

    private static void validateCommit(String[] args) {
        if (args.length == 1) {
            System.out.println("Please enter a commit message");
            System.exit(0);
        } else {
            validateNumArgs(args, 2);
        }
    }

    private static void isGitletRepo() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
