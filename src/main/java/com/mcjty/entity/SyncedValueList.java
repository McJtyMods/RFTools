package com.mcjty.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * A synchronized list of values.
 */
public abstract class SyncedValueList<T> implements List {
    private final List<T> value = new ArrayList<T>();
    private final List<T> clientValue = new ArrayList<T>();
    private boolean valueDirty = false;

    public SyncedValueList() {
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        valueDirty = tagCompound.getBoolean("dirty");
        NBTTagList list = tagCompound.getTagList("list", Constants.NBT.TAG_COMPOUND);
        value.clear();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            value.add(readElementFromNBT(list.getCompoundTagAt(i)));
        }
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setBoolean("dirty", valueDirty);
        NBTTagList list = new NBTTagList();
        for (T element : value) {
            list.appendTag(writeElementToNBT(element));
        }
    }

    public abstract T readElementFromNBT(NBTTagCompound tagCompound);
    public abstract NBTTagCompound writeElementToNBT(T element);

    public void setInvalid() {
        value.clear();
        clientValue.clear();
        valueDirty = false;
    }

    public boolean isClientValueUptodate() {
        return !valueDirty;
    }

    public void updateClientValue() {
        clientValue.clear();
        clientValue.addAll(value);
        valueDirty = false;
    }

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return value.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return value.iterator();
    }

    @Override
    public Object[] toArray() {
        return value.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return value.toArray(a);
    }

    @Override
    public boolean add(Object o) {
        valueDirty = true;
        return value.add((T)o);
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection c) {
        return value.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        valueDirty = true;
        return value.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        valueDirty = true;
        return value.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection c) {
        valueDirty = true;
        return value.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        valueDirty = true;
        return value.retainAll(c);
    }

    @Override
    public void clear() {
        valueDirty = true;
        value.clear();
    }

    @Override
    public Object get(int index) {
        return value.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        valueDirty = true;
        return value.set(index, (T) element);
    }

    @Override
    public void add(int index, Object element) {
        valueDirty = true;
        value.add(index, (T) element);
    }

    @Override
    public Object remove(int index) {
        valueDirty = true;
        return value.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return value.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return value.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return value.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return value.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return value.subList(fromIndex, toIndex);
    }
}
