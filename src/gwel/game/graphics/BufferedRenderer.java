package gwel.game.graphics;

import com.badlogic.gdx.math.Vector2;

public class BufferedRenderer extends Renderer {
    private float[] coloredVertices;
    private short[] indices;
    private short numTris;
    private short vSize, iSize;

    public BufferedRenderer() {
    }

    public void setBufferSize(short numTris) {
        coloredVertices = new float[numTris * (2+4)];
        vSize = 0;
        indices = new short[numTris * 3];
        iSize = 0;
        this.numTris = 0;
    }

    public float[] getVertices() { return coloredVertices; }

    public short[] getIndices() { return indices; }


    @Override
    public void triangles(float[] vertices, short[] indices) {
        int idx;
        short nt = 0;
        Vector2 point = new Vector2();
		for (int i = 0; i < indices.length; ) {
            idx = indices[i]<<1;
            point.set(vertices[idx], vertices[idx+1]);
            getTransform().applyTo(point);
            vertices[vSize++] = point.x;
            vertices[vSize++] = point.y;
            vertices[vSize++] = color.r;
            vertices[vSize++] = color.g;
            vertices[vSize++] = color.b;
            vertices[vSize++] = color.a;
            this.indices[iSize++] = (short) (numTris + indices[i++]);

            idx = indices[i]<<1;
            point.set(vertices[idx], vertices[idx+1]);
            getTransform().applyTo(point);
            vertices[vSize++] = point.x;
            vertices[vSize++] = point.y;
            vertices[vSize++] = color.r;
            vertices[vSize++] = color.g;
            vertices[vSize++] = color.b;
            vertices[vSize++] = color.a;
            this.indices[iSize++] = (short) (numTris + indices[i++]);

            idx = indices[i]<<1;
            point.set(vertices[idx], vertices[idx+1]);
            getTransform().applyTo(point);
            vertices[vSize++] = point.x;
            vertices[vSize++] = point.y;
            vertices[vSize++] = color.r;
            vertices[vSize++] = color.g;
            vertices[vSize++] = color.b;
            vertices[vSize++] = color.a;
            this.indices[iSize++] = (short) (numTris + indices[i++]);
            nt++;
        }
        numTris += nt;
    }

    @Override
    public void circle(float x, float y, float r) {

    }
}
