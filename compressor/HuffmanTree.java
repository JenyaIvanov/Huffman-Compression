package compressor;

import java.util.Map;

public class HuffmanTree implements Comparable<HuffmanTree>{
    // Public node values
    public HuffmanTree leftChild;
    public HuffmanTree rightChild;
    public int value; // Frequency
    public short bytes; // 2Bytes

    // ------------ Node constructors
    public HuffmanTree(Map.Entry<Short, Integer> entry){
        leftChild = null;
        rightChild = null;
        value = entry.getValue();
        bytes = entry.getKey();
    }

    public HuffmanTree(short bytes){
        this.leftChild = null;
        this.rightChild = null;
        this.value = 0;
        this.bytes = bytes;
    }

    public HuffmanTree(int value, HuffmanTree leftChild, HuffmanTree rightChild){
        this.value = value;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    // This function tests a given node status, of being a leaf.
    public boolean isLeaf(){
        return (this.leftChild == null) && (this.rightChild == null);
    }


    @Override
    // This function compares two nodes.
    public int compareTo(HuffmanTree other) {
        return this.value - other.value;
    }
}
