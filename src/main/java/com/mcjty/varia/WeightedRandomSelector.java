package com.mcjty.varia;

import java.util.*;

/**
 * A class that can return random items based on rarity.
 */
public class WeightedRandomSelector<K,E> {
    private final Random random;
    private boolean dirty = true;

    // A map associating every key with the chance that items of this key should be selected.
    private final Map<K,Float> keys = new HashMap<K, Float>();
    private float minChance = Float.MAX_VALUE;    // Used for calculation distribution with bonus.
    private float maxChance = Float.MIN_VALUE;

    private final Distribution<K> defaultDistribution = new Distribution<K>();

    // All items for every key.
    private final Map<K,List<E>> items = new HashMap<K, List<E>>();

    public WeightedRandomSelector(Random random) {
        this.random = random;
    }

    /**
     * Add a new rarity key. All items associated with this key should
     * have 'chance' chance of being selected.
     */
    public void addRarity(K key, float chance) {
        keys.put(key, chance);
        items.put(key, new ArrayList<E>());
        if (chance < minChance) {
            minChance = chance;
        }
        if (chance > maxChance) {
            maxChance = chance;
        }
        dirty = true;
    }

    /**
     * Add a new item.
     */
    public void addItem(K key, E item) {
        items.get(key).add(item);
        dirty = true;
    }

    private void distribute() {
        if (dirty) {
            dirty = false;
            setupDistribution(defaultDistribution, 0.0f);
        }
    }

    private void setupDistribution(Distribution<K> distribution, float bonus) {
        float add = bonus * (maxChance - minChance);
        distribution.reset();
        for (Map.Entry<K, Float> entry : keys.entrySet()) {
            K key = entry.getKey();
            int length = items.get(key).size();
            float chance = (entry.getValue() + add) * length;
            distribution.addKey(key, chance);
        }
    }

    /**
     * Create a new distribution. If the bonus is equal to 0.0f then this distribution
     * will be equal to the default one. With a bonus equal to 1.1f you will basically
     * make the chance of the rarest elements equal to half the chance of the most common
     * elements. Very large values will make the rarest elements almost as common as
     * the most common elements.
     */
    public Distribution<K> createDistribution(float bonus) {
        Distribution<K> distribution = new Distribution<K>();
        setupDistribution(distribution, bonus);
        return distribution;
    }

    /**
     * Return a random element given a distribution.
     */
    public E select(Distribution<K> distribution) {
        distribute();
        float r = random.nextFloat() * distribution.getTotalChance() - 0.0001f;  // Subtract with small value to ensure we actually get there.
        K key = null;
        for (Map.Entry<K, Float> entry : distribution.getKeysChance().entrySet()) {
            r -= entry.getValue();
            if (r <= 0.0f && !items.get(entry.getKey()).isEmpty()) {
                key = entry.getKey();
                break;
            }
        }
        List<E> list = items.get(key);
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Return a random element.
     */
    public E select() {
        return select(defaultDistribution);
    }

    public static class Distribution<K> {
        // A map associating every key with the chance that this key in total must be selected.
        private final Map<K,Float> keysChance = new HashMap<K, Float>();
        private float totalChance = 0.0f;

        public Map<K, Float> getKeysChance() {
            return keysChance;
        }

        public float getTotalChance() {
            return totalChance;
        }

        public void reset() {
            keysChance.clear();
            totalChance = 0.0f;
        }

        public void addKey(K key, float chance) {
            keysChance.put(key, chance);
            totalChance += chance;
        }
    }
}
