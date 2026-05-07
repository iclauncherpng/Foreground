package mindustry.world;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.struct.*;
import mindustry.gen.*;
import java.util.*;

/** A tile container. Optimized for memory and cache friendliness. */
public class Tiles implements Iterable<Tile> {
    public final int width, height;
    public final int size;

    
    private final Tile[] array;

    
    
    private final IntMap<Puddle> puddles = new IntMap<>();
    private final IntMap<Fire> fires = new IntMap<>();

    private @Nullable long[] tmpFloorState, tmpBlockState;

    public Tiles(int width, int height) {
        this.width = width;
        this.height = height;
        this.size = width * height;
        this.array = new Tile[size];
    }

    
    public long getTmpFloorState(int pos) {
        return tmpFloorState == null ? 0 : tmpFloorState[pos];
    }

    public void setTmpFloorState(int pos, long value) {
        if (tmpFloorState == null) tmpFloorState = new long[size];
        tmpFloorState[pos] = value;
    }

    public long getTmpBlockState(int pos) {
        return tmpBlockState == null ? 0 : tmpBlockState[pos];
    }

    public void setTmpBlockState(int pos, long value) {
        if (tmpBlockState == null) tmpBlockState = new long[size];
        tmpBlockState[pos] = value;
    }

    
    public Puddle getPuddle(int pos) {
        return puddles.get(pos);
    }

    public void setPuddle(int pos, Puddle p) {
        if (p == null) puddles.remove(pos);
        else puddles.put(pos, p);
    }

    public @Nullable Fire getFire(int pos) {
        return fires.get(pos);
    }

    public void setFire(int pos, Fire f) {
        if (f == null) fires.remove(pos);
        else fires.put(pos, f);
    }

    /** Быстрый проход по координатам без создания объектов */
    public void each(Intc2 cons) {
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                cons.get(x, y);
            }
        }
    }

    /** Заполнение. Важно: Tile должен быть максимально легким. */
    public void fill() {
        for (int i = 0; i < size; i++) {
            array[i] = new Tile(i % width, i / width);
        }
    }

    public void set(int x, int y, Tile tile) {
        array[y * width + x] = tile;
    }

    public void seti(int i, Tile tile) {
        array[i] = tile;
    }

    public boolean in(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    @Nullable
    public Tile get(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return array[y * width + x];
    }

    public Tile getn(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException(x + ", " + y + " out of bounds");
        }
        return array[y * width + x];
    }

    public Tile getc(int x, int y) {
        return array[Mathf.clamp(y, 0, height - 1) * width + Mathf.clamp(x, 0, width - 1)];
    }

    public Tile geti(int idx) {
        return array[idx];
    }

    public @Nullable Tile getp(int pos) {
        int x = Point2.x(pos);
        int y = Point2.y(pos);
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return array[y * width + x];
    }

    /** Самый быстрый способ итерации по тайлам */
    public void eachTile(Cons<Tile> cons) {
        for (int i = 0; i < size; i++) {
            cons.get(array[i]);
        }
    }

    @Override
    public Iterator<Tile> iterator() {
        return new TileIterator();
    }

    private class TileIterator implements Iterator<Tile> {
        int index = 0;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public Tile next() {
            return array[index++];
        }
    }
}