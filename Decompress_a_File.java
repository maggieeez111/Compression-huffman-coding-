/*
 * Group 4
 *   Trevor Hong
 *   Vijay Sharma
 *   Vinay Singamsetty
 *   Maiqi Zhang
 *
 * Decompresses a previously compressed file so that it replicates the original source file with command-line arguments.
 * Intended Usage:
 *     java Decompress_a_File.java input.txt output.txt
 */


import java.io.*;

public class Decompress_a_File {
    public static void main(String[] args) throws IOException {
        try {
            if (args.length != 2) {
                System.out.println("Intended usage:\n\tjava Decompress_a_File.java input.txt output.txt");
                System.exit(1);
            }

            File sourceFile = new File(args[0]);
            if (!sourceFile.exists()) {
                System.out.println("Error. No such source file exists.");
                System.exit(1);
            }

            File outputFile = new File(args[1]);
            if (!outputFile.exists()) {
                outputFile.createNewFile();
                System.out.println("Created file " + outputFile.getName() + ".");
            }

            // Initialize Input Streams
            FileInputStream input = new FileInputStream(sourceFile);
            ObjectInputStream codesInput = new ObjectInputStream(input);
            BitInputStream bitInput = new BitInputStream(input);
            String[] codes;

            // Reads In String Array For Codes & Int For Length
            codes = (String[]) codesInput.readObject();
            int resultLength = codesInput.readInt();

            StringBuilder bitString = new StringBuilder();
            StringBuilder result = new StringBuilder();

            while (resultLength-- > 0) {
                bitString.append(bitInput.readBit() ? '1' : '0');

                for (int i = 0; i < codes.length; i++) {
                    // If We Have Found a Code That Matches Our Current Bit String
                    if (codes[i] != null && codes[i].equals(bitString.toString())) {
                        bitString = new StringBuilder();

                        result.append((char) i);
                    }
                }
            }

            FileWriter fileWriter = new FileWriter(outputFile);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(result.toString());

            bw.close();
            codesInput.close();
            bitInput.close();
        } catch (Exception e) {
            System.out.println("An unexpected error occurred.");
            System.out.println("\t" + e);
        }
    }

    // Allows User to Read Individual Bits, Or Bits In Binary String.
    static class BitInputStream {
        private FileInputStream input;
        private byte currentByte = 0;
        private int position = 0;

        /**
         * Creates a new BitInput stream from the given FileInputStream
         * @param input A file InputStream
         */
        public BitInputStream(FileInputStream input) {
            this.input = input;
        }


        /**
         * Creates a new BitInput stream from the given File
         * @param file
         */
        public BitInputStream(File file) throws IOException {
            this(new FileInputStream(file));
        }

        /**
         * Reads an individual bit from the stream.
         * @return A boolean representing true for 1 and false for 0.
         */
        public boolean readBit() throws IOException {
            // Calculate the offset within the byte and read a new byte if needed.
            int offset = position++ % 8;
            if (offset == 0)
                currentByte = (byte) input.read();

            // Calculate the bit and return it in a character-form.
            int mask = 1 << (7 - offset);
            return (currentByte & mask) != 0;
        }


        /**
         * Closes the BitInputStream's file handles.
         */
        public void close() throws IOException {
            input.close();
        }
    }
}