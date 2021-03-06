// Copyright (c) 2020-2021 Rolf Müri
//
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package ch.alchemists.jbmesh.tools;

import ch.alchemists.jbmesh.conversion.BMeshJmeExport;
import ch.alchemists.jbmesh.conversion.DebugMeshExport;
import ch.alchemists.jbmesh.conversion.DirectImport;
import ch.alchemists.jbmesh.operator.MeshOps;
import ch.alchemists.jbmesh.operator.normalgen.NormalGenerator;
import ch.alchemists.jbmesh.structure.BMesh;
import ch.alchemists.jbmesh.util.ColorUtil;
import ch.alchemists.jbmesh.util.Gizmo;
import ch.alchemists.jbmesh.util.Profiler;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

public class Main extends SimpleApplication {
    private final Node node = new Node();
    private Spatial spatial = null;


    private BMesh makeMesh() {
        //Mesh in = new Torus(128, 128, 2.0f, 4.0f);
        //Mesh in = new Torus(32, 24, 1.2f, 2.5f);
        //Mesh in = new Sphere(32, 32, 5.0f);
        //Mesh in = new Sphere(12, 12, 1.0f);
        Mesh in = new Box(1f, 1f, 1f);
        //Mesh in = new Quad(1.0f, 1.0f);
        //Mesh in = loadModel();

        BMesh bmesh;
        try(Profiler p = Profiler.start("Import")) {
            //BMesh bmesh = Import.convertGridMapped(in); // TODO: Wrong results!
            //bmesh = Import.convertExactMapped(in);
            bmesh = DirectImport.importTriangles(in);
        }

        try(Profiler p = Profiler.start("Processing")) {
            MeshOps.mergePlanarFaces(bmesh);
            //TestMesh.spikes(bmesh);
            TestMesh.hollow(bmesh);
            TestMesh.subdiv(bmesh);
            TestMesh.subtract(bmesh);
        }

        /*try(Profiler p0 = Profiler.start("Marching Cubes")) {
            bmesh = TestMesh.marchingCubes(bmesh);
            //bmesh = MarchingCube.build(bmesh, TestMesh.dfunc(), 0.05f, false);
        }*/

        return bmesh;
    }


    private void addMesh() {
        try(Profiler p0 = Profiler.start("addMesh")) {
            if(spatial != null)
                spatial.removeFromParent();

            BMesh bmesh = makeMesh();
            try(Profiler p = Profiler.start("Compacting")) {
                bmesh.compactData();
            }

            NormalGenerator normGen = new NormalGenerator(bmesh, 40);
            try(Profiler p = Profiler.start("Normal Gen")) {
                normGen.apply();
            }

            //spatial = createDebugMesh(bmesh);
            spatial = createMesh(bmesh);
            node.attachChild(spatial);

            //node.attachChild(DebugNormals.loopNormals(assetManager, bmesh, 0.1f));
            //node.attachChild(DebugNormals.faceNormals(assetManager, bmesh, 0.33f));
        }
    }

    @Override
    public void simpleInitApp() {
        //for(int i=0; i<30; ++i)
            addMesh();

        rootNode.attachChild(node);
        rootNode.attachChild(new Gizmo(assetManager, null, 1.0f));

        rootNode.addLight(new AmbientLight(ColorRGBA.White.mult(0.04f)));
        rootNode.addLight(new DirectionalLight(new Vector3f(-0.7f, -1, -1.5f).normalizeLocal(), ColorRGBA.White.mult(1.0f)));
        rootNode.addLight(new DirectionalLight(new Vector3f(0.7f, -1, 1.5f).normalizeLocal(), ColorRGBA.White.mult(0.07f)));

        flyCam.setMoveSpeed(2);
        viewPort.setBackgroundColor(ColorUtil.hsv(0.75f, 0.35f, 0.02f));
        cam.setFrustumPerspective(60, (float)cam.getWidth()/cam.getHeight(), 0.01f, 100f);
        initCamera(25, 1.2f);
    }


    private Mesh loadModel() {
        assetManager.registerLocator("assets/", FileLocator.class);
        Node model = (Node) assetManager.loadModel("Models/Jaime.j3o");
        return ((Geometry) model.getChild(0)).getMesh();
    }


    private Geometry createMesh(BMesh bmesh) {
        ColorRGBA diffuse  = ColorUtil.hsv(0.16f, 0.25f, 0.7f);
        ColorRGBA specular = ColorUtil.hsv(0.10f, 0.4f, 1.0f);
        /*ColorProperty<Vertex> propVertexColor = new ColorProperty<>(BMeshProperty.Vertex.COLOR);
        bmesh.vertices().addProperty(propVertexColor);

        for(Vertex v : bmesh.vertices()) {
            //color = hsb((float)Math.random(), 0.8f, 0.8f);
            propVertexColor.set(v, color.r, color.g, color.b, color.a);
        }*/

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        //mat.getAdditionalRenderState().setWireframe(true);
        //mat.setBoolean("UseVertexColor", true);
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.White);
        mat.setColor("Diffuse", diffuse);
        mat.setColor("Specular", specular);
        mat.setFloat("Shininess", 120f);

        Mesh mesh;
        try(Profiler p = Profiler.start("Export")) {
            mesh = BMeshJmeExport.exportTriangles(bmesh);
        }

        Geometry geom = new Geometry("Geom", mesh);
        geom.setMaterial(mat);
        return geom;
    }

    
    private Geometry createDebugMesh(BMesh bmesh) {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseVertexColor", true);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        DebugMeshExport export = new DebugMeshExport();
        export.apply(bmesh);
        Geometry geom = new Geometry("Geom", export.createMesh());
        geom.setMaterial(mat);

        return geom;
    }



    // Calculate camera distance where BoundingBox is fully visible
    private void initCamera(float camElevation, float distanceFactor) {
        BoundingBox bbox = (BoundingBox) rootNode.getWorldBound();
        if(bbox == null)
            return;

        float sign = Math.signum(camElevation);
        camElevation *= FastMath.DEG_TO_RAD;

        float sin = FastMath.sin(camElevation);
        float cos = FastMath.cos(camElevation);

        float horizontal = bbox.getXExtent();
        float vertical   = cos*bbox.getYExtent() + Math.abs(sin*bbox.getZExtent());

        float distX = horizontal * cam.getFrustumNear() / cam.getFrustumRight();
        float distY = vertical * cam.getFrustumNear() / cam.getFrustumTop();

        float dist = Math.max(distX, distY);
        dist *= distanceFactor;

        Vector3f frontY = bbox.getCenter(new Vector3f()).addLocal(0, bbox.getYExtent()*sign, 0);
        Vector3f frontZ = bbox.getCenter(new Vector3f()).addLocal(0, 0, bbox.getZExtent());
        Vector3f front = FastMath.interpolateLinear(camElevation/FastMath.HALF_PI*sign, frontZ, frontY);

        Vector3f p = front.clone();
        p.y += dist * sin;
        p.z += dist * cos;

        cam.setLocation(p);
        cam.lookAt(front, Vector3f.UNIT_Y);
    }
    

    @Override
    public void simpleUpdate(float tpf) {
        final float speed = 0.3f;
        //node.rotate(0, speed * tpf, 0);
        //addMesh();
    }


    public static void main(String[] args) {
        /*PropertyAccessTest pa = new PropertyAccessTest(bmesh);
        pa.shouldWork();
        pa.shouldFailAtRuntime();*/

        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        settings.setFrameRate(200);
        settings.setSamples(8);
        settings.setGammaCorrection(true);

        Main app = new Main();
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }
}
