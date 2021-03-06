// Copyright (c) 2020-2021 Rolf Müri
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package ch.alchemists.jbmesh.conversion;

import ch.alchemists.jbmesh.data.BMeshAttribute;
import ch.alchemists.jbmesh.data.property.ObjectAttribute;
import ch.alchemists.jbmesh.structure.BMesh;
import ch.alchemists.jbmesh.structure.Edge;
import ch.alchemists.jbmesh.structure.Loop;
import ch.alchemists.jbmesh.structure.Vertex;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import java.util.List;

public class TriangleExport extends Export<Loop> {
    private final ObjectAttribute<Loop, Vertex> attrLoopVertex;
    private final TriangleIndices triangleIndices;


    public TriangleExport(BMesh bmesh) {
        super(bmesh, Mesh.Mode.Triangles);

        useVertexAttribute(VertexBuffer.Type.Position, BMeshAttribute.Position);

        attrLoopVertex = ObjectAttribute.getOrCreate(BMeshAttribute.VertexMap, bmesh.loops(), Vertex[].class, Vertex[]::new);
        attrLoopVertex.setComparable(false);

        triangleIndices = new TriangleIndices(bmesh, attrLoopVertex);
    }


    public static Mesh apply(BMesh bmesh) {
        TriangleExport export = new TriangleExport(bmesh);
        return export.update();
    }


    @Override
    protected void applyIndexBuffer(Mesh mesh) {
        triangleIndices.triangulateFaces();
        triangleIndices.applyIndexBuffer(mesh);
    }


    @Override
    protected void getVertexNeighborhood(Vertex vertex, List<Loop> dest) {
        for(Edge edge : vertex.edges()) {
            for(Loop loop : edge.loops()) {
                if(loop.vertex == vertex)
                    dest.add(loop);
            }
        }
    }


    @Override
    protected void setVertexReference(Vertex contactPoint, Loop element, Vertex ref) {
        attrLoopVertex.set(element, ref);
    }

    @Override
    protected Vertex getVertexReference(Vertex contactPoint, Loop element) {
        return attrLoopVertex.get(element);
    }
}
