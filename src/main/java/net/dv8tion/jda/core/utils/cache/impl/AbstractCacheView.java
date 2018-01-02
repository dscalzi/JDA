/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.utils.cache.impl;

import gnu.trove.map.TLongObjectMap;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.MiscUtil;
import net.dv8tion.jda.core.utils.cache.CacheView;
import org.apache.commons.collections4.iterators.ArrayIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ThreadSafe
public abstract class AbstractCacheView<T> implements CacheView<T>
{
    protected final TLongObjectMap<T> elements = MiscUtil.newLongMap();
    protected final Function<T, String> nameMapper;

    protected AbstractCacheView(@Nullable Function<T, String> nameMapper)
    {
        this.nameMapper = nameMapper;
    }

    public void clear()
    {
        elements.clear();
    }

    @Nonnull
    public TLongObjectMap<T> getMap()
    {
        return elements;
    }

    @Nonnull
    @Override
    public List<T> asList()
    {
        ArrayList<T> list = new ArrayList<>(elements.size());
        elements.forEachValue(list::add);
        return Collections.unmodifiableList(list);
    }

    @Nonnull
    @Override
    public Set<T> asSet()
    {
        HashSet<T> set = new HashSet<>(elements.size());
        elements.forEachValue(set::add);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public long size()
    {
        return elements.size();
    }

    @Override
    public boolean isEmpty()
    {
        return elements.isEmpty();
    }

    @Nonnull
    @Override
    public List<T> getElementsByName(String name, boolean ignoreCase)
    {
        Checks.notEmpty(name, "Name");
        if (elements.isEmpty())
            return Collections.emptyList();
        if (nameMapper == null) // no getName method available
            throw new UnsupportedOperationException("The contained elements are not assigned with names.");

        List<T> list = new LinkedList<>();
        for (T elem : elements.valueCollection())
        {
            String elementName = nameMapper.apply(elem);
            if (elementName != null)
            {
                if (ignoreCase)
                {
                    if (elementName.equalsIgnoreCase(name))
                        list.add(elem);
                }
                else
                {
                    if (elementName.equals(name))
                        list.add(elem);
                }
            }
        }

        return list;
    }

    @Override
    public Spliterator<T> spliterator()
    {
        return Spliterators.spliterator(elements.values(), Spliterator.IMMUTABLE);
    }

    @Nonnull
    @Override
    public Stream<T> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    @Nonnull
    @Override
    public Stream<T> parallelStream()
    {
        return StreamSupport.stream(spliterator(), true);
    }

    @Nonnull
    @Override
    public Iterator<T> iterator()
    {
        return new ArrayIterator<>(elements.values());
    }
}