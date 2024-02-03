class TreeOps {
    public static void main(String[] args) {
        System.out.println(new B().run());
    }
}

class B {
    public int run() {
        TreeNode root;
        int rval;
        root = buildTree();
        rval = processTree(root);
        return rval;
    }

    private TreeNode buildTree() {
        // Implement your logic to build a tree
        // For demonstration purposes, a simple tree is created here
        TreeNode root = new TreeNode(ONE());
        root.left = new TreeNode(TWO());
        return root;
    }

    private int processTree(TreeNode node) {
        // Implement your logic to process the tree
        if (node == null) {
            return 0;
        }
        return node.value + processTree(node.left) + processTree(node.right);
    }

    // Define ONE() and TWO() as static methods
    public static int ONE() {
        return 1;
    }

    public static int TWO() {
        return 2;
    }

    static class TreeNode {
        int value;
        TreeNode left;
        TreeNode right;

        public TreeNode(int value) {
            this.value = value;
        }
    }
}
