package peergos.server.tests;

import org.junit.jupiter.api.Test;
import peergos.shared.cbor.*;

import io.ipfs.multihash.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.*;

public class CborObjects {
    private final Random rnd = new Random();

    private byte[] random(int len) {
        byte[] res = new byte[len];
        rnd.nextBytes(res);
        return res;
    }

    @Test
    void dosCborObject() throws Throwable {
        // make a header for a byte[] that is 2^50 long
        byte[] raw = hexToBytes("5b0004000000000000");
        try {
            CborObject.fromByteArray(raw);
            fail("Should have failed!");
        } catch (RuntimeException e) {}
    }

    @Test
    void cborNull() {
        CborObject.CborNull cbor = new CborObject.CborNull();
        compatibleAndIdempotentSerialization(cbor);
    }

    @Test
    void cborString() {
        String value = "G'day mate!";
        CborObject.CborString cbor = new CborObject.CborString(value);
        compatibleAndIdempotentSerialization(cbor);
    }

    @Test
    void cborByteArray() {
        byte[] value = random(32);
        CborObject.CborByteArray cbor = new CborObject.CborByteArray(value);
        compatibleAndIdempotentSerialization(cbor);
    }

    @Test
    void cborBoolean() {
        compatibleAndIdempotentSerialization(new CborObject.CborBoolean(true));
        compatibleAndIdempotentSerialization(new CborObject.CborBoolean(false));
    }

    @Test
    void cborLongs() {
        cborLong(rnd.nextLong());
        cborLong(Long.MAX_VALUE);
        cborLong(Long.MIN_VALUE);
        cborLong(Integer.MAX_VALUE);
        cborLong(Integer.MIN_VALUE);
        cborLong(0);
        cborLong(100);
        cborLong(-100);
    }

    private void cborLong(long value) {
        CborObject.CborLong cbor = new CborObject.CborLong(value);
        compatibleAndIdempotentSerialization(cbor);
    }

    @Test
    void cborMerkleLink() {
        Multihash hash = Multihash.fromBase58("QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB");
        CborObject.CborMerkleLink link = new CborObject.CborMerkleLink(hash);
        compatibleAndIdempotentSerialization(link);
    }

    @Test
    void cborMap() {
        SortedMap<CborObject, CborObject> map = new TreeMap<>();
        map.put(new CborObject.CborString("KEY 1"), new CborObject.CborString("A value"));
        map.put(new CborObject.CborString("KEY 2"), new CborObject.CborByteArray("Another value".getBytes()));
        map.put(new CborObject.CborString("KEY 3"), new CborObject.CborNull());
        map.put(new CborObject.CborString("KEY 4"), new CborObject.CborBoolean(true));
        Multihash hash = Multihash.fromBase58("QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB");
        CborObject.CborMerkleLink link = new CborObject.CborMerkleLink(hash);
        map.put(new CborObject.CborString("Key 5"), link);
        List<CborObject> list = new ArrayList<>();
        list.add(new CborObject.CborBoolean(true));
        list.add(new CborObject.CborNull());
        list.add(new CborObject.CborLong(256));
        map.put(new CborObject.CborString("KEY 6"), new CborObject.CborList(list));
        CborObject.CborMap cborMap = new CborObject.CborMap(map);
        compatibleAndIdempotentSerialization(cborMap);
    }

    @Test
    void cborList() {
        List<CborObject> list = new ArrayList<>();
        list.add(new CborObject.CborString("A value"));
        list.add(new CborObject.CborByteArray("A value".getBytes()));
        list.add(new CborObject.CborNull());
        list.add(new CborObject.CborBoolean(true));
        CborObject.CborList cborList = new CborObject.CborList(list);
        compatibleAndIdempotentSerialization(cborList);
    }

    public void compatibleAndIdempotentSerialization(CborObject value) {
        byte[] raw = value.toByteArray();
        CborObject deserialized = CborObject.fromByteArray(raw);

        boolean equals = deserialized.equals(value);
        assertTrue(equals, "Equal objects");
        byte[] raw2 = deserialized.toByteArray();
        boolean sameRaw = Arrays.equals(raw, raw2);
        assertTrue(sameRaw, "Idempotent serialization");
    }

    public static byte[] hexToBytes(String hex) {
        byte[] res = new byte[hex.length()/2];
        for (int i=0; i < res.length; i++)
            res[i] = (byte) Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        return res;
    }
}
