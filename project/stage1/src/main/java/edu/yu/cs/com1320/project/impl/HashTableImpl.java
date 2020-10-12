package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.stage1.Document;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
    LinkedList[] array = new LinkedList[5];

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
        if(k == null) { throw new IllegalArgumentException("ERROR: Cannot have null key");}
        if(v == null) {return (Value) removeElement(k, v);}
        if(get(k) != null && get(k).equals(v)) {return (Value) (Integer) v.hashCode();}
        int i = k.hashCode();
        int i1 = hashFunction((i));
        LinkedListElement<Key, Value> element = new LinkedListElement<>(k, v);
        if (array[i1] == null) {
            LinkedList list = new LinkedList();
            list.addElement(element);
            array[i1] = list;
            return (Value) (Integer) 0;
        }
        else {
            LinkedList list = array[i1];
            Object edited = list.editElement(element);
            if(edited instanceof Integer) {
                if ((Integer) edited != 0) {return (Value) (Integer) edited;}
                else {
                    list.addElement(element); // Adding element to array
                    return null;
                }
            }
            else {return (Value) edited;}
        }
    }

    private Integer removeElement(Key k, Value v) {
        int i = k.hashCode();
        int i1 = hashFunction((i));
        LinkedList list = array[i1];
        if(list.head == null) {
            return 0;
        }
        LinkedListElement element = new LinkedListElement(k, v);
        int match = list.removeElement(element);
        array[i1] = list;
        return match;
    }

    private int hashFunction(int i) {
        return (i & 0x7fffffff) % 5;
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

        private LinkedListElement getNext() {
            return next;
        }

        private void setNext(LinkedListElement element) {
            next = element;
        }

    }


    class LinkedList {
        LinkedListElement head;
        int counter;

        protected void addElement(LinkedListElement newElement) {
            if(newElement == null) {
                return;
            }

            if(this.head == null) {
                this.head = newElement;
                this.head.next = null;
                this.head.previous = null;
                counter++;
                return;
            }

            else {
                LinkedListElement temp = this.head;
                this.head = newElement;
                this.head.next = temp;
                this.head.previous = null;
                temp.previous = this.head;
                counter++;
            }
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
            Object hcOfEdited = 0;
            while(current != null) {
                if (current.key.equals(element.key)) {
                    Object obj = (Object) current.value;
                    if (obj instanceof Document) {
                        Document document = (Document) obj;
                        hcOfEdited = document.getDocumentTextHashCode();
                        current.value = element.value;
                        break;
                    }
                    else {
                        hcOfEdited = obj;
                        current.value = element.value;
                        break;
                    }
                }
                else {
                    current = current.next;
                }
            }
            return hcOfEdited;
        }
    }

