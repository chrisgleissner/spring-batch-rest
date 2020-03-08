package com.github.chrisgleissner.springbatchrest.util.core;

import org.springframework.batch.item.ItemWriter;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.synchronizedList;

public class CacheItemWriter<T> implements ItemWriter<T> {

    private List<T> items = synchronizedList(new LinkedList<>());

    @Override
    public void write(List<? extends T> items) {
        this.items.addAll(items);
    }

    public List<T> getItems() {
        return items;
    }
}
