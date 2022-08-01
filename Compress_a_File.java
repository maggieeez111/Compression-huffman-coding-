/*
 * Group 4
 *   Trevor Hong
 *   Vijay Sharma
 *   Vinay Singamsetty
 *   Maiqi Zhang
 *
 * Compress a source file into a target file using the Huffman coding method with command-line arguments.
 * Intended Usage:
 *     java Compress_a_File.java input.txt output.txt
 *
 * Reference 1: Textbook List25.11 HuffmanCode.java
 * Reference 2: Textbook List23.9 Heap.java
 * Reference 3: Textbook List23.11 CreateLargeFile.java
 *
 * NOTES:
 * Compressed file sizes may be larger than the original data.
 * This depends on the data inputted, but usually it is less.
 */

import java.io.*;
import java.util.ArrayList;

public class Compress_a_File {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Intended usage:\n\tjava Compress_a_File.java input.txt output.txt");
            System.exit(1);
        }

        File sourceFile = new File(args[0]);
        File outputFile = new File(args[1]);
        if (!sourceFile.exists()) {

            sourceFile.createNewFile();

            FileWriter fileWriter = new FileWriter(sourceFile, true);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write("This course is hard.");
            bw.close();

            System.out.println("Created file " + args[0] + " .");
        }

        DataInputStream sourceFileStream = new DataInputStream(
                new BufferedInputStream(new FileInputStream(sourceFile)));
        int size = sourceFileStream.available();
        byte[] b = new byte[size];
        sourceFileStream.read(b);
        sourceFileStream.close();
        String text = new String(b);

        int[] counts = getCharacterFrequency(text);
        Tree tree = getHuffmanTree(counts);
        String[] codes = getCode(tree.root);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append(codes[text.charAt(i)]);
        }

        ObjectOutputStream codesOutput = new ObjectOutputStream(new FileOutputStream(args[1]));
        codesOutput.writeObject(codes);
        codesOutput.writeInt(result.length());
        codesOutput.close();

        BitOutputStream output = new BitOutputStream(outputFile);
        output.writeBit(result.toString());
        output.close();

        System.out.printf(
                "File %s compressed from %d bytes to %d bytes. %nThat is a %f%% %s in file size. %n",
                sourceFile.getName(),
                sourceFile.length(),
                outputFile.length(),
                ((double) outputFile.length() / sourceFile.length()) * 100,
                sourceFile.length() < outputFile.length() ? "increase" : "decrease"
        );
    }

    static class BitOutputStream {
        private ArrayList<Integer> bits = new ArrayList<>();
        private DataOutputStream output;
        private int value;
        private int count = 0;
        private int invoke = 1;

        public BitOutputStream(File file) throws FileNotFoundException {
            output = new DataOutputStream(new FileOutputStream(file, true));
        }

        /**
         * Writes a bit to the file output.
         * @param bit The bit to write, either '0' or '1'.
         */
        public void writeBit(char bit) throws IOException {
            // Shift Value To The Left
            value = value << 1;
            // Add # Of Bit Values
            count++;
            // Check If Bit Value Is Equal To 1
            if (bit == '1') {
                value = value | invoke;
            }
            // Make Sure Bit Number Is Equal To 8
            if (count == 8) {
                output.write(value);
                count = 0;
            }
        }

        /**
         * Traverses the bit string and invokes the writeBit() method for each character.
         * @param bit The bit string to write.
         */
        public void writeBit(String bit) throws IOException {
            for (int i = 0; i < bit.length(); i++)
                writeBit(bit.charAt(i));
        }

        /**
         * Closes the output stream and writes the remaining byte after left shifting.
         */
        public void close() throws IOException {
            if (count > 0) {
                value = value << (8 - count);
                output.write(value);
            }
            output.close();
        }
    }

    /**
     * @param root The root TreeNode of the Huffman tree.
     * @return A string[] repesenting the codes for each character.
     */
    public static String[] getCode(Tree.Node root) {
        if (root == null)
            return null;
        String[] codes = new String[2 * 128];
        assignCode(root, codes);
        return codes;
    }

    /**
     * Recursively assigns codes for each element, storing them in the second paramater.
     * @param root The root TreeNode of the Huffman tree.
     * @param codes The String[] representing the codes for each character.
     */
    private static void assignCode(Tree.Node root, String[] codes) {
        if (root.left != null) {
            root.left.code = root.code + "0";
            assignCode(root.left, codes);

            root.right.code = root.code + "1";
            assignCode(root.right, codes);
        } else {
            codes[(int) root.element] = root.code;
        }
    }

    /**
     * Retrieves a  Huffman Tree from the frequency.
     * @param counts The frequency for each character.
     */
    public static Tree getHuffmanTree(int[] counts) {
        // Create a heap to hold trees
        Heap<Tree> heap = new Heap<Tree>();
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0)
                heap.add(new Tree(counts[i], (char) i)); // Leaf Node Tree
        }

        while (heap.getSize() > 1) {
            // Remove Smallest Weight Tree
            Tree t1 = heap.remove();
            // Remove The Next Smallest Weight
            Tree t2 = heap.remove();
            // Combine Subtrees T1 & T2
            heap.add(new Tree(t1, t2));
        }
        return heap.remove(); // Finalized Tree
    }


    /**
     * Finds the character frequency for text.
     * @param text The input text.
     * @return An array with the frequencies for each character.
     */
    public static int[] getCharacterFrequency(String text) {
        int[] counts = new int[256];

        for (int i = 0; i < text.length(); i++)
            counts[(int) text.charAt(i)]++;
        return counts;
    }

    // Define Huffman Coding Tree
    public static class Tree implements Comparable<Tree> {
        Node root;

        // Create Tree and Sub-Trees T1 & T2
        public Tree(Tree t1, Tree t2) {
            root = new Node();
            root.left = t1.root;
            root.right = t2.root;
            root.weight = t1.root.weight + t2.root.weight;
        }

        // Create Tree That Has A Leaf Node
        public Tree(int weight, char element) {
            root = new Node(weight, element);
        }

        // Utilize @Override Annotation To Compare The Trees Based On Weights
        @Override
        public int compareTo(Tree t) {
            if (root.weight < t.root.weight) // Purposely reverse the order
                return 1;
            else if (root.weight == t.root.weight)
                return 0;
            else
                return -1;
        }

        public class Node {
            char element; // Stores Characters For A Leaf Node
            int weight; // Weight Of Subtree Rooted At This Node
            Node left; // Left Sub-Tree Reference
            Node right; // Right Sub-Tree Reference
            String code = ""; // Code Of This Node From The Root

            // Create Empty Node
            public Node() {
            }

            // Create Node With Defined Weight & Character
            public Node(int weight, char element) {
                this.weight = weight;
                this.element = element;
            }
        }
    }

    static class Heap<E extends Comparable<E>> {
        private java.util.ArrayList<E> list = new java.util.ArrayList<E>();

        // Create Default/Empty Heap
        public Heap() {
        }

        // Create Heap From An Array Of Objects
        public Heap(E[] objects) {
            for (int i = 0; i < objects.length; i++)
                add(objects[i]);
        }

        // Add New Object Into The Heap
        public void add(E newObject) {
            list.add(newObject); // Append Object To The Heap
            int currentIndex = list.size() - 1; // Index Of The Last Node

            while (currentIndex > 0) {
                int parentIndex = (currentIndex - 1) / 2;
                // Swap If The Current Object Is Greater Than Its Parent
                if (list.get(currentIndex).compareTo(list.get(parentIndex)) > 0) {
                    E temp = list.get(currentIndex);
                    list.set(currentIndex, list.get(parentIndex));
                    list.set(parentIndex, temp);
                } else
                    break; // The Tree Is Now A Heap

                currentIndex = parentIndex;
            }
        }

        // Remove the root from the heap
        public E remove() {
            if (list.size() == 0)
                return null;

            E removedObject = list.get(0);
            list.set(0, list.get(list.size() - 1));
            list.remove(list.size() - 1);

            int currentIndex = 0;
            while (currentIndex < list.size()) {
                int leftChildIndex = 2 * currentIndex + 1;
                int rightChildIndex = 2 * currentIndex + 2;

                // Finding The Maximum Between Two Child Indexes
                if (leftChildIndex >= list.size())
                    break; // The Tree Is Now A Heap
                int maxIndex = leftChildIndex;
                if (rightChildIndex < list.size()) {
                    if (list.get(maxIndex).compareTo(list.get(rightChildIndex)) < 0) {
                        maxIndex = rightChildIndex;
                    }
                }

                // Swap If Current Node Is Less Than The Maximum
                if (list.get(currentIndex).compareTo(list.get(maxIndex)) < 0) {
                    E temp = list.get(maxIndex);
                    list.set(maxIndex, list.get(currentIndex));
                    list.set(currentIndex, temp);
                    currentIndex = maxIndex;
                } else
                    break; // The Tree Is Now A Heap
            }

            return removedObject;
        }

        // Retrieve The # Of Nodes In The Tree
        public int getSize() {
            return list.size();
        }
    }
}