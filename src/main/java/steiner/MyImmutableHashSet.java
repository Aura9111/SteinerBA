package steiner;

import java.util.HashSet;
import java.util.Iterator;

public class MyImmutableHashSet<T> implements Iterable<T>{

    private HashSet<T> set;

    public MyImmutableHashSet(){
        set=new HashSet<T>();
    }

    public MyImmutableHashSet(HashSet<T> set){
        this.set=set;
    }

	public int getSize() {
		return set.size();
	}

	public MyImmutableHashSet<T> add(T add) {
        HashSet<T> tmp= new HashSet<T>();
        for (T t:set){
            tmp.add(t);
        }
        tmp.add(add);
        return new MyImmutableHashSet<T>(tmp);
    }
    
    public MyImmutableHashSet<T> remove(T rem) {
        HashSet<T> tmp= new HashSet<T>();
        for (T t:set){
            tmp.add(t);
        }
        tmp.remove(rem);
        return new MyImmutableHashSet<T>(tmp);
	}

	public HashSet<T> getHashSet() {
		return set;
    }
    

    public boolean contains(T find){
		 return set.contains(find);
	}

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

}
