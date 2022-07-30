package gwel.game.graphics;

import com.badlogic.gdx.math.Vector2;

public class BufferedRenderer extends Renderer {
    private float[] coloredVertices;
    private short[] indices;
    private short numVerts;
    private short vSize, iSize;

    public BufferedRenderer() {
        super();
    }

    public void setBufferSize(int numVerts, short numIndices) {
        coloredVertices = new float[numVerts * (2+4)];
        indices = new short[numIndices];
        //coloredVertices = new float[20000];
        //indices = new short[10000];

        System.out.println("vertices: " + coloredVertices.length);
        System.out.println("indices: " + indices.length);
        reset();
    }

    public void reset() {
        vSize = 0;
        iSize = 0;
        numVerts = 0;
    }

    public float[] getVertices() { return coloredVertices; }

    public short[] getIndices() { return indices; }


    @Override
    public void triangles(float[] vertices, short[] indices) {
        for (short idx : indices) {
            this.indices[iSize++] = (short) (idx + numVerts);
        }
        Vector2 vertex = new Vector2();
        for (int i = 0; i < vertices.length; i += 2) {
            vertex.set(vertices[i], vertices[i+1]);
            getTransform().applyTo(vertex);
            coloredVertices[vSize++] = vertex.x;
            coloredVertices[vSize++] = vertex.y;
            coloredVertices[vSize++] = color.r;
            coloredVertices[vSize++] = color.g;
            coloredVertices[vSize++] = color.b;
            coloredVertices[vSize++] = color.a;
            numVerts++;
        }
    }

    @Override
    public void circle(float x, float y, float r) {

    }
}
