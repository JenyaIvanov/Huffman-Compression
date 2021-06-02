package compressor;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class NodeConstructor {
    // -- Private fields
    private final HashMap<Short, Integer> frequencyTable;
    private final PriorityQueue<HuffmanTree> queue;

    // -- Value constructor
    public NodeConstructor(HashMap<Short, Integer> frequencyTable, PriorityQueue<HuffmanTree> queue){
        this.queue = queue;
        this.frequencyTable = frequencyTable;
    }

    // -------- Functions
    // This function creates a Huffman Encoding Tree.
    public HuffmanTree constructTree(){

        // Convert all hashes to nodes inside the priority queue.
        for (Map.Entry<Short, Integer> frequencyHash : frequencyTable.entrySet()) {
            queue.add(new HuffmanTree(frequencyHash));
        }

        while(queue.size() > 1){

            // Pull two lowest value nodes and combine them.
            HuffmanTree rightHuffmanTree = queue.poll();
            HuffmanTree leftHuffmanTree = queue.poll();


            if(leftHuffmanTree != null && rightHuffmanTree != null){
                final int nodesSum = leftHuffmanTree.value + leftHuffmanTree.value; // Frequencies sum.
                queue.add(new HuffmanTree(nodesSum, leftHuffmanTree, rightHuffmanTree)); // Add this new merged node to the stack.
            }
        }

        return queue.poll();
    }



}
