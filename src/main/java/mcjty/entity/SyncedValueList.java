package mcjty.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * A synchronized list of values.
 */
public abstract class SyncedValueList<T> implements List<T>, SyncedObject {
    private final List<T> value = new ArrayList<T>();
    private int serverVersion = 0;
    private int clientVersion = -1;

    public SyncedValueList() {
    }

    public int getClientVersion() {
        return clientVersion;
    }

    // @@@ TEMPORARY!
    public int getServerVersion() {
        return serverVersion;
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        serverVersion = tagCompound.getInteger("version");
//        clientVersion = tagCompound.getInteger("cVersion");
        NBTTagList list = tagCompound.getTagList("list", Constants.NBT.TAG_COMPOUND);
        value.clear();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            value.add(readElementFromNBT(list.getCompoundTagAt(i)));
        }
    }

    public void readFromNBT(NBTTagCompound tagCompound, String tagName) {
        NBTTagCompound compound = tagCompound.getCompoundTag(tagName);
        if (compound != null) {
            readFromNBT(compound);
        }
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("version", serverVersion);
//        tagCompound.setInteger("cVersion", clientVersion);
        NBTTagList list = new NBTTagList();
        for (T element : value) {
            list.appendTag(writeElementToNBT(element));
        }
        tagCompound.setTag("list", list);
    }

    public void writeToNBT(NBTTagCompound tagCompound, String tagName) {
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        tagCompound.setTag(tagName, compound);
    }

    public abstract T readElementFromNBT(NBTTagCompound tagCompound);
    public abstract NBTTagCompound writeElementToNBT(T element);

    @Override
    public void setInvalid() {
        value.clear();
        serverVersion = 0;
        clientVersion = -1;
    }

    @Override
    public boolean isClientValueUptodate() {
        return serverVersion == clientVersion;
    }

    @Override
    public void updateClientValue() {
        clientVersion = serverVersion;
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
    public T[] toArray() {
        return (T[]) value.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return value.toArray(a);
    }

    @Override
    public boolean add(Object o) {
        serverVersion++;
        return value.add((T)o);
    }

    @Override
    public boolean remove(Object o) {
        serverVersion++;
        return value.remove(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        return value.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        serverVersion++;
        return value.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        serverVersion++;
        return value.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection c) {
        serverVersion++;
        return value.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        serverVersion++;
        return value.retainAll(c);
    }

    @Override
    public void clear() {
        serverVersion++;
        value.clear();
    }

    @Override
    public T get(int index) {
        return value.get(index);
    }

    @Override
    public T set(int index, T element) {
        serverVersion++;
        return value.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        serverVersion++;
        value.add(index, element);
    }

    @Override
    public T remove(int index) {
        serverVersion++;
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
