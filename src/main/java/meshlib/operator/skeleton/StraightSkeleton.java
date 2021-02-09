package meshlib.operator.skeleton;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import java.util.*;
import meshlib.data.BMeshProperty;
import meshlib.data.property.Vec3Property;
import meshlib.operator.FaceOps;
import meshlib.structure.BMesh;
import meshlib.structure.Face;
import meshlib.structure.Vertex;
import meshlib.util.PlanarCoordinateSystem;

public class StraightSkeleton {
    private final BMesh bmesh;
    private final FaceOps faceOps;
    private final Vec3Property<Vertex> propPosition;

    private float offsetDistance = 0.0f; // Absolute value
    private float distanceSign = 1.0f;

    private PlanarCoordinateSystem coordSys;
    private final ArrayList<SkeletonNode> initialNodes = new ArrayList<>();
    private final SkeletonContext ctx = new SkeletonContext();


    public StraightSkeleton(BMesh bmesh) {
        this.bmesh = bmesh;
        faceOps = new FaceOps(bmesh);
        propPosition = Vec3Property.get(BMeshProperty.Vertex.POSITION, bmesh.vertices());
    }


    /**
     * @param distance Absolute distance in units by which the edges should be moved.<br>
     *                 Positive: Grow face. Negative: Shrink face.
     */
    public void setDistance(float distance) {
        distanceSign = Math.signum(distance);
        offsetDistance = Math.abs(distance);
    }


    public void apply(Face face) {
        System.out.println("===== Apply StraightSkeleton, distance: " + (offsetDistance*distanceSign) + " =====");
        List<Vertex> vertices = face.getVertices();
        assert vertices.size() >= 3;

        ctx.reset(offsetDistance, distanceSign);

        Vector3f n = faceOps.normal(face);
        coordSys = new PlanarCoordinateSystem(propPosition.get(vertices.get(0)), propPosition.get(vertices.get(1)), n);

        project(vertices);
        initBisectors();
        initEdgeEvents();
        initSplitEvents();

        loop();

        /*float time = 0;
        while(time < offsetDistance) {
            System.out.println("time = " + time);
            time = loop(time);
        }*/
    }


    private float loop(float time) {
        //ctx.printEvents();

        SkeletonEvent event = ctx.pollQueue();
        if(event == null) {
            scale((offsetDistance - time) * distanceSign);
            return offsetDistance; // End loop
        }

        scale((event.time - time) * distanceSign);
        ctx.time = event.time;

        System.out.println("{{ handle: " + event);
        event.handle(ctx);
        System.out.println("}} handled");

        return event.time;
    }


    private void loop() {
        ctx.time = 0;
        //while(ctx.time < offsetDistance) {
        while(true) {
            System.out.println("time = " + ctx.time);

            SkeletonEvent event = ctx.pollQueue();
            if(event == null) {
                scale((offsetDistance - ctx.time) * distanceSign);
                return;
            }

            scale((event.time - ctx.time) * distanceSign);
            ctx.time = event.time;

            System.out.println("{{ handle: " + event);
            event.handle(ctx);
            System.out.println("}} handled");
        }
    }


    private void project(List<Vertex> vertices) {
        initialNodes.clear();
        initialNodes.ensureCapacity(vertices.size());

        //movingNodes.clear();
        ctx.movingNodes.ensureCapacity(vertices.size());

        Vector3f vertexPos = new Vector3f();
        Vector3f last = propPosition.get(vertices.get(vertices.size()-1));

        for(Vertex vertex : vertices) {
            propPosition.get(vertex, vertexPos);

            float distSquared = last.distanceSquared(vertexPos);
            last.set(vertexPos);
            if(distSquared < SkeletonContext.EPSILON_SQUARED) {
                System.out.println("Ignoring duplicate vertex at " + vertexPos);
                // TODO: This messes with MovingNode IDs. They don't match text of original points in visualization.
                continue;
            }

            // Create initial node
            SkeletonNode initialNode = new SkeletonNode();
            coordSys.project(vertexPos, initialNode.p);
            initialNodes.add(initialNode);

            // Create moving node
            MovingNode movingNode = ctx.createMovingNode();
            movingNode.skelNode = new SkeletonNode();
            movingNode.skelNode.p.set(initialNode.p);
            initialNode.addEdge(movingNode.skelNode);
        }
    }


    private void initBisectors() {
        final int numVertices = ctx.movingNodes.size();
        for(MovingNode node : ctx.movingNodes) {
            node.edgeLengthChange = 0;
        }

        MovingNode prev = ctx.movingNodes.get(numVertices-1);
        MovingNode current = ctx.movingNodes.get(0);

        for(int i=0; i<numVertices; ++i) {
            int nextIndex = (i+1) % numVertices;
            MovingNode next = ctx.movingNodes.get(nextIndex);

            // Link nodes
            current.next = next;
            current.prev = prev;

            // TODO: check if valid?
            current.calcBisector(distanceSign);

            // Next iteration
            prev = current;
            current = next;
        }
    }


    private void scale(float dist) {
        if(dist == 0) {
            System.out.println("=> scaling = 0, skip");
            return;
        }

        System.out.println("=> scaling " + ctx.movingNodes.size() + " nodes by " + dist);
        Vector2f dir = new Vector2f();

        for(MovingNode node : ctx.movingNodes) {
            dir.set(node.bisector).multLocal(dist);
            node.skelNode.p.addLocal(dir);

            if(isInvalid(node.skelNode.p)) {
                System.out.println("invalid after scale: bisector=" + node.bisector + ", dir=" + dir);
            }
        }
    }


    private void initEdgeEvents() {
        for(MovingNode current : ctx.movingNodes) {
            ctx.tryQueueEdgeEvent(current.prev, current);
        }
    }

    private void initSplitEvents() {
        for(MovingNode current : ctx.movingNodes) {
            if(!current.isReflex())
                continue;

            MovingNode start = current.next;
            MovingNode end = current.prev.prev;

            MovingNode op0 = start;
            while(op0 != end) {
                ctx.tryQueueSplitEvent(current, op0, op0.next);
                op0 = op0.next;
            }
        }
    }


    private boolean isInvalid(Vector2f v) {
        return Float.isNaN(v.x) || Float.isInfinite(v.x);
    }


    public SkeletonVisualization getVisualization() {
        return new SkeletonVisualization(coordSys, initialNodes, ctx);
    }
}