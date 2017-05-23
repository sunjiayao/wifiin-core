package com.wifiin.rocksdb;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class RocksdbTest{
    @Test
    public void testPrefix() throws RocksDBException{
        RocksDB db=RocksDB.open("/Users/Running/Documents/dev/data/rocksdb");
        byte[] kv="1".getBytes();
        db.put(kv,kv);
        kv="abcd".getBytes();
        db.put(kv,kv);
        kv="a123".getBytes();
        db.put(kv,kv);
        kv="bb".getBytes();
        db.put(kv,kv);
        kv="basdf".getBytes();
        db.put(kv,kv);
        kv="b143214".getBytes();
        db.put(kv,kv);
        List<String> result=new ArrayList<String>();
        RocksIterator iterator=db.newIterator();
        String prefix="a";
        for(iterator.seek(prefix.getBytes());iterator.isValid();iterator.next()){
            String key=new String(iterator.key());
            if(!key.startsWith(prefix)){
                System.out.println("iterates not starts with prefix:"+key);
                break;
            }
            result.add(new String(iterator.key()));
        }
        System.out.println(result);
    }
}
