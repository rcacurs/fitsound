package lv.edi.fitsound;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lipponep on 22.11.2017.
 */

        import com.google.gson.annotations.SerializedName;

public class BPMResponse {

    @SerializedName("Body")
    public final Body body;

    public BPMResponse(Body body) {
        this.body = body;
    }

    public static class Body {

        @SerializedName("JumpCount")
        public final long jumpcount;

        @SerializedName("Timestamp")
        public final long timestamp;

        public Body(long jumpcount, long timestamp) {
            this.timestamp = timestamp;
            this.jumpcount = jumpcount;
        }
    }

    public static class Headers {
        @SerializedName("Param0")
        public final int param0;

        public Headers(int param0) {
            this.param0 = param0;
        }
    }
}
