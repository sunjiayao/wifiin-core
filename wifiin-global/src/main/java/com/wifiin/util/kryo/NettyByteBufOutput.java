package com.wifiin.util.kryo;

import java.io.OutputStream;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class NettyByteBufOutput extends Output{
    private ByteBuf nettyBuf;
    /** Creates an uninitialized Output. {@link #setBuffer(byte[], int)} must be called before the Output is used. */
    public NettyByteBufOutput () {
        nettyBuf=PooledByteBufAllocator.DEFAULT.buffer();
    }

    /** Creates a new Output for writing to a byte array.
     * @param bufferSize The initial and maximum size of the buffer. An exception is thrown if this size is exceeded. */
    public NettyByteBufOutput (int bufferSize) {
        this(bufferSize, bufferSize);
    }

    /** Creates a new Output for writing to a byte array.
     * @param bufferSize The initial size of the buffer.
     * @param maxBufferSize The buffer is doubled as needed until it exceeds maxBufferSize and an exception is thrown. Can be -1
     *           for no maximum. */
    public NettyByteBufOutput (int bufferSize, int maxBufferSize) {
        if (maxBufferSize < -1) throw new IllegalArgumentException("maxBufferSize cannot be < -1: " + maxBufferSize);
        this.capacity = bufferSize;
        this.maxCapacity = maxBufferSize == -1 ? Integer.MAX_VALUE : maxBufferSize;
        nettyBuf=PooledByteBufAllocator.DEFAULT.buffer(bufferSize, maxCapacity);
    }

    /** Creates a new Output for writing to a byte array.
     * @see #setBuffer(byte[]) */
    public NettyByteBufOutput (ByteBuf buf) {
        this(buf, buf.maxCapacity());
    }

    /** Creates a new Output for writing to a byte array.
     * @see #setBuffer(byte[], int) */
    public NettyByteBufOutput (ByteBuf buffer, int maxBufferSize) {
        if (buffer == null) throw new IllegalArgumentException("buffer cannot be null.");
        setBuffer(buffer, maxBufferSize);
    }

    /** Sets the buffer that will be written to. {@link #setBuffer(byte[], int)} is called with the specified buffer's length as the
     * maxBufferSize. */
    public void setBuffer (ByteBuf buffer) {
        setBuffer(buffer, buffer.maxWritableBytes());
    }

    /** Sets the buffer that will be written to. The position and total are reset, discarding any buffered bytes. The
     * {@link #setOutputStream(OutputStream) OutputStream} is set to null.
     * @param maxBufferSize The buffer is doubled as needed until it exceeds maxBufferSize and an exception is thrown. */
    protected void setBuffer (ByteBuf buffer, int maxBufferSize) {
        if (buffer == null) throw new IllegalArgumentException("buffer cannot be null.");
        if (maxBufferSize < -1) throw new IllegalArgumentException("maxBufferSize cannot be < -1: " + maxBufferSize);
        this.nettyBuf = buffer;
        this.maxCapacity = maxBufferSize == -1 ? Integer.MAX_VALUE : maxBufferSize;
        capacity = buffer.capacity();
        position = 0;
        total = 0;
        outputStream = null;
    }

    /** Returns the buffer. The bytes between zero and {@link #position()} are the data that has been written. */
    public ByteBuf getByteBuf () {
        return nettyBuf;
    }

    /** Returns a new byte array containing the bytes currently in the buffer between zero and {@link #position()}. */
    public byte[] toBytes () {
        byte[] newBuffer = new byte[nettyBuf.writerIndex()];
        nettyBuf.readBytes(newBuffer);
        return newBuffer;
    }

    /** Returns the current position in the buffer. This is the number of bytes that have not been flushed. */
    public int position () {
        return position=nettyBuf.writerIndex();
    }

    /** Sets the current position in the buffer. */
    public void setPosition (int position) {
        nettyBuf.writerIndex(position);
        this.position=position;
    }

    /** Returns the total number of bytes written. This may include bytes that have not been flushed. */
    public long total () {
        return total=nettyBuf.writerIndex();
    }

    /** Sets the position and total to zero. */
    public void clear () {
        position = 0;
        total = 0;
        nettyBuf.clear();
    }
    private void refreshPosition(){
        position();
    }
    /** @return true if the buffer has been resized. */
    protected boolean require (int required) throws KryoException {
        int writable=nettyBuf.writableBytes();
        int capacity=nettyBuf.capacity();
        int position=position();
        if (writable >= required) return false;
        if (position+required > maxCapacity){
            throw new KryoException("Buffer overflow. Max capacity: " + maxCapacity + ", required: " + required);
        }
        if (capacity == maxCapacity){
            throw new KryoException("Buffer overflow. Available: " + (capacity - position) + ", required: " + required);
        }
        // Grow buffer.
        if (capacity == 0) capacity = 1;
        capacity = Math.min(capacity * 2, maxCapacity);
        if (capacity < 0) capacity = maxCapacity;
        nettyBuf.capacity(capacity);
        this.capacity=capacity;
        return true;
    }


    /** Writes the buffered bytes to the underlying OutputStream, if any. */
    public void flush () throws KryoException {
    }

    /** Flushes any buffered bytes and closes the underlying OutputStream, if any. */
    public void close () throws KryoException {
        clear();
    }

    /** Writes a byte. */
    public void write (int value) throws KryoException {
        writeByte((byte)value);
    }

    /** Writes the bytes. Note the byte[] length is not written. */
    public void write (byte[] bytes) throws KryoException {
        if (bytes == null) throw new IllegalArgumentException("bytes cannot be null.");
        writeBytes(bytes, 0, bytes.length);
    }

    /** Writes the bytes. Note the byte[] length is not written. */
    public void write (byte[] bytes, int offset, int length) throws KryoException {
        writeBytes(bytes, offset, length);
    }

    // byte

    public void writeByte (byte value) throws KryoException {
        if (nettyBuf.writerIndex() == nettyBuf.capacity()) require(1);
        nettyBuf.writeByte(value);
        refreshPosition();
    }

    public void writeByte (int value) throws KryoException {
        writeByte((byte)value);
    }

    /** Writes the bytes. Note the byte[] length is not written. */
    public void writeBytes (byte[] bytes) throws KryoException {
        if (bytes == null) throw new IllegalArgumentException("bytes cannot be null.");
        writeBytes(bytes, 0, bytes.length);
    }

    /** Writes the bytes. Note the byte[] length is not written. */
    public void writeBytes (byte[] bytes, int offset, int count) throws KryoException {
        if (bytes == null) throw new IllegalArgumentException("bytes cannot be null.");
        int writable=nettyBuf.writableBytes();
        if(writable<count){
            require(count);
        }
        nettyBuf.writeBytes(bytes,offset,count);
        refreshPosition();
    }

    // int

    /** Writes a 4 byte int. Uses BIG_ENDIAN byte order. */
    public void writeInt (int value) throws KryoException {
        require(4);
        nettyBuf.writeInt(value);
        refreshPosition();
    }

    /** Writes a 1-5 byte int. This stream may consider such a variable length encoding request as a hint. It is not guaranteed that
     * a variable length encoding will be really used. The stream may decide to use native-sized integer representation for
     * efficiency reasons.
     * 
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     *           inefficient (5 bytes). */
    public int writeInt (int value, boolean optimizePositive) throws KryoException {
        return writeVarInt(value, optimizePositive);
    }

    /** Writes a 1-5 byte int. It is guaranteed that a varible length encoding will be used.
     * 
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     *           inefficient (5 bytes). */
    public int writeVarInt (int value, boolean optimizePositive) throws KryoException {
        if (!optimizePositive) value = (value << 1) ^ (value >> 31);
        if (value >>> 7 == 0) {
            require(1);
            nettyBuf.writeByte(value);
            refreshPosition();
            return 1;
        }
        if (value >>> 14 == 0) {
            require(2);
            nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
            nettyBuf.writeByte((byte)(value >>> 7));
            refreshPosition();
            return 2;
        }
        if (value >>> 21 == 0) {
            require(3);
            nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
            nettyBuf.writeByte((byte)(value >>> 7 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 14));
            refreshPosition();
            return 3;
        }
        if (value >>> 28 == 0) {
            require(4);
            nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
            nettyBuf.writeByte((byte)(value >>> 7 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 14 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 21));
            refreshPosition();
            return 4;
        }
        require(5);
        nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
        nettyBuf.writeByte((byte)(value >>> 7 | 0x80));
        nettyBuf.writeByte((byte)(value >>> 14 | 0x80));
        nettyBuf.writeByte((byte)(value >>> 21 | 0x80));
        nettyBuf.writeByte((byte)(value >>> 28));
        refreshPosition();
        return 5;
    }

    // string

    /** Writes the length and string, or null. Short strings are checked and if ASCII they are written more efficiently, else they
     * are written as UTF8. If a string is known to be ASCII, {@link #writeAscii(String)} may be used. The string can be read using
     * {@link Input#readString()} or {@link Input#readStringBuilder()}.
     * @param value May be null. */
    public void writeString (String value) throws KryoException {
        writeString((CharSequence)value);
    }
    private void writeCharSequence(CharSequence value){
        int charCount=value.length();
        writeUtf8Length(charCount + 1);
        int charIndex = 0;
        if (nettyBuf.writableBytes() >= charCount) {
            // Try to write 8 bit chars.
            ByteBuf buffer = nettyBuf;
            for (; charIndex < charCount; charIndex++) {
                int c = value.charAt(charIndex);
                if (c > 127) break;
                buffer.writeByte(c);
            }
            this.position = buffer.writerIndex();
        }
        if (charIndex < charCount){
            writeString_slow(value, charCount, charIndex);
        }
    }
    /** Writes the length and CharSequence as UTF8, or null. The string can be read using {@link Input#readString()} or
     * {@link Input#readStringBuilder()}.
     * @param value May be null. */
    public void writeString (CharSequence value) throws KryoException {
        if (value == null) {
            writeByte(0x80); // 0 means null, bit 8 means UTF8.
            return;
        }
        int charCount = value.length();
        if (charCount == 0) {
            writeByte(1 | 0x80); // 1 means empty string, bit 8 means UTF8.
            return;
        }
        writeCharSequence(value);
    }

    /** Writes a string that is known to contain only ASCII characters. Non-ASCII strings passed to this method will be corrupted.
     * Each byte is a 7 bit character with the remaining byte denoting if another character is available. This is slightly more
     * efficient than {@link #writeString(String)}. The string can be read using {@link Input#readString()} or
     * {@link Input#readStringBuilder()}.
     * @param value May be null. */
    public void writeAscii (String value) throws KryoException {
        if (value == null) {
            writeByte(0x80); // 0 means null, bit 8 means UTF8.
            return;
        }
        int charCount = value.length();
        switch (charCount) {
        case 0:
            writeByte(1 | 0x80); // 1 is string length + 1, bit 8 means UTF8.
            return;
        case 1:
            writeByte(2 | 0x80); // 2 is string length + 1, bit 8 means UTF8.
            writeByte(value.charAt(0));
            return;
        }
        if (nettyBuf.writableBytes() < charCount)
            writeAscii_slow(value, charCount);
        else {
            writeCharSequence(value);
        }
        refreshPosition();
        int index=position-1;
        nettyBuf.setByte(index, nettyBuf.getByte(index) | 0x80);//all characters are ascii chars
    }

    /** Writes the length of a string, which is a variable length encoded int except the first byte uses bit 8 to denote UTF8 and
     * bit 7 to denote if another byte is present. */
    private void writeUtf8Length (int value) {
        if (value >>> 6 == 0) {
            require(1);
            nettyBuf.writeByte((byte)(value | 0x80)); // Set bit 8.
            refreshPosition();
        } else if (value >>> 13 == 0) {
            require(2);
            nettyBuf.writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
            nettyBuf.writeByte((byte)(value >>> 6));
            refreshPosition();
        } else if (value >>> 20 == 0) {
            require(3);
            nettyBuf.writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
            nettyBuf.writeByte((byte)((value >>> 6) | 0x80)); // Set bit 8.
            nettyBuf.writeByte((byte)(value >>> 13));
            refreshPosition();
        } else if (value >>> 27 == 0) {
            require(4);
            nettyBuf.writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
            nettyBuf.writeByte((byte)((value >>> 6) | 0x80)); // Set bit 8.
            nettyBuf.writeByte((byte)((value >>> 13) | 0x80)); // Set bit 8.
            nettyBuf.writeByte((byte)(value >>> 20));
            refreshPosition();
        } else {
            require(5);
            nettyBuf.writeByte((byte)(value | 0x40 | 0x80)); // Set bit 7 and 8.
            nettyBuf.writeByte((byte)((value >>> 6) | 0x80)); // Set bit 8.
            nettyBuf.writeByte((byte)((value >>> 13) | 0x80)); // Set bit 8.
            nettyBuf.writeByte((byte)((value >>> 20) | 0x80)); // Set bit 8.
            nettyBuf.writeByte((byte)(value >>> 27));
            refreshPosition();
        }
    }

    private void writeString_slow (CharSequence value, int charCount, int charIndex) {
        for (; charIndex < charCount; charIndex++) {
            int capacity=nettyBuf.capacity();
            if (nettyBuf.writerIndex() == capacity) require(Math.min(capacity, charCount - charIndex));
            int c = value.charAt(charIndex);
            if (c <= 0x007F) {
                nettyBuf.writeByte((byte)c);
            } else if (c > 0x07FF) {
                nettyBuf.writeByte((byte)(0xE0 | c >> 12 & 0x0F));
                require(2);
                nettyBuf.writeByte((byte)(0x80 | c >> 6 & 0x3F));
                nettyBuf.writeByte((byte)(0x80 | c & 0x3F));
            } else {
                nettyBuf.writeByte((byte)(0xC0 | c >> 6 & 0x1F));
                require(1);
                nettyBuf.writeByte((byte)(0x80 | c & 0x3F));
            }
        }
        refreshPosition();
    }

    private void writeAscii_slow (String value, int charCount) throws KryoException {
        int charsToWrite = Math.min(charCount, nettyBuf.maxCapacity());
        require(charsToWrite);
        for(int charIndex = 0;charIndex<charCount;charIndex++){
            nettyBuf.writeByte(value.charAt(charIndex));
        }
        refreshPosition();
    }

    // float

    /** Writes a 4 byte float. */
    public void writeFloat (float value) throws KryoException {
        writeInt(Float.floatToIntBits(value));
    }

    /** Writes a 1-5 byte float with reduced precision.
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     *           inefficient (5 bytes). */
    public int writeFloat (float value, float precision, boolean optimizePositive) throws KryoException {
        return writeInt((int)(value * precision), optimizePositive);
    }

    // short

    /** Writes a 2 byte short. Uses BIG_ENDIAN byte order. */
    public void writeShort (int value) throws KryoException {
        require(2);
        nettyBuf.writeByte((byte)(value >>> 8));
        nettyBuf.writeByte((byte)value);
        refreshPosition();
    }

    // long

    /** Writes an 8 byte long. Uses BIG_ENDIAN byte order. */
    public void writeLong (long value) throws KryoException {
        require(8);
        nettyBuf.writeByte((byte)(value >>> 56));
        nettyBuf.writeByte((byte)(value >>> 48));
        nettyBuf.writeByte((byte)(value >>> 40));
        nettyBuf.writeByte((byte)(value >>> 32));
        nettyBuf.writeByte((byte)(value >>> 24));
        nettyBuf.writeByte((byte)(value >>> 16));
        nettyBuf.writeByte((byte)(value >>> 8));
        nettyBuf.writeByte((byte)value);
        refreshPosition();
    }

    /** Writes a 1-9 byte long. This stream may consider such a variable length encoding request as a hint. It is not guaranteed
     * that a variable length encoding will be really used. The stream may decide to use native-sized integer representation for
     * efficiency reasons.
     * 
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     *           inefficient (9 bytes). */
    public int writeLong (long value, boolean optimizePositive) throws KryoException {
        return writeVarLong(value, optimizePositive);
    }

    /** Writes a 1-9 byte long. It is guaranteed that a varible length encoding will be used.
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     *           inefficient (9 bytes). */
    public int writeVarLong (long value, boolean optimizePositive) throws KryoException {
        if (!optimizePositive) value = (value << 1) ^ (value >> 63);
        if (value >>> 7 == 0) {
            require(1);
            nettyBuf.writeByte((byte)value);
            refreshPosition();
            return 1;
        }
        if (value >>> 14 == 0) {
            require(2);
            nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
            nettyBuf.writeByte((byte)(value >>> 7));
            refreshPosition();
            return 2;
        }
        if (value >>> 21 == 0) {
            require(3);
            nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
            nettyBuf.writeByte((byte)(value >>> 7 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 14));
            refreshPosition();
            return 3;
        }
        if (value >>> 28 == 0) {
            require(4);
            nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
            nettyBuf.writeByte((byte)(value >>> 7 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 14 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 21));
            refreshPosition();
            return 4;
        }
        if (value >>> 35 == 0) {
            require(5);
            nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
            nettyBuf.writeByte((byte)(value >>> 7 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 14 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 21 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 28));
            refreshPosition();
            return 5;
        }
        if (value >>> 42 == 0) {
            require(6);
            nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
            nettyBuf.writeByte((byte)(value >>> 7 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 14 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 21 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 28 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 35));
            refreshPosition();
            return 6;
        }
        if (value >>> 49 == 0) {
            require(7);
            nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
            nettyBuf.writeByte((byte)(value >>> 7 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 14 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 21 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 28 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 35 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 42));
            refreshPosition();
            return 7;
        }
        if (value >>> 56 == 0) {
            require(8);
            nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
            nettyBuf.writeByte((byte)(value >>> 7 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 14 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 21 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 28 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 35 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 42 | 0x80));
            nettyBuf.writeByte((byte)(value >>> 49));
            refreshPosition();
            return 8;
        }
        require(9);
        nettyBuf.writeByte((byte)((value & 0x7F) | 0x80));
        nettyBuf.writeByte((byte)(value >>> 7 | 0x80));
        nettyBuf.writeByte((byte)(value >>> 14 | 0x80));
        nettyBuf.writeByte((byte)(value >>> 21 | 0x80));
        nettyBuf.writeByte((byte)(value >>> 28 | 0x80));
        nettyBuf.writeByte((byte)(value >>> 35 | 0x80));
        nettyBuf.writeByte((byte)(value >>> 42 | 0x80));
        nettyBuf.writeByte((byte)(value >>> 49 | 0x80));
        nettyBuf.writeByte((byte)(value >>> 56));
        refreshPosition();
        return 9;
    }

    // boolean

    /** Writes a 1 byte boolean. */
    public void writeBoolean (boolean value) throws KryoException {
        if (position == capacity) require(1);
        nettyBuf.writeByte((byte)(value ? 1 : 0));
        refreshPosition();
    }

    // char

    /** Writes a 2 byte char. Uses BIG_ENDIAN byte order. */
    public void writeChar (char value) throws KryoException {
        require(2);
        nettyBuf.writeByte((byte)(value >>> 8));
        nettyBuf.writeByte((byte)value);
        refreshPosition();
    }

    // double

    /** Writes an 8 byte double. */
    public void writeDouble (double value) throws KryoException {
        writeLong(Double.doubleToLongBits(value));
    }

    /** Writes a 1-9 byte double with reduced precision.
     * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
     *           inefficient (9 bytes). */
    public int writeDouble (double value, double precision, boolean optimizePositive) throws KryoException {
        return writeLong((long)(value * precision), optimizePositive);
    }

    /** Returns the number of bytes that would be written with {@link #writeInt(int, boolean)}. */
    static public int intLength (int value, boolean optimizePositive) {
        if (!optimizePositive) value = (value << 1) ^ (value >> 31);
        if (value >>> 7 == 0) return 1;
        if (value >>> 14 == 0) return 2;
        if (value >>> 21 == 0) return 3;
        if (value >>> 28 == 0) return 4;
        return 5;
    }

    /** Returns the number of bytes that would be written with {@link #writeLong(long, boolean)}. */
    static public int longLength (long value, boolean optimizePositive) {
        if (!optimizePositive) value = (value << 1) ^ (value >> 63);
        if (value >>> 7 == 0) return 1;
        if (value >>> 14 == 0) return 2;
        if (value >>> 21 == 0) return 3;
        if (value >>> 28 == 0) return 4;
        if (value >>> 35 == 0) return 5;
        if (value >>> 42 == 0) return 6;
        if (value >>> 49 == 0) return 7;
        if (value >>> 56 == 0) return 8;
        return 9;
    }

    // Methods implementing bulk operations on arrays of primitive types

    /** Bulk output of an int array. */
    public void writeInts (int[] object, boolean optimizePositive) throws KryoException {
        for (int i = 0, n = object.length; i < n; i++)
            writeInt(object[i], optimizePositive);
    }

    /** Bulk output of an long array. */
    public void writeLongs (long[] object, boolean optimizePositive) throws KryoException {
        for (int i = 0, n = object.length; i < n; i++)
            writeLong(object[i], optimizePositive);
    }

    /** Bulk output of an int array. */
    public void writeInts (int[] object) throws KryoException {
        for (int i = 0, n = object.length; i < n; i++)
            writeInt(object[i]);
    }

    /** Bulk output of an long array. */
    public void writeLongs (long[] object) throws KryoException {
        for (int i = 0, n = object.length; i < n; i++)
            writeLong(object[i]);
    }

    /** Bulk output of a float array. */
    public void writeFloats (float[] object) throws KryoException {
        for (int i = 0, n = object.length; i < n; i++)
            writeFloat(object[i]);
    }

    /** Bulk output of a short array. */
    public void writeShorts (short[] object) throws KryoException {
        for (int i = 0, n = object.length; i < n; i++)
            writeShort(object[i]);
    }

    /** Bulk output of a char array. */
    public void writeChars (char[] object) throws KryoException {
        for (int i = 0, n = object.length; i < n; i++)
            writeChar(object[i]);
    }

    /** Bulk output of a double array. */
    public void writeDoubles (double[] object) throws KryoException {
        for (int i = 0, n = object.length; i < n; i++)
            writeDouble(object[i]);
    }
}
