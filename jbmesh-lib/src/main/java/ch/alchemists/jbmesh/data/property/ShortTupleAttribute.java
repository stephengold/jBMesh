// Copyright (c) 2020-2021 Rolf Müri
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package ch.alchemists.jbmesh.data.property;

import ch.alchemists.jbmesh.data.BMeshData;
import ch.alchemists.jbmesh.data.BMeshAttribute;
import ch.alchemists.jbmesh.data.Element;

public class ShortTupleAttribute<E extends Element> extends BMeshAttribute<E, short[]> {
    public ShortTupleAttribute(String name, int components) {
        super(name, components);
    }


    public short getComponent(E element, int component) {
        return data[indexOf(element, component)];
    }

    public void setComponent(E element, int component, short value) {
        data[indexOf(element, component)] = value;
    }

    public void setComponent(E element, int component, int value) {
        data[indexOf(element, component)] = (short) value;
    }


    public void setValues(E element, short... values) {
        if(values.length != numComponents)
            throw new IllegalArgumentException("Number of values does not match number of components.");

        int index = indexOf(element);
        for(int i = 0; i < numComponents; ++i)
            data[index++] = values[i];
    }

    public void setValues(E element, int... values) {
        if(values.length != numComponents)
            throw new IllegalArgumentException("Number of values does not match number of components.");

        int index = indexOf(element);
        for(int i = 0; i < numComponents; ++i)
            data[index++] = (short) values[i];
    }


    @Override
    public boolean equals(E a, E b) {
        int indexA = indexOf(a);
        int indexB = indexOf(b);

        for(int i = 0; i < numComponents; ++i) {
            if(data[indexA++] != data[indexB++])
                return false;
        }

        return true;
    }


    @Override
    protected short[] alloc(int size) {
        return new short[size];
    }


    public static <E extends Element> ShortTupleAttribute<E> get(String name, BMeshData<E> meshData) {
        return (ShortTupleAttribute<E>) getAttribute(name, meshData, short[].class);
    }

    public static <E extends Element> ShortTupleAttribute<E> getOrCreate(String name, int components, BMeshData<E> meshData) {
        ShortTupleAttribute<E> attribute = get(name, meshData);

        if(attribute == null) {
            attribute = new ShortTupleAttribute<>(name, components);
            meshData.addAttribute(attribute);
        }
        else if(attribute.numComponents != components)
            throw new IllegalStateException("Attribute with same name but different number of components already exists.");

        return attribute;
    }
}