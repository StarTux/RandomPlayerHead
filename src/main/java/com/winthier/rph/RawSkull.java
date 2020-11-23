package com.winthier.rph;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("MemberName")
public final class RawSkull {
    public static final long HI = 0xFFFFFFFF00000000L;
    public static final long LO = 0x00000000FFFFFFFFL;
    RawDisplay display;
    RawSkullOwner SkullOwner;

    public RawSkull() { }

    public RawSkull(final Head head) {
        display = new RawDisplay();
        display.Name = head.name;
        SkullOwner = new RawSkullOwner();
        SkullOwner.Id = uuidToInt(head.uuid);
        SkullOwner.Properties = new RawProperties();
        RawTexture texture = new RawTexture();
        texture.Value = head.texture;
        texture.Signature = head.signature;
        SkullOwner.Properties.textures = Arrays.asList(texture);
    }

    static final class RawDisplay {
        String Name;
    }

    static final class RawSkullOwner {
        Object Id;
        RawProperties Properties;
    }

    static final class RawProperties {
        List<RawTexture> textures;
    }

    static final class RawTexture {
        String Value;
        String Signature;
    }

    public String getName() {
        return display.Name;
    }

    public UUID getId() {
        Object o = SkullOwner.Id;
        if (o == null) throw new IllegalStateException("Id=null");
        if (o instanceof String) {
            return UUID.fromString((String) o);
        } else if (o instanceof List) {
            @SuppressWarnings("Unchecked")
            List<Number> ls = (List<Number>) o;
            if (ls.size() == 5) ls = ls.subList(1, 5);
            if (ls.size() != 4) throw new IllegalStateException("Id.length=" + ls.size());
            return intToUuid(ls.stream().map(Number::intValue).collect(Collectors.toList()));
        } else {
            throw new IllegalStateException("Id.type=" + o.getClass().getName());
        }
    }

    public String getTexture() {
        return SkullOwner.Properties.textures.get(0).Value;
    }

    public String getSignature() {
        return SkullOwner.Properties.textures.get(0).Signature;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public Head toHead() {
        return new Head(getName(), getId(), getTexture(), getSignature());
    }

    public static RawSkull fromGiveString(String in) throws Exception {
        if (in.contains("{")) in = "{" + in.split("\\{", 2)[1];
        if (in.contains("}")) {
            int at = in.lastIndexOf("}");
            in = in.substring(0, at + 1);
        }
        System.out.println("DEBUG " + in);
        Gson gson = new Gson();
        return gson.fromJson(in, RawSkull.class);
    }

    public static UUID intToUuid(List<Integer> ls) {
        return intToUuid(ls.get(0), ls.get(1), ls.get(2), ls.get(3));
    }

    public static UUID intToUuid(int a, int b, int c, int d) {
        long hi = ((long) a << 32) | ((long) b & LO);
        long lo = ((long) c << 32) | ((long) d & LO);
        return new UUID(hi, lo);
    }

    public static List<Integer> uuidToInt(UUID uuid) {
        long hi = uuid.getMostSignificantBits();
        long lo = uuid.getLeastSignificantBits();
        return Arrays.asList((int) ((hi >> 32) & LO),
                             (int) (hi & LO),
                             (int) ((lo >> 32) & LO),
                             (int) (lo & LO));
    }
}
