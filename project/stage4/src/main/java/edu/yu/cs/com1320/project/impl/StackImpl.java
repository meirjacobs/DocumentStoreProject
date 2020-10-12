package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Undoable;

public class StackImpl<T> implements Stack {
    int size;
    Node head;

    public StackImpl() {
        this.size = 0;
    }

    @Override
    public void push(Object element) {
        Node node = new Node((Undoable) element);
        node.next = head;
        head = node;
        size++;
    }

    @Override
    public Undoable pop() {
        if(head == null) {
            return null;
        }
        Undoable temp = head.command;
        head = head.next;
        size--;
        return temp;
    }

    @Override
    public Undoable peek() {
        if(head == null) {
            return null;
        }
        return head.command;
    }

    @Override
    public int size() {
        return size;
    }

    private class Node {
        Undoable command;
        Node next;

        Node(Undoable command) {
            this.command = command;
        }

    }

}
