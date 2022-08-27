package org.taanisak.javi.models;

import java.util.LinkedList;
import java.util.Queue;

public class Rope {
    protected static class Node {
        Node left;
        Node right;
        int lengthWeight;
        String data;

        public Node() {}

        public Node(String str) {
            data = str;
            lengthWeight = str.length();
        }

        private Node(Node left, Node right) {
            if (left.lengthWeight == 0) {
                left = right;
                right = null;
            }

            this.left = left;
            this.right = right;
            lengthWeight = getWeight(left);
        }

        protected static int getWeight(Node leftChild) {
            int weight = leftChild.lengthWeight;
            if (!leftChild.isLeaf() && leftChild.right != null) {
                do {
                    weight += leftChild.right.lengthWeight;
                    leftChild = leftChild.right;
                } while (leftChild.right != null);
            }
            return weight;
        }

        public boolean isLeaf() {
            return data != null;
        }

        public boolean isEmpty() {
            return lengthWeight == 0;
        }
    }

    private static class Pair {
        Node left;
        Node right;

        public Pair(Node left, Node right) {
            this.left = left;
            this.right = right;
        }
    }

    private Node root;

    public void insert(String str, int index) {
        if (str == null || str.length() == 0) {
            return;
        }

        Node newNode = new Node(str);
        if (root == null) {
            root = newNode;
        } else {
            Pair splitNodes = split(index, root);
            root = concatenate(splitNodes.left, concatenate(newNode, splitNodes.right));
        }
    }

    public void delete(int from, int to) {
        if (root == null) {
            return;
        }

        if (from == 0) {
            Pair splitNodes = split(to, root);
            root = splitNodes.right;

            if (root.isEmpty()) {
                root = null;
            }
        } else {
            Pair splitNodesLeft = split(from, root);
            Pair splitNodesRight = split(to - from, splitNodesLeft.right);
            root = concatenate(splitNodesLeft.left, splitNodesRight.right);
        }
    }

    private Node concatenate(Node left, Node right) {
        return new Node(left, right);
    }

    private Pair split(int index, Node startingRoot) {
        Queue<Node> prunedNodes = new LinkedList<>();
        goDownPathAndUpdate(index, startingRoot, prunedNodes);
        return new Pair(startingRoot, rightSideSplit(prunedNodes));
    }

    private void goDownPathAndUpdate(int index, Node current, Queue<Node> pruneNodes) {
        if (current.isLeaf()) {
            pruneNodes.add(new Node(current.data.substring(index)));
            current.data = current.data.substring(0, index);
            current.lengthWeight = current.data.length();
        } else {
            Node selectedChild = null; //Remember which child we went down
            if (current.lengthWeight <= index && current.right != null) {
                selectedChild = current.right;
                goDownPathAndUpdate(index - current.lengthWeight, selectedChild, pruneNodes);
            } else {
                selectedChild = current.left;
                goDownPathAndUpdate(index, selectedChild, pruneNodes);
            }

            //Update the parent node based on child node
            if (current.right != selectedChild) { //only prune right side if child was left
                pruneNodes.add(current.right);
                current.right = null;
            } else if (current.left.isEmpty()) { //fixing the child links
                current.left = current.right;
                current.right = null;
            } else if (current.right.isEmpty()) {
                current.right = null;
            }

            current.lengthWeight = Node.getWeight(current.left);
        }
    }

    private Node rightSideSplit(Queue<Node> prunedNodes) {
        Node rightSide = prunedNodes.remove();
        while (!prunedNodes.isEmpty()) {
            rightSide = concatenate(rightSide, prunedNodes.remove());
        }
        return rightSide;
    }

    public char charAt(int index) {
        return charAt(index, root);
    }

    private char charAt(int index, Node current) {
        if (current == null) {
            return '\0';
        }

        if (!current.isLeaf()) {
            if (current.lengthWeight <= index && current.right != null) {
                return charAt(index - current.lengthWeight, current.right);
            } else {
                return charAt(index, current.left);
            }
        } else {
            return current.data.charAt(index);
        }
    }

    public String report(int start, int end) {
        return report(start, end, root);
    }

    private String report(int start, int end, Node current) {
        if (current == null || end < 1) {
            return "";
        }

        if (current.isLeaf()) {
            if (start > current.lengthWeight) {
                return "";
            }

            if (end > current.lengthWeight) {
                end = current.lengthWeight;
            }

            if (start < 0) {
                start = 0;
            }

            return current.data.substring(start, end);
        }

        return report(start, end, current.left)
                + report(start - current.lengthWeight, end - current.lengthWeight, current.right);
    }

    public static void main(String[] args) {
        Rope rope = new Rope();
        rope.insert("llo_", 0);
        rope.insert("na", 4);
        rope.insert("my_", 4);
        rope.insert("me_i", 9);
        rope.insert("s", 13);
        rope.insert("_Simon", 14);
        rope.insert("He", 0);

        String finalInput = "Hello_my_name_is_Simon";
        for (int i = 0; i < finalInput.length(); i++) {
            System.out.print(rope.charAt(i));
        } //Hello_my_name_is_Simon

        rope.delete(0, finalInput.length()/2); //delete "Hello_my_na",
        System.out.println("\n" + rope.report(0, finalInput.length())); //me_is_Simon
        rope.delete(3, 5); //delete "is", result = me__Simon
        System.out.println(rope.report(0, finalInput.length())); //me__Simon
        rope.insert("Hello my na", 0);
        rope.insert("is", 14);
        System.out.println(rope.report(0, finalInput.length())); //Hello_my_name_is_Simon
    }
}
