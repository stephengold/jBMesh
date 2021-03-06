// Copyright (c) 2020-2021 Rolf Müri
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package ch.alchemists.jbmesh.operator.normalgen;

import com.jme3.math.Vector3f;

public class NewellNormal {
    // Newell's Method that also works for concave polygons: https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal
    public static void addToNormal(Vector3f nStore, Vector3f last, Vector3f current) {
        nStore.x += (last.y - current.y) * (last.z + current.z);
        nStore.y += (last.z - current.z) * (last.x + current.x);
        nStore.z += (last.x - current.x) * (last.y + current.y);
    }
}
