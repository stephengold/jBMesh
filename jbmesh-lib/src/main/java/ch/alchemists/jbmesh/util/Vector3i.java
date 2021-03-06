// Copyright (c) 2020-2021 Rolf Müri
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package ch.alchemists.jbmesh.util;

public final class Vector3i {
    public int x;
    public int y;
    public int z;


    public Vector3i() {
        x = 0;
        y = 0;
        z = 0;
    }


    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public Vector3i(Vector3i copy) {
        set(copy);
    }


    public Vector3i set(Vector3i vect) {
        return set(vect.x, vect.y, vect.z);
    }

    public Vector3i set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }


    public Vector3i addLocal(Vector3i other) {
        addLocal(other.x, other.y, other.z);
        return this;
    }

    public Vector3i addLocal(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }


    public Vector3i subtractLocal(Vector3i other) {
        subtractLocal(other.x, other.y, other.z);
        return this;
    }

    public Vector3i subtractLocal(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }


    public Vector3i multLocal(Vector3i other) {
        multLocal(other.x, other.y, other.z);
        return this;
    }

    public Vector3i multLocal(int x, int y, int z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vector3i multLocal(int scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }


    public Vector3i divideLocal(Vector3i other) {
        divideLocal(other.x, other.y, other.z);
        return this;
    }

    public Vector3i divideLocal(int x, int y, int z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public Vector3i divideLocal(int scalar) {
        x /= scalar;
        y /= scalar;
        z /= scalar;
        return this;
    }


    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }


    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Vector3i))
            return false;

        if(this == o)
            return true;

        Vector3i comp = (Vector3i) o;
        if(x != comp.x) return false;
        if(y != comp.y) return false;
        if(z != comp.z) return false;
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 37;
        hash += 37 * hash + x;
        hash += 37 * hash + y;
        hash += 37 * hash + z;
        return hash;
    }
}
