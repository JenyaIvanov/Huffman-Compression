package compressor;

// Imports
import includes.BitInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

public class Dictionary {
    // Class private fields
    final private Stack<HuffmanTree> nodesStack;

    // Default constructor
    public Dictionary(){
        // Initialize a new node stack.
        this.nodesStack = new Stack<>();
    }

    // This function creates the encoding dictionary using a recursive algorithm to reach all leaves.
    public void createDictionary(HuffmanTree huffmanTree, String path, HashMap<Short, String> dictionary){

        if(huffmanTree.isLeaf()){
            // If a leaf is reached, add it to the dictionary.
            dictionary.put(huffmanTree.bytes, path);
            return;
        }

        // Create path to node with a recursive algorithm. (Appends 0/1 to the path, until a leaf is reached)
        createDictionary(huffmanTree.leftChild,path+"0", dictionary);

        createDictionary(huffmanTree.rightChild,path+"1", dictionary);
    }

    // This function decodes the post order scan and returns a Hoffman Tree.
    public HuffmanTree decodeDictionary(BitInputStream bitInputStream, int numberOfNodes, int numberOfInternalNodes){
        try {
            while(numberOfNodes > 0 || numberOfInternalNodes > 0){
                // Read one bit.
                int bit = bitInputStream.readBits(1);


                // The bit we read tells us if we need to reconstruct a new node or merge the last nodes.
                if(bit == 1){ // Construct leaf.
                    short bytes = (short) bitInputStream.readBits(16); // Originla bytes
                    HuffmanTree leaf = new HuffmanTree(bytes); // Construct a leaf with those bytes.
                    nodesStack.push(leaf);
                    numberOfNodes--; // Decrease amount of leafs we need to reconstruct.
                } else {  // We read '0'.
                    // Pop two nodes out of the stack and merge them together, push that new node inside.
                    HuffmanTree rightHuffmanTree = nodesStack.pop();
                    HuffmanTree leftHuffmanTree = nodesStack.pop();
                    nodesStack.push(new HuffmanTree(0, leftHuffmanTree, rightHuffmanTree));
                    numberOfInternalNodes--; // Decrease amount of internal nodes.
                }
            }

            // Return the Huffman Tree.
            return nodesStack.pop();

        } catch (IOException e) {
            System.out.println("Couldn't read the file to decode.");
        }

        return null;
    }

    // This function recreates the encoding dictionary using a recursive algorithm to reach all leaves.
    public void recreateDictionary(HuffmanTree huffmanTree, String path, HashMap<String, Short> dictionary) {
        if(huffmanTree.isLeaf()){
            // If a leaf is reached, add it to the dictionary.
            dictionary.put(path, huffmanTree.bytes);
            return;
        }

        // Create path to node with a recursive algorithm. (Appends 0/1 to the path, until a leaf is reached)
        recreateDictionary(huffmanTree.leftChild,path+"0", dictionary);

        recreateDictionary(huffmanTree.rightChild,path+"1", dictionary);

    }
}
