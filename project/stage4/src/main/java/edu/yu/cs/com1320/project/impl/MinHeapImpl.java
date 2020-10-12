package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable> extends MinHeap<E> {

    public MinHeapImpl() {
        this.elements = (E[]) new Comparable[10];
        this.count = 0;
        this.elementsToArrayIndex = new HashMap<E, Integer>();
    }

    @Override
    public void reHeapify(E element) {
        if(elementsToArrayIndex.get(element) == null) {
            return;
        }
        int position = elementsToArrayIndex.get(element);
        if(position * 2 > this.count && (position * 2) + 1 > this.count) { // element is a leaf
            upHeap(position);
        }
        else { // element is internal
            downHeap(position);
            upHeap(position);
        }
    }

    @Override
    protected int getArrayIndex(E element) {
        Integer index = elementsToArrayIndex.get(element);
        return index == null ? -1 : index;
    }

    @Override
    protected void doubleArraySize() {
        E[] newArray = (E[]) new Comparable[elements.length * 2];
        for(int i = 0; i < elements.length; i++) {
            newArray[i] = elements[i];
        }
        elements = newArray;
    }

    @Override
    protected  void swap(int i, int j)
    {
        if(i < 1 || j < 1 || this.elements[i] == null || this.elements[j] == null) {
            return;
        }

        E temp = this.elements[i];
        this.elements[i] = this.elements[j];
        this.elements[j] = temp;

        //--------------------------------------------------------------------------------------------------------------
        //added by me (updating map to reflect the swap)
        Integer tempInt = elementsToArrayIndex.get(this.elements[i]);
        elementsToArrayIndex.put(this.elements[i], elementsToArrayIndex.get(this.elements[j]));
        elementsToArrayIndex.put(this.elements[j], tempInt);
        //--------------------------------------------------------------------------------------------------------------
    }

    @Override
    public void insert(E x)
    {
        // double size of array if necessary
        if (this.count >= this.elements.length - 1)
        {
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        this.elements[++this.count] = x;

        //--------------------------------------------------------------------------------------------------------------
        //I'M ADDING STUFF
        elementsToArrayIndex.put(x, this.count);
        //--------------------------------------------------------------------------------------------------------------

        //percolate it up to maintain heap order property
        this.upHeap(this.count);
    }

    @Override
    public E removeMin()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC

        //--------------------------------------------------------------------------------------------------------------
        elementsToArrayIndex.remove(min);
        //--------------------------------------------------------------------------------------------------------------

        return min;
    }
}
