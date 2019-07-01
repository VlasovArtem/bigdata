package org.avlasov.sparkexample.simple;

import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SimpleExampleTest {

    private SimpleExample simpleExample = new SimpleExample();

    @Test
    public void groupNumbers() {
        Map<String, Iterable<Integer>> stringIterableMap = simpleExample.groupNumbersByEvenOdd(Arrays.asList("1", "2", "3", "4", "5"));

        assertThat(stringIterableMap.get(SimpleExample.EVEN), IsCollectionContaining.hasItems(2, 4));
        assertThat(stringIterableMap.get(SimpleExample.ODD), IsCollectionContaining.hasItems(1, 3, 5));
    }

    @Test
    public void stringsSortedByLength() {
        List<String> strings = simpleExample.stringsSortedByLength(Arrays.asList("first", "second", "big", "small"));

        assertThat(strings, IsCollectionWithSize.hasSize(4));
        assertEquals("second", strings.get(0));
    }

    @Test
    public void topStringSortedByLength() {
        List<String> strings = simpleExample.top(Arrays.asList("first", "second", "big", "small"));

        assertThat(strings, IsCollectionWithSize.hasSize(1));
        assertEquals("small", strings.get(0));
    }
}