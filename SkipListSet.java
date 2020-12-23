import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.SortedSet;

public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {
    // Variable Declarations
    HashMap<Integer, ArrayList<SkipListSetItem>> skip = null;
    Random r = new Random();
    int size = 0;
    int count = 0;
    int maxHeight;
    
    // SkipListSetItem class that holds the generic value and height of node
    private class SkipListSetItem implements Comparable<SkipListSetItem>
	{
		T value;
		int height = 0;
		SkipListSetItem(T value)
		{
            this.value = value;
            if(size() != 0)
			    height = r.nextInt(size());
		}

        @Override
        public int compareTo(SkipListSet<T>.SkipListSetItem o) {
            return this.value.compareTo(o.value);
        }
    }

    // SkipListSetIterator class that traverses throught the lowest level of the skip list
    private class SkipListSetIterator implements Iterator<T> 
    {
        ArrayList<SkipListSetItem> it;
        SkipListSetItem current;
        int index = 0;
        SkipListSetIterator()
        {
            it = skip.get(0);
            current = it.get(index);
        }
        @Override
        public boolean hasNext() {
            return index != size-1;
        }

        @Override
        public T next() {
            return it.get(++index).value;
        }
        public void remove()
        {
            it.remove(current);
        }
    }

    // SkipListSet constructor that takes no parameters and returns an empty skip list
    SkipListSet()
    {
        skip = new HashMap<>();
        skip.put(0,new ArrayList<>());
    }

    // SkipListSet constructor that takes in a collection as a parameter and returns a skiplist set containing the collection
    SkipListSet(Collection <? extends T> c)
    {

        skip = new HashMap<>();
        skip.put(0,new ArrayList<>());
        this.addAll(c);
    }

    // toString for print
    public String toString()
	{
        for(int i = size(); i >= 0; i--){
            if(!skip.containsKey(i))
                continue;
            for(SkipListSetItem t : skip.get(i))
            {
                System.out.print(t.value + "( " + t.height+ " )"+ "->");
            }
            System.out.println("\n");
        }
        return "";
	}
    
    // Rebalances the heights for the nodes and makes the appropriate reconnections
    void reBalance()
    {
        // creates an array list and fills it with the original values in the skiplist
        ArrayList<T> n = new ArrayList<>();
        for(SkipListSetItem t : skip.get(0))
            n.add(t.value);
        
        // clears the skip list so it can be overwritten, then initialize the skiplist values
        skip.clear();
        skip = new HashMap<>();
        skip.put(0, new ArrayList<>());
        
        // adds all the elements back into the skiplist with re randomized heights based on
        // the total size of the skiplist
        for(T t : n)
        {
            size--;
            this.add(t);
        }

    }

    // returns number of elements in skip list
    @Override
    public int size() {
        return size;
    }

    // returns if the skip list has no elements
    @Override
    public boolean isEmpty() {
        return skip.get(0).isEmpty();
    }

    // traverses throught the skiplist to see if it contains a certain object o,
    // returns whether it was found or not
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        int index = 0;
        
        // starts at the height of the head node and moves right until there are no more
        // elements or until the current node is greater than what we are trying to find
        for(Integer i = maxHeight; i >= 0; i--)
        {
            if(skip.get(i) == null)
                continue;
            for(int j = index; j < size() ; j++)
            {
                int comp = skip.get(i).get(j).value.compareTo((T)o);
                if(comp == 0)
                    return true;
                
                // if current element is greater than what we are trying to find,
                // move back one space and move to the index where the previous element is
                // in the row below it ( back one then down one )
                else if(comp > 0)
                {
                    if(i == 0 || j == 0)
                        return false;
                    index = skip.get(i-1).indexOf(skip.get(i).get(j-1));
                    break;
                }
                else{ 
                    continue;
                }
            }
        }
        return false;
    }

    // returns an iterator through all the elements in the skiplist
    @Override
    public Iterator<T> iterator() {
        return new SkipListSetIterator();
    }

    // returns the skiplist as an Object array
    @Override
    public Object[] toArray() {
        return skip.get(0).toArray();
    }

    //returns the skiplist as an array of T type
    @Override
    public <T> T[] toArray(T[] a) {
        return skip.get(0).toArray(a);
    }

    // adds the element to the skip list and returns whether it was successful
    @Override
    public boolean add(T e) {
        int h;
        size++;
        SkipListSetItem add = new SkipListSetItem(e);
        
        // if the list is empty,insert head and number of heights = height of head
        if(skip.get(0).isEmpty())
            h = add.height;
        
        // if the skiplist is not empty and the new item height is less than the head height,
        // number of heights where this element is inserted is the new item's height
        else if(add.height < skip.get(0).get(0).height)
            h = add.height;

        // else, height = head height
        else
            h = skip.get(0).get(0).height;

        // for the heights less than or equal to h, 
        // add the element to that arraylist for that height
        for(int i = h; i >= 0; i--)
        {
            if(!skip.containsKey(i))
                skip.put(i,new ArrayList<>());
            skip.get(i).add(add);
        }
        return true;
    }

    // finds element that needs to be removed and removes it,
    // returns whether it was successful
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        // decrements size accordingly
        size--;

        // height for traversal is head height
        Integer maxHeight = skip.get(0).get(0).height;

        //  index for traversal that changes at every height
        int index = 0;
        for(Integer i = maxHeight; i >= 0; i--)
        {
            if(skip.get(i) == null)
                continue;

            // for all elements at current height, if item is found delete and return
            // if current item is greater than item to be deleted, move back one and down one
            // else, move to next item
            for(int j = index; j < size() ; j++)
            {
                int comp = skip.get(i).get(j).value.compareTo((T)o);
                if(comp == 0)
                {
                    skip.get(i).remove(skip.get(i).get(j));
                    return true;
                }
                else if(comp > 0)
                {
                    if(i == 0 || j == 0)
                        return false;
                    index = skip.get(i-1).indexOf(skip.get(i).get(j-1));
                    break;
                }
                else{ 
                    continue;
                }
            }
        }
        return false;
    }

    // checks if the skiplist contains all the elements in the collection
    // runs contains(), c.size() times and returns false if contains() returns false
    @Override
    public boolean containsAll(Collection<?> c) {
        maxHeight = skip.get(0).get(0).height;
        for(Object o : c)
        {
            if(!contains(o))
                return false;
        }
        return true;
    }

    // adds all the elements in the collection to the skiplist
    // runs add(), c.size() times sorts the different heights' array lists
    @Override
    public boolean addAll(Collection<? extends T> c) {
       // System.out.println("ADDING");
        for(T o : c)
            this.add(o);
        //System.out.println("ADDED");
        for(Integer i = skip.get(0).get(0).height; i >= 0; i--)
        {
            skip.get(i).sort(null);
        }
        return true;
    }

    // for all the items in the skip list, if an item in the skiplist is not in the collection,
    // it is removed and the function returns whether it was successful
    @Override
    public boolean retainAll(Collection<?> c) {
        for(SkipListSetItem t : skip.get(0))
        {
            if(!c.contains(t.value))
                remove(t);
        }
        return c.size() == size;
    }

    // runs remove(), c.size() times and removes all the items in the skiplist that
    // are in the collection
    @Override
    public boolean removeAll(Collection<?> c) {
        for(Object o : c)
            remove(o);
        return true;
    }

    // clears the skiplist
    @Override
    public void clear() {
        skip.get(0).clear();
        skip = null;
    }

    // Functions not changed or throw exceptions 
    @Override
    public Comparator<? super T> comparator() {
        return null;
    }
    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) throws UnsupportedOperationException {
        return null;
    }
    @Override
    public SortedSet<T> headSet(T toElement) throws UnsupportedOperationException{
        return null;
    }
    @Override
    public SortedSet<T> tailSet(T fromElement) throws UnsupportedOperationException{
        return null;
    }

    // returns the skiplist first element's value
    @Override
    public T first() {
        return skip.get(0).get(0).value;
    }

    // returns the skiplist last element's value
    @Override
    public T last() {
        return skip.get(0).get(size()-1).value;
    }
    
}