package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.io.NumberInput;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

public final class TextBuffer {
    static final int MAX_SEGMENT_LEN = 262144;
    static final int MIN_SEGMENT_LEN = 1000;
    static final char[] NO_CHARS = new char[0];
    private final BufferRecycler _allocator;
    private char[] _currentSegment;
    private int _currentSize;
    private boolean _hasSegments;
    private char[] _inputBuffer;
    private int _inputLen;
    private int _inputStart;
    private char[] _resultArray;
    private String _resultString;
    private int _segmentSize;
    private ArrayList<char[]> _segments;

    public TextBuffer(BufferRecycler bufferRecycler) {
        this._allocator = bufferRecycler;
    }

    private char[] buf(int i) {
        return this._allocator != null ? this._allocator.allocCharBuffer(2, i) : new char[Math.max(i, 1000)];
    }

    private char[] carr(int i) {
        return new char[i];
    }

    private void clearSegments() {
        this._hasSegments = false;
        this._segments.clear();
        this._segmentSize = 0;
        this._currentSize = 0;
    }

    private void expand(int i) {
        int i2 = 262144;
        if (this._segments == null) {
            this._segments = new ArrayList();
        }
        Object obj = this._currentSegment;
        this._hasSegments = true;
        this._segments.add(obj);
        this._segmentSize += obj.length;
        this._currentSize = 0;
        int length = obj.length;
        length += length >> 1;
        if (length < 1000) {
            i2 = 1000;
        } else if (length <= 262144) {
            i2 = length;
        }
        this._currentSegment = carr(i2);
    }

    private char[] resultArray() {
        if (this._resultString != null) {
            return this._resultString.toCharArray();
        }
        int i;
        int i2;
        if (this._inputStart >= 0) {
            i = this._inputLen;
            if (i <= 0) {
                return NO_CHARS;
            }
            i2 = this._inputStart;
            return i2 == 0 ? Arrays.copyOf(this._inputBuffer, i) : Arrays.copyOfRange(this._inputBuffer, i2, i + i2);
        } else {
            i = size();
            if (i <= 0) {
                return NO_CHARS;
            }
            Object carr = carr(i);
            if (this._segments != null) {
                int size = this._segments.size();
                i2 = 0;
                for (int i3 = 0; i3 < size; i3++) {
                    char[] cArr = (char[]) this._segments.get(i3);
                    int length = cArr.length;
                    System.arraycopy(cArr, 0, carr, i2, length);
                    i2 += length;
                }
                i = i2;
            } else {
                i = 0;
            }
            System.arraycopy(this._currentSegment, 0, carr, i, this._currentSize);
            return carr;
        }
    }

    private void unshare(int i) {
        int i2 = this._inputLen;
        this._inputLen = 0;
        Object obj = this._inputBuffer;
        this._inputBuffer = null;
        int i3 = this._inputStart;
        this._inputStart = -1;
        int i4 = i2 + i;
        if (this._currentSegment == null || i4 > this._currentSegment.length) {
            this._currentSegment = buf(i4);
        }
        if (i2 > 0) {
            System.arraycopy(obj, i3, this._currentSegment, 0, i2);
        }
        this._segmentSize = 0;
        this._currentSize = i2;
    }

    public final void append(char c) {
        if (this._inputStart >= 0) {
            unshare(16);
        }
        this._resultString = null;
        this._resultArray = null;
        char[] cArr = this._currentSegment;
        if (this._currentSize >= cArr.length) {
            expand(1);
            cArr = this._currentSegment;
        }
        int i = this._currentSize;
        this._currentSize = i + 1;
        cArr[i] = c;
    }

    public final void append(String str, int i, int i2) {
        if (this._inputStart >= 0) {
            unshare(i2);
        }
        this._resultString = null;
        this._resultArray = null;
        char[] cArr = this._currentSegment;
        int length = cArr.length - this._currentSize;
        if (length >= i2) {
            str.getChars(i, i + i2, cArr, this._currentSize);
            this._currentSize += i2;
            return;
        }
        if (length > 0) {
            str.getChars(i, i + length, cArr, this._currentSize);
            i2 -= length;
            i += length;
        }
        do {
            expand(i2);
            int min = Math.min(this._currentSegment.length, i2);
            str.getChars(i, i + min, this._currentSegment, 0);
            this._currentSize += min;
            i += min;
            i2 -= min;
        } while (i2 > 0);
    }

    public final void append(char[] cArr, int i, int i2) {
        if (this._inputStart >= 0) {
            unshare(i2);
        }
        this._resultString = null;
        this._resultArray = null;
        Object obj = this._currentSegment;
        int length = obj.length - this._currentSize;
        if (length >= i2) {
            System.arraycopy(cArr, i, obj, this._currentSize, i2);
            this._currentSize += i2;
            return;
        }
        if (length > 0) {
            System.arraycopy(cArr, i, obj, this._currentSize, length);
            i += length;
            i2 -= length;
        }
        do {
            expand(i2);
            int min = Math.min(this._currentSegment.length, i2);
            System.arraycopy(cArr, i, this._currentSegment, 0, min);
            this._currentSize += min;
            i += min;
            i2 -= min;
        } while (i2 > 0);
    }

    public final char[] contentsAsArray() {
        char[] cArr = this._resultArray;
        if (cArr != null) {
            return cArr;
        }
        cArr = resultArray();
        this._resultArray = cArr;
        return cArr;
    }

    public final BigDecimal contentsAsDecimal() throws NumberFormatException {
        return this._resultArray != null ? NumberInput.parseBigDecimal(this._resultArray) : (this._inputStart < 0 || this._inputBuffer == null) ? (this._segmentSize != 0 || this._currentSegment == null) ? NumberInput.parseBigDecimal(contentsAsArray()) : NumberInput.parseBigDecimal(this._currentSegment, 0, this._currentSize) : NumberInput.parseBigDecimal(this._inputBuffer, this._inputStart, this._inputLen);
    }

    public final double contentsAsDouble() throws NumberFormatException {
        return NumberInput.parseDouble(contentsAsString());
    }

    public final String contentsAsString() {
        if (this._resultString == null) {
            if (this._resultArray != null) {
                this._resultString = new String(this._resultArray);
            } else if (this._inputStart < 0) {
                int i = this._segmentSize;
                int i2 = this._currentSize;
                if (i == 0) {
                    this._resultString = i2 == 0 ? "" : new String(this._currentSegment, 0, i2);
                } else {
                    StringBuilder stringBuilder = new StringBuilder(i + i2);
                    if (this._segments != null) {
                        int size = this._segments.size();
                        for (i2 = 0; i2 < size; i2++) {
                            char[] cArr = (char[]) this._segments.get(i2);
                            stringBuilder.append(cArr, 0, cArr.length);
                        }
                    }
                    stringBuilder.append(this._currentSegment, 0, this._currentSize);
                    this._resultString = stringBuilder.toString();
                }
            } else if (this._inputLen <= 0) {
                this._resultString = "";
                return "";
            } else {
                this._resultString = new String(this._inputBuffer, this._inputStart, this._inputLen);
            }
        }
        return this._resultString;
    }

    public final char[] emptyAndGetCurrentSegment() {
        this._inputStart = -1;
        this._currentSize = 0;
        this._inputLen = 0;
        this._inputBuffer = null;
        this._resultString = null;
        this._resultArray = null;
        if (this._hasSegments) {
            clearSegments();
        }
        char[] cArr = this._currentSegment;
        if (cArr != null) {
            return cArr;
        }
        cArr = buf(0);
        this._currentSegment = cArr;
        return cArr;
    }

    public final void ensureNotShared() {
        if (this._inputStart >= 0) {
            unshare(16);
        }
    }

    public final char[] expandCurrentSegment() {
        char[] cArr = this._currentSegment;
        int length = cArr.length;
        int i = (length >> 1) + length;
        if (i > 262144) {
            i = (length >> 2) + length;
        }
        char[] copyOf = Arrays.copyOf(cArr, i);
        this._currentSegment = copyOf;
        return copyOf;
    }

    public final char[] expandCurrentSegment(int i) {
        char[] cArr = this._currentSegment;
        if (cArr.length >= i) {
            return cArr;
        }
        cArr = Arrays.copyOf(cArr, i);
        this._currentSegment = cArr;
        return cArr;
    }

    public final char[] finishCurrentSegment() {
        int i = 262144;
        if (this._segments == null) {
            this._segments = new ArrayList();
        }
        this._hasSegments = true;
        this._segments.add(this._currentSegment);
        int length = this._currentSegment.length;
        this._segmentSize += length;
        this._currentSize = 0;
        length += length >> 1;
        if (length < 1000) {
            i = 1000;
        } else if (length <= 262144) {
            i = length;
        }
        char[] carr = carr(i);
        this._currentSegment = carr;
        return carr;
    }

    public final char[] getCurrentSegment() {
        if (this._inputStart >= 0) {
            unshare(1);
        } else {
            char[] cArr = this._currentSegment;
            if (cArr == null) {
                this._currentSegment = buf(0);
            } else if (this._currentSize >= cArr.length) {
                expand(1);
            }
        }
        return this._currentSegment;
    }

    public final int getCurrentSegmentSize() {
        return this._currentSize;
    }

    public final char[] getTextBuffer() {
        if (this._inputStart >= 0) {
            return this._inputBuffer;
        }
        if (this._resultArray != null) {
            return this._resultArray;
        }
        if (this._resultString == null) {
            return !this._hasSegments ? this._currentSegment == null ? NO_CHARS : this._currentSegment : contentsAsArray();
        } else {
            char[] toCharArray = this._resultString.toCharArray();
            this._resultArray = toCharArray;
            return toCharArray;
        }
    }

    public final int getTextOffset() {
        return this._inputStart >= 0 ? this._inputStart : 0;
    }

    public final boolean hasTextAsCharacters() {
        return this._inputStart >= 0 || this._resultArray != null || this._resultString == null;
    }

    public final void releaseBuffers() {
        if (this._allocator == null) {
            resetWithEmpty();
        } else if (this._currentSegment != null) {
            resetWithEmpty();
            char[] cArr = this._currentSegment;
            this._currentSegment = null;
            this._allocator.releaseCharBuffer(2, cArr);
        }
    }

    public final void resetWithCopy(char[] cArr, int i, int i2) {
        this._inputBuffer = null;
        this._inputStart = -1;
        this._inputLen = 0;
        this._resultString = null;
        this._resultArray = null;
        if (this._hasSegments) {
            clearSegments();
        } else if (this._currentSegment == null) {
            this._currentSegment = buf(i2);
        }
        this._segmentSize = 0;
        this._currentSize = 0;
        append(cArr, i, i2);
    }

    public final void resetWithEmpty() {
        this._inputStart = -1;
        this._currentSize = 0;
        this._inputLen = 0;
        this._inputBuffer = null;
        this._resultString = null;
        this._resultArray = null;
        if (this._hasSegments) {
            clearSegments();
        }
    }

    public final void resetWithShared(char[] cArr, int i, int i2) {
        this._resultString = null;
        this._resultArray = null;
        this._inputBuffer = cArr;
        this._inputStart = i;
        this._inputLen = i2;
        if (this._hasSegments) {
            clearSegments();
        }
    }

    public final void resetWithString(String str) {
        this._inputBuffer = null;
        this._inputStart = -1;
        this._inputLen = 0;
        this._resultString = str;
        this._resultArray = null;
        if (this._hasSegments) {
            clearSegments();
        }
        this._currentSize = 0;
    }

    public final String setCurrentAndReturn(int i) {
        this._currentSize = i;
        if (this._segmentSize > 0) {
            return contentsAsString();
        }
        int i2 = this._currentSize;
        String str = i2 == 0 ? "" : new String(this._currentSegment, 0, i2);
        this._resultString = str;
        return str;
    }

    public final void setCurrentLength(int i) {
        this._currentSize = i;
    }

    public final int size() {
        return this._inputStart >= 0 ? this._inputLen : this._resultArray != null ? this._resultArray.length : this._resultString != null ? this._resultString.length() : this._segmentSize + this._currentSize;
    }

    public final String toString() {
        return contentsAsString();
    }
}
