package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.Stack;

public class StackImpl<Command> implements Stack {
    int size;
    Node head;

    public StackImpl() {
        this.size = 0;
    }

    @Override
    public void push(Object element) {
        Node node = new Node((Command) element);
        node.next = head;
        head = node;
        size++;
    }

    @Override
    public Command pop() {
        if(head == null) {
            return null;
        }
        Command temp = head.command;
        head = head.next;
        size--;
        return temp;
    }

    @Override
    public Command peek() {
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
        Command command;
        Node next;

        Node(Command command) {
            this.command = command;
        }

    }

}
