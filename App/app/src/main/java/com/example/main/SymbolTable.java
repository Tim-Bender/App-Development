package com.example.main;

import java.util.TreeMap;

public class SymbolTable<K,Value> {

   private TreeMap<K,Value> container;

   public class Value<T> {
       public T value;

       Value(T v){
           this.value = v;
       }

   }
    SymbolTable(){
        this.container = new TreeMap<>();
    }

    public void put(K key, Value value){
        this.container.put(key,value);
    }
    public void remove(K key){
       container.remove(key);
    }

    public boolean contains(K key){
        return container.containsKey(key);
    }

    public boolean isEmpty(){
        return this.container.isEmpty();
    }






}
