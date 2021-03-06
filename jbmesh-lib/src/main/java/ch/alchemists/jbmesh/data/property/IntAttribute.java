// Copyright (c) 2020-2021 Rolf Müri
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package ch.alchemists.jbmesh.data.property;

import ch.alchemists.jbmesh.data.BMeshData;
import ch.alchemists.jbmesh.data.BMeshAttribute;
import ch.alchemists.jbmesh.data.Element;

public class IntAttribute<E extends Element> extends BMeshAttribute<E, int[]> {
    public IntAttribute(String name) {
        super(name);
    }


    public int get(E element) {
        return data[element.getIndex()];
    }

    public void set(E element, int value) {
        data[element.getIndex()] = value;
    }


    @Override
    public boolean equals(E a, E b) {
        return data[a.getIndex()] == data[b.getIndex()];
    }


    @Override
    protected int[] alloc(int size) {
        return new int[size];
    }

    public static <E extends Element> IntAttribute<E> get(String name, BMeshData<E> meshData) {
        return (IntAttribute<E>) getAttribute(name, meshData, int[].class);
    }
}
