package compressor;
import base.Compressor;
import includes.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;


public class HuffmanEncoderDecoder implements Compressor {

	@Override
	public void Compress(String[] input_names, String[] output_names) {
		// Final variables
		final long START_TIME = System.currentTimeMillis(); // Starting time of the compression.
		final String INPUT_FILE = input_names[0];

		// Variables
		HashMap<Short, String> dictionary = new HashMap<>();
		HashMap<Short, Integer> frequencyTable = new HashMap<>();
		PriorityQueue<HuffmanTree> priorityQueue = new PriorityQueue<>();

		// Open the file and create absolute path to that file.
		final File outputFile = new File(output_names[0]);

		// -- Read bytes and calculate frequencies.
		int byteCount = getFrequencies(INPUT_FILE,frequencyTable); // Create frequency hashmap.
		System.out.printf("~[%4dms] Finished creating frequency table.\n",System.currentTimeMillis() - START_TIME);


		// -- Huffman Tree
		System.out.println("~ Constructing nodes.");
		NodeConstructor nodeConstructor = new NodeConstructor(frequencyTable, priorityQueue); // Create node constructor
		System.out.printf("~[%4dms] Finished constructing nodes.\n",System.currentTimeMillis() - START_TIME);
		System.out.println("~ Constructing a Huffman Tree.");
		HuffmanTree huffmanTree = nodeConstructor.constructTree(); // Construct a Huffman Tree
		System.out.printf("~[%4dms] Finished constructing tree.\n",System.currentTimeMillis() - START_TIME);
		Dictionary dictionaryConstructor = new Dictionary();
		System.out.println("~ Constructing a dictionary.");
		dictionaryConstructor.createDictionary(huffmanTree, "", dictionary); // Construct a dictionary
		System.out.printf("~[%4dms] Finished constructing dictionary.\n",System.currentTimeMillis() - START_TIME);


		// ------- Compressing the file.
		encodeFile(outputFile, INPUT_FILE, dictionary, huffmanTree , frequencyTable.size(), byteCount);
		System.out.printf("~ Done, total compression time [%d ms]!\n",System.currentTimeMillis() - START_TIME);


	}


	@Override
	public void Decompress(String[] input_names, String[] output_names) {
		final long START_TIME = System.currentTimeMillis();
		HashMap<String, Short> dictionary = new HashMap<>();


		// Open the file and create absolute path to that file.
		final File inputFile = new File(input_names[0]);
		final File outputFile = new File(output_names[0]);

		try {
			// Variables and initial reading
			BitInputStream bitInputStream = new BitInputStream(new FileInputStream(inputFile));

			System.out.println("~ Reading decompression file.");
			int amountOfBytes = bitInputStream.readBits(32); // Amount of bytes in the original file.
			int nodesInTree = bitInputStream.readBits(32); // Amount of leaves in original Huffman Tree.
			int numberOfInternalNodes = nodesInTree - 1;


			// ---- Dictionary Reconstruction
			System.out.println("~ Constructing dictionary.");
			Dictionary dictionaryConstructor = new Dictionary();
			HuffmanTree huffmanTree = dictionaryConstructor.decodeDictionary(bitInputStream, nodesInTree, numberOfInternalNodes);
			System.out.printf("~[%4dms] Recunstructed Huffman Tree.\n",System.currentTimeMillis() - START_TIME);
			dictionaryConstructor.recreateDictionary(huffmanTree, "", dictionary); // Construct a dictionary
			System.out.printf("~[%4dms] Finished constructing dictionary.\n",System.currentTimeMillis() - START_TIME);


			// -------- Decompression
			System.out.println("~ Decompressing file.");
			decompressFile(bitInputStream, outputFile, dictionary, amountOfBytes);
			System.out.printf("~[%4dms] Finished decompressing!.\n",System.currentTimeMillis() - START_TIME);


		} catch (IOException e) {
			System.out.println("Cannot read the file to decode.");
		}





	}

	private void decompressFile(BitInputStream bitInputStream, File outputFile, HashMap<String, Short> dictionary, int amountOfBytes) {
		try {
			// Variables
			final int EOF = -1, // Reached the end of file
					BITS_TO_WRITE = 16; // Amount of bits to write each time.

			String buffer = ""; // This is a buffer string used to search in the Hashmap for such encoding.
			BitOutputStream bitOutputStream = new BitOutputStream(new FileOutputStream(outputFile));

			// Read as long there's remaining bytes to read.
			while(amountOfBytes > 0) {

				int bit = bitInputStream.readBits(1); // Read single bit.

				if(bit == EOF) // Check if we reached the end of the file.
					break;

				buffer += bit; // Concat that bit to the buffer string.

				if(dictionary.containsKey(buffer)) { // Search for the encoding in the dictionary
					// If exists, write the original bytes to the file and reset the buffer.
					bitOutputStream.writeBits(BITS_TO_WRITE, dictionary.get(buffer));
					buffer = ""; // Reset buffer.
					amountOfBytes -= 2; // Bytes left to read.
				}

			}

			// Close the file.
			bitOutputStream.close();
			bitInputStream.close();
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Not used
	@Override
	public byte[] CompressWithArray(String[] input_names, String[] output_names) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] DecompressWithArray(String[] input_names, String[] output_names) {
		// TODO Auto-generated method stub
		return null;
	}


	// ~-~-~-~-~-~-~-~-~- Custom Functions ~-~-~-~-~-~-~-~-~-
	// This function adds the two bytes inside a hashmap that counts its frequency.
	private int getFrequencies(String filePath, HashMap<Short, Integer> frequencyTable) {
		// Variables
		final int EOF = -1; // End of file.
		int byteCount = 0; // Amount of bytes in the file.

		System.out.printf("~ Creating frequency table for \"%s\".\n",filePath);

		try {
			FileInputStream input = new FileInputStream(filePath); // Reader

			while(true){
				// Refresh variables for next read.
				short firstByte, secondByte, twoBytes;

				firstByte = (short) input.read(); // Read first byte.

				if(firstByte == EOF) // End of file check.
					break;

				secondByte = (short) input.read(); // Read second byte.

				twoBytes = (short) ((firstByte << 8) | secondByte); // Allocate two bytes inside the variable.

				byteCount = secondByte == EOF ? byteCount + 1: byteCount + 2; // Count bytes.

				frequencyTable.put(twoBytes, frequencyTable.getOrDefault(twoBytes,0) + 1); // Insert bytes to hashtable.
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteCount;
	}

	// ------- Compressing Function
	private void encodeFile(File outputFile, String filePath, HashMap<Short, String> dictionary, HuffmanTree huffmanTree, int leavesInTree, int bytesCount) {
		final int EOF = -1; // End of file.
		System.out.println("~ Encoding (compressing) file.");

		try {
			// Readers and writers.
			OutputStream outputStream = new FileOutputStream(outputFile);
			BitOutputStream bitOutputStream = new BitOutputStream(outputStream);

			// Header Routine
			bitOutputStream.writeBits(32,bytesCount); // Amount of bytes in original file.
			bitOutputStream.writeBits(32, leavesInTree); // Amount of leaves in the Huffman Tree.
			scanPostorder(huffmanTree, bitOutputStream); // Scan and write a postorder traversal of the Huffman Tree.

			try {
				FileInputStream input = new FileInputStream(filePath);

				while(true){
					// Refresh variables for next read.
					short firstByte, secondByte, twoBytes;

					firstByte = (short) input.read(); // Read first byte.

					if(firstByte == EOF) // End of file check.
						break;

					secondByte = (short) input.read(); // Read second byte.

					twoBytes = (short) ((firstByte << 8) | secondByte); // Allocate two bytes inside the variable.

					// Get the encoding bits.
					String bytesToWrite = dictionary.get(twoBytes);

					// Write the encoded bits to the file.
					// Format: writeBits(amount of bits, bits to write)
					bitOutputStream.writeBits(bytesToWrite.length(), Integer.parseInt(bytesToWrite,2));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Flush all bits, and close the file.
			bitOutputStream.flush();
			bitOutputStream.close();

		} catch (FileNotFoundException e) {
			System.out.printf("Couldn't resolve output file \"%s\".\n", outputFile.getPath());
		}

	}

	// This function traverses a tree in postorder.
	private void scanPostorder(HuffmanTree huffmanTree, BitOutputStream bitOutputStream) {
		if(huffmanTree.isLeaf()){
			bitOutputStream.writeBits(1,1); // Indicate being a leaf.
			bitOutputStream.writeBits(16, huffmanTree.bytes); // Write the original bytes
			return;
		}

		// Recursion left.
		scanPostorder(huffmanTree.leftChild, bitOutputStream);

		// Recursion right.
		scanPostorder(huffmanTree.rightChild, bitOutputStream);

		bitOutputStream.writeBits(1,0); // Indicate the end of a rank.
	}

	// ~-~-~-~-~-~-~-~-~- DEBUG Functions ~-~-~-~-~-~-~-~-~-
	// Function that prints a hashmap.
	private void printHashMap(HashMap<Short, Integer> map) {
		for (Map.Entry<Short, Integer> element : map.entrySet()) {
			System.out.printf("%s | %d\n",Integer.toBinaryString((element.getKey()) & 0xFFFF), element.getValue());
		}
	}

	private void printDictionary(HashMap<Short, String> map) {
		for (Map.Entry<Short, String> element : map.entrySet()) {
			System.out.printf("%s | %s\n",Integer.toBinaryString((element.getKey()) & 0xFFFF), element.getValue());
		}
	}

	private void printDecodedDictionary(HashMap<String, Short> map) {
		for (Map.Entry<String, Short> element : map.entrySet()) {
			System.out.printf("%s | %s\n", element.getKey(), Integer.toBinaryString(element.getValue()));
		}
	}

	// Function that prints a binary tree.
	private void printTree(HuffmanTree huffmanTree) {
		if(huffmanTree == null)
			return;
		if(huffmanTree.isLeaf()){
			System.out.printf("%s | %d\n",Integer.toBinaryString(huffmanTree.bytes), huffmanTree.value);
		} else {
			printTree(huffmanTree.rightChild);
			printTree(huffmanTree.leftChild);
		}
	}

}
