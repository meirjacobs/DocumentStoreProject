package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.stage2.Document;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
    LinkedList[] array;
    int entries;

    public HashTableImpl() {
        array = new LinkedList[5];
        entries = 0;
    }

    @Override
    public Value get(Key k) {
        if(k == null) {
            throw new IllegalArgumentException("ERROR: Cannot have null key");
        }
        int i = k.hashCode();
        int i1 = hashFunction((i));
        LinkedListElement tempi = new LinkedListElement(null, null);
        if (array[i1] != null) {
            LinkedList list = array[i1];
            tempi = list.head;
            while (tempi != null) {
                if (tempi.key.equals(k)) {
                    return (Value) tempi.value;
                }
                tempi = tempi.next;
            }
        }
        return null;
    }

    @Override
    public Value put(Key k, Value v) {
        if(k == null) {throw new IllegalArgumentException("ERROR: Cannot have null key");}
        if(v == null) {return (Value) removeElement(k, v);}
        if(get(k) != null && get(k).equals(v)) {return (Value) (Integer) v.hashCode();}
        int i = k.hashCode();
        int i1 = hashFunction((i));
        LinkedListElement<Key, Value> element = new LinkedListElement<>(k, v);
        if (array[i1] == null) {
            LinkedList list = new LinkedList();
            list.addElement(element);
            array[i1] = list;
            entries++;
            if(((double) entries / array.length) > 0.75) { arrayDouble();}
            return null;
        }
        else {
            LinkedList list = array[i1];
            Object edited = list.editElement(element);
            if(edited == null) {
                list.addElement(element);// Adding element to array
                entries++;
                if (((double) entries / array.length) > 0.75) { arrayDouble();}
            }
            if(edited instanceof Integer) { return (Value) (Integer) edited;}
            else { return (Value) edited;}
        }
    }

    private Integer removeElement(Key k, Value v) {
        int i = k.hashCode();
        int i1 = hashFunction((i));
        LinkedList list = array[i1];
        if(list == null || list.head == null) {
            return 0;
        }
        LinkedListElement element = new LinkedListElement(k, v);
        int match = list.removeElement(element);
        array[i1] = list;
        return match;
    }

    private int hashFunction(int i) {
        return (i & 0x7fffffff) % array.length;
    }

    private LinkedList[] arrayDouble() {
        LinkedList[] temp = this.array;
        this.array = new LinkedList[array.length * 2];
        int newCounter = 0;
        for(LinkedList list : temp) {
            if(list == null || list.head == null) {
                continue;
            }
            LinkedListElement current = new LinkedListElement(null, null);
            current = list.head;
            while(current != null) {
                int i1 = getArraySlot(current);
                if(this.array[i1] != null) {
                    LinkedList newList = this.array[i1];
                    LinkedListElement newer = new LinkedListElement(current.key, current.value);
                    newList.addElement(newer);
                    newCounter++;
                }
                else {
                    LinkedList newList = new LinkedList();
                    LinkedListElement newer = new LinkedListElement(current.key, current.value);
                    newList.addElement(newer);
                    newCounter++;
                    this.array[i1] = newList;
                }
                current = current.next;
            }
        }
        this.entries = newCounter;
        return this.array;
    }

    private int getArraySlot(LinkedListElement element) {
        int i = element.key.hashCode();
        return hashFunction(i);
    }

}

class LinkedListElement<K, V> {
    K key;
    V value;
    LinkedListElement next;
    LinkedListElement previous;

    protected LinkedListElement(K key, V value) {
        this.key = key;
        this.value = value;
        this.next = null;
    }

    protected LinkedListElement getNext() {
        return next;
    }

    protected V getValue() {
        return this.value;
    }

    protected K getKey() {
        return this.key;
    }

    private void setNext(LinkedListElement element) {
        next = element;
    }

}


class LinkedList {
    LinkedListElement head;
    int counter;

    LinkedList() {
        head = null;
        counter = 0;
    }

    protected boolean addElement(LinkedListElement newElement) {
        if(newElement == null) {
            return false;
        }

        if(this.head == null) {
            this.head = newElement;
            this.head.next = null;
            this.head.previous = null;
        }

        else {
            LinkedListElement temp = this.head;
            this.head = newElement;
            this.head.next = temp;
            this.head.previous = null;
            temp.previous = this.head;
        }
        counter++;
        return true;

    }

    protected int removeElement(LinkedListElement element) {
        LinkedListElement current = this.head;
        int hcOfRemoved = 0;
        while(current != null) {
            if(current.key.equals(element.key)) {
                hcOfRemoved = current.value.hashCode();
                if(current.previous == null && current.next == null) {
                    this.head = null;
                    current = null;
                    break;
                }
                else if(current.previous == null) {
                    this.head = current.next;
                    current = null;
                    break;
                }
                else {
                    current.previous.next = current.next;
                    current = null;
                    break;
                }
            }
            else {
                current = current.next;
            }
        }
        return hcOfRemoved;
    }

    protected <T> Object editElement(LinkedListElement element) {
        LinkedListElement current = this.head;
        Object hcOfEdited = null;
        while(current != null) {
            if (current.key.equals(element.key)) {
                Object obj = (Object) current.value;
                if (obj instanceof Document) {
                    Document document = (Document) obj;
                    hcOfEdited = document.getDocumentTextHashCode();
                }
                else {
                    hcOfEdited = obj;
                }
                current.value = element.value;
                break;
            }
            else {
                current = current.next;
            }
        }
        return hcOfEdited;
    }

    protected LinkedListElement getElement(LinkedListElement element) {
        LinkedListElement current = this.head;
        while(current != null) {
            if(current.key.equals(element.key)) {
                if(current.previous == null && current.next == null) {
                    this.head = null;
                    return current;
                }
                else if(current.previous == null) {
                    this.head = current.next;
                    return current;
                }
                else {
                    current.previous.next = current.next;
                    return current;
                }
            }
            else {
                current = current.next;
            }
        }
        return null;
    }

    protected LinkedListElement removeElementForDoubling(LinkedListElement element) {
        LinkedListElement current = this.head;
        LinkedListElement match =  new LinkedListElement(null, null);
        while(current != null) {
            if(current.key.equals(element.key)) {
                match = current;
                if(current.previous == null && current.next == null) {
                    this.head = null;
                    current = null;
                    break;
                }
                else if(current.previous == null) {
                    this.head = current.next;
                    current = null;
                    break;
                }
                else {
                    current.previous.next = current.next;
                    current = null;
                    break;
                }
            }
            else {
                current = current.next;
            }
        }
        return match;
    }
}

