package com.fasterxml.jackson.core.json;

import android.support.v4.media.TransportMediator;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.google.android.gms.location.places.Place;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class UTF8StreamJsonParser extends ParserBase {
    static final byte BYTE_LF = (byte) 10;
    protected static final int[] _icLatin1 = CharTypes.getInputCodeLatin1();
    private static final int[] _icUTF8 = CharTypes.getInputCodeUtf8();
    protected boolean _bufferRecyclable;
    protected byte[] _inputBuffer;
    protected InputStream _inputStream;
    protected int _nameStartCol;
    protected int _nameStartOffset;
    protected int _nameStartRow;
    protected ObjectCodec _objectCodec;
    private int _quad1;
    protected int[] _quadBuffer = new int[16];
    protected final ByteQuadsCanonicalizer _symbols;
    protected boolean _tokenIncomplete;

    public UTF8StreamJsonParser(IOContext iOContext, int i, InputStream inputStream, ObjectCodec objectCodec, ByteQuadsCanonicalizer byteQuadsCanonicalizer, byte[] bArr, int i2, int i3, boolean z) {
        super(iOContext, i);
        this._inputStream = inputStream;
        this._objectCodec = objectCodec;
        this._symbols = byteQuadsCanonicalizer;
        this._inputBuffer = bArr;
        this._inputPtr = i2;
        this._inputEnd = i3;
        this._currInputRowStart = i2;
        this._currInputProcessed = (long) (-i2);
        this._bufferRecyclable = z;
    }

    private final void _checkMatchEnd(String str, int i, int i2) throws IOException {
        if (Character.isJavaIdentifierPart((char) _decodeCharForError(i2))) {
            _reportInvalidToken(str.substring(0, i));
        }
    }

    private final int _decodeUtf8_2(int i) throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        return (b & 63) | ((i & 31) << 6);
    }

    private final int _decodeUtf8_3(int i) throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr2 = this._inputBuffer;
        int i3 = this._inputPtr;
        this._inputPtr = i3 + 1;
        byte b2 = bArr2[i3];
        if ((b2 & 192) != 128) {
            _reportInvalidOther(b2 & 255, this._inputPtr);
        }
        return (((b & 63) | ((i & 15) << 6)) << 6) | (b2 & 63);
    }

    private final int _decodeUtf8_3fast(int i) throws IOException {
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        byte[] bArr2 = this._inputBuffer;
        int i3 = this._inputPtr;
        this._inputPtr = i3 + 1;
        byte b2 = bArr2[i3];
        if ((b2 & 192) != 128) {
            _reportInvalidOther(b2 & 255, this._inputPtr);
        }
        return (((b & 63) | ((i & 15) << 6)) << 6) | (b2 & 63);
    }

    private final int _decodeUtf8_4(int i) throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr2 = this._inputBuffer;
        int i3 = this._inputPtr;
        this._inputPtr = i3 + 1;
        byte b2 = bArr2[i3];
        if ((b2 & 192) != 128) {
            _reportInvalidOther(b2 & 255, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr3 = this._inputBuffer;
        int i4 = this._inputPtr;
        this._inputPtr = i4 + 1;
        byte b3 = bArr3[i4];
        if ((b3 & 192) != 128) {
            _reportInvalidOther(b3 & 255, this._inputPtr);
        }
        return ((((((b & 63) | ((i & 7) << 6)) << 6) | (b2 & 63)) << 6) | (b3 & 63)) - 65536;
    }

    private final void _finishString2(char[] cArr, int i) throws IOException {
        int[] iArr = _icUTF8;
        byte[] bArr = this._inputBuffer;
        while (true) {
            int i2 = this._inputPtr;
            if (i2 >= this._inputEnd) {
                loadMoreGuaranteed();
                i2 = this._inputPtr;
            }
            if (i >= cArr.length) {
                cArr = this._textBuffer.finishCurrentSegment();
                i = 0;
            }
            int min = Math.min(this._inputEnd, (cArr.length - i) + i2);
            while (i2 < min) {
                int i3 = i2 + 1;
                i2 = bArr[i2] & 255;
                if (iArr[i2] != 0) {
                    this._inputPtr = i3;
                    if (i2 != 34) {
                        switch (iArr[i2]) {
                            case 1:
                                i2 = _decodeEscaped();
                                break;
                            case 2:
                                i2 = _decodeUtf8_2(i2);
                                break;
                            case 3:
                                if (this._inputEnd - this._inputPtr < 2) {
                                    i2 = _decodeUtf8_3(i2);
                                    break;
                                } else {
                                    i2 = _decodeUtf8_3fast(i2);
                                    break;
                                }
                            case 4:
                                i3 = _decodeUtf8_4(i2);
                                i2 = i + 1;
                                cArr[i] = (char) (GeneratorBase.SURR1_FIRST | (i3 >> 10));
                                if (i2 >= cArr.length) {
                                    cArr = this._textBuffer.finishCurrentSegment();
                                    i = 0;
                                } else {
                                    i = i2;
                                }
                                i2 = (i3 & Place.TYPE_SUBLOCALITY_LEVEL_1) | GeneratorBase.SURR2_FIRST;
                                break;
                            default:
                                if (i2 >= 32) {
                                    _reportInvalidChar(i2);
                                    break;
                                } else {
                                    _throwUnquotedSpace(i2, "string value");
                                    break;
                                }
                        }
                        if (i >= cArr.length) {
                            cArr = this._textBuffer.finishCurrentSegment();
                            i3 = 0;
                        } else {
                            i3 = i;
                        }
                        i = i3 + 1;
                        cArr[i3] = (char) i2;
                    } else {
                        this._textBuffer.setCurrentLength(i);
                        return;
                    }
                }
                cArr[i] = (char) i2;
                i++;
                i2 = i3;
            }
            this._inputPtr = i2;
        }
    }

    private final boolean _isNextTokenNameMaybe(int i, SerializableString serializableString) throws IOException {
        String _parseName = _parseName(i);
        this._parsingContext.setCurrentName(_parseName);
        boolean equals = _parseName.equals(serializableString.getValue());
        this._currToken = JsonToken.FIELD_NAME;
        int _skipColon = _skipColon();
        _updateLocation();
        if (_skipColon == 34) {
            this._tokenIncomplete = true;
            this._nextToken = JsonToken.VALUE_STRING;
            return equals;
        }
        JsonToken _parseNegNumber;
        switch (_skipColon) {
            case 45:
                _parseNegNumber = _parseNegNumber();
                break;
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                _parseNegNumber = _parsePosNumber(_skipColon);
                break;
            case 91:
                _parseNegNumber = JsonToken.START_ARRAY;
                break;
            case 102:
                _matchToken("false", 1);
                _parseNegNumber = JsonToken.VALUE_FALSE;
                break;
            case 110:
                _matchToken("null", 1);
                _parseNegNumber = JsonToken.VALUE_NULL;
                break;
            case 116:
                _matchToken("true", 1);
                _parseNegNumber = JsonToken.VALUE_TRUE;
                break;
            case 123:
                _parseNegNumber = JsonToken.START_OBJECT;
                break;
            default:
                _parseNegNumber = _handleUnexpectedValue(_skipColon);
                break;
        }
        this._nextToken = _parseNegNumber;
        return equals;
    }

    private final void _isNextTokenNameYes(int i) throws IOException {
        this._currToken = JsonToken.FIELD_NAME;
        _updateLocation();
        switch (i) {
            case 34:
                this._tokenIncomplete = true;
                this._nextToken = JsonToken.VALUE_STRING;
                return;
            case 45:
                this._nextToken = _parseNegNumber();
                return;
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                this._nextToken = _parsePosNumber(i);
                return;
            case 91:
                this._nextToken = JsonToken.START_ARRAY;
                return;
            case 102:
                _matchToken("false", 1);
                this._nextToken = JsonToken.VALUE_FALSE;
                return;
            case 110:
                _matchToken("null", 1);
                this._nextToken = JsonToken.VALUE_NULL;
                return;
            case 116:
                _matchToken("true", 1);
                this._nextToken = JsonToken.VALUE_TRUE;
                return;
            case 123:
                this._nextToken = JsonToken.START_OBJECT;
                return;
            default:
                this._nextToken = _handleUnexpectedValue(i);
                return;
        }
    }

    private final void _matchToken2(String str, int i) throws IOException {
        int length = str.length();
        do {
            if ((this._inputPtr >= this._inputEnd && !loadMore()) || this._inputBuffer[this._inputPtr] != str.charAt(i)) {
                _reportInvalidToken(str.substring(0, i));
            }
            this._inputPtr++;
            i++;
        } while (i < length);
        if (this._inputPtr < this._inputEnd || loadMore()) {
            length = this._inputBuffer[this._inputPtr] & 255;
            if (length >= 48 && length != 93 && length != 125) {
                _checkMatchEnd(str, i, length);
            }
        }
    }

    private final JsonToken _nextAfterName() {
        this._nameCopied = false;
        JsonToken jsonToken = this._nextToken;
        this._nextToken = null;
        if (jsonToken == JsonToken.START_ARRAY) {
            this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
        } else if (jsonToken == JsonToken.START_OBJECT) {
            this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
        }
        this._currToken = jsonToken;
        return jsonToken;
    }

    private final JsonToken _nextTokenNotInObject(int i) throws IOException {
        if (i == 34) {
            this._tokenIncomplete = true;
            JsonToken jsonToken = JsonToken.VALUE_STRING;
            this._currToken = jsonToken;
            return jsonToken;
        }
        switch (i) {
            case 45:
                jsonToken = _parseNegNumber();
                this._currToken = jsonToken;
                return jsonToken;
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                jsonToken = _parsePosNumber(i);
                this._currToken = jsonToken;
                return jsonToken;
            case 91:
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                jsonToken = JsonToken.START_ARRAY;
                this._currToken = jsonToken;
                return jsonToken;
            case 102:
                _matchToken("false", 1);
                jsonToken = JsonToken.VALUE_FALSE;
                this._currToken = jsonToken;
                return jsonToken;
            case 110:
                _matchToken("null", 1);
                jsonToken = JsonToken.VALUE_NULL;
                this._currToken = jsonToken;
                return jsonToken;
            case 116:
                _matchToken("true", 1);
                jsonToken = JsonToken.VALUE_TRUE;
                this._currToken = jsonToken;
                return jsonToken;
            case 123:
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                jsonToken = JsonToken.START_OBJECT;
                this._currToken = jsonToken;
                return jsonToken;
            default:
                jsonToken = _handleUnexpectedValue(i);
                this._currToken = jsonToken;
                return jsonToken;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final com.fasterxml.jackson.core.JsonToken _parseFloat(char[] r10, int r11, int r12, boolean r13, int r14) throws java.io.IOException {
        /*
        r9 = this;
        r0 = 0;
        r4 = 0;
        r3 = 0;
        r1 = 46;
        if (r12 != r1) goto L_0x011f;
    L_0x0007:
        r1 = r11 + 1;
        r2 = (char) r12;
        r10[r11] = r2;
    L_0x000c:
        r2 = r9._inputPtr;
        r4 = r9._inputEnd;
        if (r2 < r4) goto L_0x00cf;
    L_0x0012:
        r2 = r9.loadMore();
        if (r2 != 0) goto L_0x00cf;
    L_0x0018:
        r4 = 1;
        r3 = r12;
    L_0x001a:
        if (r0 != 0) goto L_0x0021;
    L_0x001c:
        r2 = "Decimal point not followed by a digit";
        r9.reportUnexpectedNumberChar(r3, r2);
    L_0x0021:
        r6 = r0;
        r0 = r1;
        r1 = r10;
    L_0x0024:
        r5 = 0;
        r2 = 101; // 0x65 float:1.42E-43 double:5.0E-322;
        if (r3 == r2) goto L_0x002d;
    L_0x0029:
        r2 = 69;
        if (r3 != r2) goto L_0x0113;
    L_0x002d:
        r2 = r1.length;
        if (r0 < r2) goto L_0x0037;
    L_0x0030:
        r0 = r9._textBuffer;
        r1 = r0.finishCurrentSegment();
        r0 = 0;
    L_0x0037:
        r2 = r0 + 1;
        r3 = (char) r3;
        r1[r0] = r3;
        r0 = r9._inputPtr;
        r3 = r9._inputEnd;
        if (r0 < r3) goto L_0x0045;
    L_0x0042:
        r9.loadMoreGuaranteed();
    L_0x0045:
        r0 = r9._inputBuffer;
        r3 = r9._inputPtr;
        r7 = r3 + 1;
        r9._inputPtr = r7;
        r0 = r0[r3];
        r3 = r0 & 255;
        r0 = 45;
        if (r3 == r0) goto L_0x0059;
    L_0x0055:
        r0 = 43;
        if (r3 != r0) goto L_0x010d;
    L_0x0059:
        r0 = r1.length;
        if (r2 < r0) goto L_0x010a;
    L_0x005c:
        r0 = r9._textBuffer;
        r1 = r0.finishCurrentSegment();
        r0 = 0;
    L_0x0063:
        r2 = (char) r3;
        r1[r0] = r2;
        r2 = r9._inputPtr;
        r3 = r9._inputEnd;
        if (r2 < r3) goto L_0x006f;
    L_0x006c:
        r9.loadMoreGuaranteed();
    L_0x006f:
        r2 = r9._inputBuffer;
        r3 = r9._inputPtr;
        r7 = r3 + 1;
        r9._inputPtr = r7;
        r2 = r2[r3];
        r2 = r2 & 255;
        r3 = r0 + 1;
        r0 = r1;
        r1 = r3;
        r3 = r5;
    L_0x0080:
        r5 = 57;
        if (r2 > r5) goto L_0x0107;
    L_0x0084:
        r5 = 48;
        if (r2 < r5) goto L_0x0107;
    L_0x0088:
        r5 = r3 + 1;
        r3 = r0.length;
        if (r1 < r3) goto L_0x0094;
    L_0x008d:
        r0 = r9._textBuffer;
        r0 = r0.finishCurrentSegment();
        r1 = 0;
    L_0x0094:
        r3 = r1 + 1;
        r7 = (char) r2;
        r0[r1] = r7;
        r1 = r9._inputPtr;
        r7 = r9._inputEnd;
        if (r1 < r7) goto L_0x00f7;
    L_0x009f:
        r1 = r9.loadMore();
        if (r1 != 0) goto L_0x00f7;
    L_0x00a5:
        r1 = 1;
        r0 = r3;
        r3 = r5;
    L_0x00a8:
        if (r3 != 0) goto L_0x0126;
    L_0x00aa:
        r4 = "Exponent indicator not followed by a digit";
        r9.reportUnexpectedNumberChar(r2, r4);
        r8 = r2;
        r2 = r1;
        r1 = r8;
    L_0x00b2:
        if (r2 != 0) goto L_0x00c5;
    L_0x00b4:
        r2 = r9._inputPtr;
        r2 = r2 + -1;
        r9._inputPtr = r2;
        r2 = r9._parsingContext;
        r2 = r2.inRoot();
        if (r2 == 0) goto L_0x00c5;
    L_0x00c2:
        r9._verifyRootSpace(r1);
    L_0x00c5:
        r1 = r9._textBuffer;
        r1.setCurrentLength(r0);
        r0 = r9.resetFloat(r13, r14, r6, r3);
        return r0;
    L_0x00cf:
        r2 = r9._inputBuffer;
        r4 = r9._inputPtr;
        r5 = r4 + 1;
        r9._inputPtr = r5;
        r2 = r2[r4];
        r12 = r2 & 255;
        r2 = 48;
        if (r12 < r2) goto L_0x011b;
    L_0x00df:
        r2 = 57;
        if (r12 > r2) goto L_0x011b;
    L_0x00e3:
        r0 = r0 + 1;
        r2 = r10.length;
        if (r1 < r2) goto L_0x0119;
    L_0x00e8:
        r1 = r9._textBuffer;
        r10 = r1.finishCurrentSegment();
        r1 = 0;
        r2 = r1;
    L_0x00f0:
        r1 = r2 + 1;
        r4 = (char) r12;
        r10[r2] = r4;
        goto L_0x000c;
    L_0x00f7:
        r1 = r9._inputBuffer;
        r2 = r9._inputPtr;
        r7 = r2 + 1;
        r9._inputPtr = r7;
        r1 = r1[r2];
        r2 = r1 & 255;
        r1 = r3;
        r3 = r5;
        goto L_0x0080;
    L_0x0107:
        r0 = r1;
        r1 = r4;
        goto L_0x00a8;
    L_0x010a:
        r0 = r2;
        goto L_0x0063;
    L_0x010d:
        r0 = r1;
        r1 = r2;
        r2 = r3;
        r3 = r5;
        goto L_0x0080;
    L_0x0113:
        r1 = 0;
        r2 = r4;
        r8 = r3;
        r3 = r1;
        r1 = r8;
        goto L_0x00b2;
    L_0x0119:
        r2 = r1;
        goto L_0x00f0;
    L_0x011b:
        r4 = r3;
        r3 = r12;
        goto L_0x001a;
    L_0x011f:
        r0 = 0;
        r1 = r10;
        r6 = r0;
        r3 = r12;
        r0 = r11;
        goto L_0x0024;
    L_0x0126:
        r8 = r2;
        r2 = r1;
        r1 = r8;
        goto L_0x00b2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8StreamJsonParser._parseFloat(char[], int, int, boolean, int):com.fasterxml.jackson.core.JsonToken");
    }

    private final JsonToken _parseNumber2(char[] cArr, int i, boolean z, int i2) throws IOException {
        int i3 = i2;
        int i4 = i;
        char[] cArr2 = cArr;
        while (true) {
            int i5;
            if (this._inputPtr < this._inputEnd || loadMore()) {
                byte[] bArr = this._inputBuffer;
                i5 = this._inputPtr;
                this._inputPtr = i5 + 1;
                i5 = bArr[i5] & 255;
                if (i5 <= 57 && i5 >= 48) {
                    int i6;
                    if (i4 >= cArr2.length) {
                        cArr2 = this._textBuffer.finishCurrentSegment();
                        i6 = 0;
                    } else {
                        i6 = i4;
                    }
                    i4 = i6 + 1;
                    cArr2[i6] = (char) i5;
                    i3++;
                }
            } else {
                this._textBuffer.setCurrentLength(i4);
                return resetInt(z, i3);
            }
        }
        if (i5 == 46 || i5 == 101 || i5 == 69) {
            return _parseFloat(cArr2, i4, i5, z, i3);
        }
        this._inputPtr--;
        this._textBuffer.setCurrentLength(i4);
        if (this._parsingContext.inRoot()) {
            bArr = this._inputBuffer;
            int i7 = this._inputPtr;
            this._inputPtr = i7 + 1;
            _verifyRootSpace(bArr[i7] & 255);
        }
        return resetInt(z, i3);
    }

    private final void _skipCComment() throws IOException {
        int[] inputCodeComment = CharTypes.getInputCodeComment();
        while (true) {
            if (this._inputPtr < this._inputEnd || loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                int i2 = bArr[i] & 255;
                i = inputCodeComment[i2];
                if (i != 0) {
                    switch (i) {
                        case 2:
                            _skipUtf8_2(i2);
                            continue;
                        case 3:
                            _skipUtf8_3(i2);
                            continue;
                        case 4:
                            _skipUtf8_4(i2);
                            continue;
                        case 10:
                            this._currInputRow++;
                            this._currInputRowStart = this._inputPtr;
                            continue;
                        case 13:
                            _skipCR();
                            continue;
                        case 42:
                            if (this._inputPtr >= this._inputEnd && !loadMore()) {
                                break;
                            } else if (this._inputBuffer[this._inputPtr] == (byte) 47) {
                                this._inputPtr++;
                                return;
                            } else {
                                continue;
                            }
                            break;
                        default:
                            _reportInvalidChar(i2);
                            continue;
                    }
                }
            }
            _reportInvalidEOF(" in a comment");
            return;
        }
    }

    private final int _skipColon() throws IOException {
        if (this._inputPtr + 4 >= this._inputEnd) {
            return _skipColon2(false);
        }
        byte b = this._inputBuffer[this._inputPtr];
        byte[] bArr;
        int i;
        if (b == (byte) 58) {
            bArr = this._inputBuffer;
            i = this._inputPtr + 1;
            this._inputPtr = i;
            b = bArr[i];
            if (b <= (byte) 32) {
                if (b == (byte) 32 || b == (byte) 9) {
                    bArr = this._inputBuffer;
                    i = this._inputPtr + 1;
                    this._inputPtr = i;
                    b = bArr[i];
                    if (b > (byte) 32) {
                        if (b == (byte) 47 || b == (byte) 35) {
                            return _skipColon2(true);
                        }
                        this._inputPtr++;
                        return b;
                    }
                }
                return _skipColon2(true);
            } else if (b == (byte) 47 || b == (byte) 35) {
                return _skipColon2(true);
            } else {
                this._inputPtr++;
                return b;
            }
        }
        if (b == (byte) 32 || b == (byte) 9) {
            bArr = this._inputBuffer;
            i = this._inputPtr + 1;
            this._inputPtr = i;
            b = bArr[i];
        }
        if (b != (byte) 58) {
            return _skipColon2(false);
        }
        bArr = this._inputBuffer;
        i = this._inputPtr + 1;
        this._inputPtr = i;
        b = bArr[i];
        if (b <= (byte) 32) {
            if (b == (byte) 32 || b == (byte) 9) {
                bArr = this._inputBuffer;
                i = this._inputPtr + 1;
                this._inputPtr = i;
                b = bArr[i];
                if (b > (byte) 32) {
                    if (b == (byte) 47 || b == (byte) 35) {
                        return _skipColon2(true);
                    }
                    this._inputPtr++;
                    return b;
                }
            }
            return _skipColon2(true);
        } else if (b == (byte) 47 || b == (byte) 35) {
            return _skipColon2(true);
        } else {
            this._inputPtr++;
            return b;
        }
    }

    private final int _skipColon2(boolean z) throws IOException {
        while (true) {
            if (this._inputPtr < this._inputEnd || loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                int i2 = bArr[i] & 255;
                if (i2 > 32) {
                    if (i2 == 47) {
                        _skipComment();
                    } else if (i2 != 35 || !_skipYAMLComment()) {
                        if (z) {
                            return i2;
                        }
                        if (i2 != 58) {
                            if (i2 < 32) {
                                _throwInvalidSpace(i2);
                            }
                            _reportUnexpectedChar(i2, "was expecting a colon to separate field name and value");
                        }
                        z = true;
                    }
                } else if (i2 != 32) {
                    if (i2 == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (i2 == 13) {
                        _skipCR();
                    } else if (i2 != 9) {
                        _throwInvalidSpace(i2);
                    }
                }
            } else {
                throw _constructError("Unexpected end-of-input within/between " + this._parsingContext.getTypeDesc() + " entries");
            }
        }
    }

    private final int _skipColonFast(int i) throws IOException {
        int i2 = i + 1;
        byte b = this._inputBuffer[i];
        int i3;
        byte b2;
        if (b == (byte) 58) {
            i3 = i2 + 1;
            b2 = this._inputBuffer[i2];
            if (b2 > (byte) 32) {
                if (!(b2 == (byte) 47 || b2 == (byte) 35)) {
                    this._inputPtr = i3;
                    return b2;
                }
            } else if (b2 == (byte) 32 || b2 == (byte) 9) {
                i2 = i3 + 1;
                b = this._inputBuffer[i3];
                if (b <= (byte) 32 || b == (byte) 47 || b == (byte) 35) {
                    i3 = i2;
                } else {
                    this._inputPtr = i2;
                    return b;
                }
            }
            this._inputPtr = i3 - 1;
            return _skipColon2(true);
        }
        if (b == (byte) 32 || b == (byte) 9) {
            b = this._inputBuffer[i2];
            i2++;
        }
        if (b == (byte) 58) {
            i3 = i2 + 1;
            b2 = this._inputBuffer[i2];
            if (b2 > (byte) 32) {
                if (!(b2 == (byte) 47 || b2 == (byte) 35)) {
                    this._inputPtr = i3;
                    return b2;
                }
            } else if (b2 == (byte) 32 || b2 == (byte) 9) {
                i2 = i3 + 1;
                b = this._inputBuffer[i3];
                if (b <= (byte) 32 || b == (byte) 47 || b == (byte) 35) {
                    i3 = i2;
                } else {
                    this._inputPtr = i2;
                    return b;
                }
            }
            this._inputPtr = i3 - 1;
            return _skipColon2(true);
        }
        this._inputPtr = i2 - 1;
        return _skipColon2(false);
    }

    private final void _skipComment() throws IOException {
        if (!isEnabled(Feature.ALLOW_COMMENTS)) {
            _reportUnexpectedChar(47, "maybe a (non-standard) comment? (not recognized as one since Feature 'ALLOW_COMMENTS' not enabled for parser)");
        }
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            _reportInvalidEOF(" in a comment");
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        int i2 = bArr[i] & 255;
        if (i2 == 47) {
            _skipLine();
        } else if (i2 == 42) {
            _skipCComment();
        } else {
            _reportUnexpectedChar(i2, "was expecting either '*' or '/' for a comment");
        }
    }

    private final void _skipLine() throws IOException {
        int[] inputCodeComment = CharTypes.getInputCodeComment();
        while (true) {
            if (this._inputPtr < this._inputEnd || loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i = this._inputPtr;
                this._inputPtr = i + 1;
                int i2 = bArr[i] & 255;
                i = inputCodeComment[i2];
                if (i != 0) {
                    switch (i) {
                        case 2:
                            _skipUtf8_2(i2);
                            break;
                        case 3:
                            _skipUtf8_3(i2);
                            break;
                        case 4:
                            _skipUtf8_4(i2);
                            break;
                        case 10:
                            this._currInputRow++;
                            this._currInputRowStart = this._inputPtr;
                            return;
                        case 13:
                            _skipCR();
                            return;
                        case 42:
                            break;
                        default:
                            if (i >= 0) {
                                break;
                            }
                            _reportInvalidChar(i2);
                            break;
                    }
                }
            } else {
                return;
            }
        }
    }

    private final void _skipUtf8_2(int i) throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
    }

    private final void _skipUtf8_3(int i) throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        bArr = this._inputBuffer;
        i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
    }

    private final void _skipUtf8_4(int i) throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        bArr = this._inputBuffer;
        i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        bArr = this._inputBuffer;
        i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        b = bArr[i2];
        if ((b & 192) != 128) {
            _reportInvalidOther(b & 255, this._inputPtr);
        }
    }

    private final int _skipWS() throws IOException {
        while (this._inputPtr < this._inputEnd) {
            byte[] bArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            int i2 = bArr[i] & 255;
            if (i2 > 32) {
                if (i2 != 47 && i2 != 35) {
                    return i2;
                }
                this._inputPtr--;
                return _skipWS2();
            } else if (i2 != 32) {
                if (i2 == 10) {
                    this._currInputRow++;
                    this._currInputRowStart = this._inputPtr;
                } else if (i2 == 13) {
                    _skipCR();
                } else if (i2 != 9) {
                    _throwInvalidSpace(i2);
                }
            }
        }
        return _skipWS2();
    }

    private final int _skipWS2() throws IOException {
        int i;
        while (true) {
            if (this._inputPtr < this._inputEnd || loadMore()) {
                byte[] bArr = this._inputBuffer;
                int i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                i = bArr[i2] & 255;
                if (i > 32) {
                    if (i == 47) {
                        _skipComment();
                    } else if (i != 35 || !_skipYAMLComment()) {
                        return i;
                    }
                } else if (i != 32) {
                    if (i == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (i == 13) {
                        _skipCR();
                    } else if (i != 9) {
                        _throwInvalidSpace(i);
                    }
                }
            } else {
                throw _constructError("Unexpected end-of-input within/between " + this._parsingContext.getTypeDesc() + " entries");
            }
        }
        return i;
    }

    private final int _skipWSOrEnd() throws IOException {
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            return _eofAsNextChar();
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        int i2 = bArr[i] & 255;
        if (i2 <= 32) {
            if (i2 != 32) {
                if (i2 == 10) {
                    this._currInputRow++;
                    this._currInputRowStart = this._inputPtr;
                } else if (i2 == 13) {
                    _skipCR();
                } else if (i2 != 9) {
                    _throwInvalidSpace(i2);
                }
            }
            while (this._inputPtr < this._inputEnd) {
                bArr = this._inputBuffer;
                i = this._inputPtr;
                this._inputPtr = i + 1;
                i2 = bArr[i] & 255;
                if (i2 > 32) {
                    if (i2 != 47 && i2 != 35) {
                        return i2;
                    }
                    this._inputPtr--;
                    return _skipWSOrEnd2();
                } else if (i2 != 32) {
                    if (i2 == 10) {
                        this._currInputRow++;
                        this._currInputRowStart = this._inputPtr;
                    } else if (i2 == 13) {
                        _skipCR();
                    } else if (i2 != 9) {
                        _throwInvalidSpace(i2);
                    }
                }
            }
            return _skipWSOrEnd2();
        } else if (i2 != 47 && i2 != 35) {
            return i2;
        } else {
            this._inputPtr--;
            return _skipWSOrEnd2();
        }
    }

    private final int _skipWSOrEnd2() throws IOException {
        while (true) {
            if (this._inputPtr >= this._inputEnd && !loadMore()) {
                return _eofAsNextChar();
            }
            byte[] bArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            int i2 = bArr[i] & 255;
            if (i2 > 32) {
                if (i2 == 47) {
                    _skipComment();
                } else if (i2 != 35 || !_skipYAMLComment()) {
                    return i2;
                }
            } else if (i2 != 32) {
                if (i2 == 10) {
                    this._currInputRow++;
                    this._currInputRowStart = this._inputPtr;
                } else if (i2 == 13) {
                    _skipCR();
                } else if (i2 != 9) {
                    _throwInvalidSpace(i2);
                }
            }
        }
    }

    private final boolean _skipYAMLComment() throws IOException {
        if (!isEnabled(Feature.ALLOW_YAML_COMMENTS)) {
            return false;
        }
        _skipLine();
        return true;
    }

    private final void _updateLocation() {
        this._tokenInputRow = this._currInputRow;
        int i = this._inputPtr;
        this._tokenInputTotal = this._currInputProcessed + ((long) i);
        this._tokenInputCol = i - this._currInputRowStart;
    }

    private final void _updateNameLocation() {
        this._nameStartRow = this._currInputRow;
        int i = this._inputPtr;
        this._nameStartOffset = i;
        this._nameStartCol = i - this._currInputRowStart;
    }

    private final int _verifyNoLeadingZeroes() throws IOException {
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            return 48;
        }
        int i = this._inputBuffer[this._inputPtr] & 255;
        if (i < 48 || i > 57) {
            return 48;
        }
        if (!isEnabled(Feature.ALLOW_NUMERIC_LEADING_ZEROS)) {
            reportInvalidNumber("Leading zeroes not allowed");
        }
        this._inputPtr++;
        if (i == 48) {
            do {
                if (this._inputPtr < this._inputEnd || loadMore()) {
                    i = this._inputBuffer[this._inputPtr] & 255;
                    if (i < 48 || i > 57) {
                        return 48;
                    }
                    this._inputPtr++;
                }
            } while (i == 48);
            return i;
        }
        return i;
    }

    private final void _verifyRootSpace(int i) throws IOException {
        this._inputPtr++;
        switch (i) {
            case 9:
            case 32:
                return;
            case 10:
                this._currInputRow++;
                this._currInputRowStart = this._inputPtr;
                return;
            case 13:
                _skipCR();
                return;
            default:
                _reportMissingRootWS(i);
                return;
        }
    }

    private final String addName(int[] iArr, int i, int i2) throws JsonParseException {
        int i3;
        int i4 = ((i << 2) - 4) + i2;
        if (i2 < 4) {
            i3 = iArr[i - 1];
            iArr[i - 1] = i3 << ((4 - i2) << 3);
        } else {
            i3 = 0;
        }
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int i5 = 0;
        int i6 = 0;
        while (i6 < i4) {
            int i7;
            int i8 = (iArr[i6 >> 2] >> ((3 - (i6 & 3)) << 3)) & 255;
            i6++;
            if (i8 > TransportMediator.KEYCODE_MEDIA_PAUSE) {
                if ((i8 & 224) == 192) {
                    i8 &= 31;
                    i7 = 1;
                } else if ((i8 & 240) == 224) {
                    i8 &= 15;
                    i7 = 2;
                } else if ((i8 & 248) == 240) {
                    i8 &= 7;
                    i7 = 3;
                } else {
                    _reportInvalidInitial(i8);
                    i8 = 1;
                    i7 = 1;
                }
                if (i6 + i7 > i4) {
                    _reportInvalidEOF(" in field name");
                }
                int i9 = iArr[i6 >> 2] >> ((3 - (i6 & 3)) << 3);
                i6++;
                if ((i9 & 192) != 128) {
                    _reportInvalidOther(i9);
                }
                i8 = (i8 << 6) | (i9 & 63);
                if (i7 > 1) {
                    i9 = iArr[i6 >> 2] >> ((3 - (i6 & 3)) << 3);
                    i6++;
                    if ((i9 & 192) != 128) {
                        _reportInvalidOther(i9);
                    }
                    i8 = (i8 << 6) | (i9 & 63);
                    if (i7 > 2) {
                        i9 = iArr[i6 >> 2] >> ((3 - (i6 & 3)) << 3);
                        i6++;
                        if ((i9 & 192) != 128) {
                            _reportInvalidOther(i9 & 255);
                        }
                        i8 = (i8 << 6) | (i9 & 63);
                    }
                }
                if (i7 > 2) {
                    i7 = i8 - 65536;
                    if (i5 >= emptyAndGetCurrentSegment.length) {
                        emptyAndGetCurrentSegment = this._textBuffer.expandCurrentSegment();
                    }
                    emptyAndGetCurrentSegment[i5] = (char) (GeneratorBase.SURR1_FIRST + (i7 >> 10));
                    i8 = i5 + 1;
                    i5 = i6;
                    i6 = (i7 & Place.TYPE_SUBLOCALITY_LEVEL_1) | GeneratorBase.SURR2_FIRST;
                    if (i8 >= emptyAndGetCurrentSegment.length) {
                        emptyAndGetCurrentSegment = this._textBuffer.expandCurrentSegment();
                    }
                    i7 = i8 + 1;
                    emptyAndGetCurrentSegment[i8] = (char) i6;
                    i6 = i5;
                    i5 = i7;
                }
            }
            int i10 = i5;
            i5 = i6;
            i6 = i8;
            i8 = i10;
            if (i8 >= emptyAndGetCurrentSegment.length) {
                emptyAndGetCurrentSegment = this._textBuffer.expandCurrentSegment();
            }
            i7 = i8 + 1;
            emptyAndGetCurrentSegment[i8] = (char) i6;
            i6 = i5;
            i5 = i7;
        }
        String str = new String(emptyAndGetCurrentSegment, 0, i5);
        if (i2 < 4) {
            iArr[i - 1] = i3;
        }
        return this._symbols.addName(str, iArr, i);
    }

    private final String findName(int i, int i2) throws JsonParseException {
        int pad = pad(i, i2);
        String findName = this._symbols.findName(pad);
        if (findName != null) {
            return findName;
        }
        this._quadBuffer[0] = pad;
        return addName(this._quadBuffer, 1, i2);
    }

    private final String findName(int i, int i2, int i3) throws JsonParseException {
        int pad = pad(i2, i3);
        String findName = this._symbols.findName(i, pad);
        if (findName != null) {
            return findName;
        }
        this._quadBuffer[0] = i;
        this._quadBuffer[1] = pad;
        return addName(this._quadBuffer, 2, i3);
    }

    private final String findName(int i, int i2, int i3, int i4) throws JsonParseException {
        int pad = pad(i3, i4);
        String findName = this._symbols.findName(i, i2, pad);
        if (findName != null) {
            return findName;
        }
        int[] iArr = this._quadBuffer;
        iArr[0] = i;
        iArr[1] = i2;
        iArr[2] = pad(pad, i4);
        return addName(iArr, 3, i4);
    }

    private final String findName(int[] iArr, int i, int i2, int i3) throws JsonParseException {
        if (i >= iArr.length) {
            iArr = growArrayBy(iArr, iArr.length);
            this._quadBuffer = iArr;
        }
        int i4 = i + 1;
        iArr[i] = pad(i2, i3);
        String findName = this._symbols.findName(iArr, i4);
        return findName == null ? addName(iArr, i4, i3) : findName;
    }

    public static int[] growArrayBy(int[] iArr, int i) {
        return iArr == null ? new int[i] : Arrays.copyOf(iArr, iArr.length + i);
    }

    private int nextByte() throws IOException {
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        return bArr[i] & 255;
    }

    private static final int pad(int i, int i2) {
        return i2 == 4 ? i : i | (-1 << (i2 << 3));
    }

    private final String parseName(int i, int i2, int i3) throws IOException {
        return parseEscapedName(this._quadBuffer, 0, i, i2, i3);
    }

    private final String parseName(int i, int i2, int i3, int i4) throws IOException {
        this._quadBuffer[0] = i;
        return parseEscapedName(this._quadBuffer, 1, i2, i3, i4);
    }

    private final String parseName(int i, int i2, int i3, int i4, int i5) throws IOException {
        this._quadBuffer[0] = i;
        this._quadBuffer[1] = i2;
        return parseEscapedName(this._quadBuffer, 2, i3, i4, i5);
    }

    protected void _closeInput() throws IOException {
        if (this._inputStream != null) {
            if (this._ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_SOURCE)) {
                this._inputStream.close();
            }
            this._inputStream = null;
        }
    }

    protected final byte[] _decodeBase64(Base64Variant base64Variant) throws IOException {
        ByteArrayBuilder _getByteArrayBuilder = _getByteArrayBuilder();
        while (true) {
            if (this._inputPtr >= this._inputEnd) {
                loadMoreGuaranteed();
            }
            byte[] bArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            i = bArr[i] & 255;
            if (i > 32) {
                int decodeBase64Char = base64Variant.decodeBase64Char(i);
                if (decodeBase64Char < 0) {
                    if (i == 34) {
                        return _getByteArrayBuilder.toByteArray();
                    }
                    decodeBase64Char = _decodeBase64Escape(base64Variant, i, 0);
                    if (decodeBase64Char < 0) {
                        continue;
                    }
                }
                if (this._inputPtr >= this._inputEnd) {
                    loadMoreGuaranteed();
                }
                byte[] bArr2 = this._inputBuffer;
                int i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                i2 = bArr2[i2] & 255;
                i = base64Variant.decodeBase64Char(i2);
                if (i < 0) {
                    i = _decodeBase64Escape(base64Variant, i2, 1);
                }
                i |= decodeBase64Char << 6;
                if (this._inputPtr >= this._inputEnd) {
                    loadMoreGuaranteed();
                }
                bArr = this._inputBuffer;
                i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                i2 = bArr[i2] & 255;
                decodeBase64Char = base64Variant.decodeBase64Char(i2);
                if (decodeBase64Char < 0) {
                    if (decodeBase64Char != -2) {
                        if (i2 != 34 || base64Variant.usesPadding()) {
                            decodeBase64Char = _decodeBase64Escape(base64Variant, i2, 2);
                        } else {
                            _getByteArrayBuilder.append(i >> 4);
                            return _getByteArrayBuilder.toByteArray();
                        }
                    }
                    if (decodeBase64Char == -2) {
                        if (this._inputPtr >= this._inputEnd) {
                            loadMoreGuaranteed();
                        }
                        bArr = this._inputBuffer;
                        i2 = this._inputPtr;
                        this._inputPtr = i2 + 1;
                        decodeBase64Char = bArr[i2] & 255;
                        if (base64Variant.usesPaddingChar(decodeBase64Char)) {
                            _getByteArrayBuilder.append(i >> 4);
                        } else {
                            throw reportInvalidBase64Char(base64Variant, decodeBase64Char, 3, "expected padding character '" + base64Variant.getPaddingChar() + "'");
                        }
                    }
                }
                i = (i << 6) | decodeBase64Char;
                if (this._inputPtr >= this._inputEnd) {
                    loadMoreGuaranteed();
                }
                bArr = this._inputBuffer;
                i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                i2 = bArr[i2] & 255;
                decodeBase64Char = base64Variant.decodeBase64Char(i2);
                if (decodeBase64Char < 0) {
                    if (decodeBase64Char != -2) {
                        if (i2 != 34 || base64Variant.usesPadding()) {
                            decodeBase64Char = _decodeBase64Escape(base64Variant, i2, 3);
                        } else {
                            _getByteArrayBuilder.appendTwoBytes(i >> 2);
                            return _getByteArrayBuilder.toByteArray();
                        }
                    }
                    if (decodeBase64Char == -2) {
                        _getByteArrayBuilder.appendTwoBytes(i >> 2);
                    }
                }
                _getByteArrayBuilder.appendThreeBytes(decodeBase64Char | (i << 6));
            }
        }
    }

    protected int _decodeCharForError(int i) throws IOException {
        int i2 = i & 255;
        if (i2 <= TransportMediator.KEYCODE_MEDIA_PAUSE) {
            return i2;
        }
        Object obj;
        int i3;
        if ((i2 & 224) == 192) {
            i2 &= 31;
            obj = 1;
        } else if ((i2 & 240) == 224) {
            i2 &= 15;
            i3 = 2;
        } else if ((i2 & 248) == 240) {
            i2 &= 7;
            obj = 3;
        } else {
            _reportInvalidInitial(i2 & 255);
            i3 = 1;
        }
        int nextByte = nextByte();
        if ((nextByte & 192) != 128) {
            _reportInvalidOther(nextByte & 255);
        }
        i2 = (i2 << 6) | (nextByte & 63);
        if (obj <= 1) {
            return i2;
        }
        int nextByte2 = nextByte();
        if ((nextByte2 & 192) != 128) {
            _reportInvalidOther(nextByte2 & 255);
        }
        i2 = (i2 << 6) | (nextByte2 & 63);
        if (obj <= 2) {
            return i2;
        }
        i3 = nextByte();
        if ((i3 & 192) != 128) {
            _reportInvalidOther(i3 & 255);
        }
        return (i2 << 6) | (i3 & 63);
    }

    protected char _decodeEscaped() throws IOException {
        int i = 0;
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            _reportInvalidEOF(" in character escape sequence");
        }
        byte[] bArr = this._inputBuffer;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        byte b = bArr[i2];
        switch (b) {
            case (byte) 34:
            case (byte) 47:
            case (byte) 92:
                return (char) b;
            case (byte) 98:
                return '\b';
            case (byte) 102:
                return '\f';
            case (byte) 110:
                return '\n';
            case (byte) 114:
                return '\r';
            case (byte) 116:
                return '\t';
            case (byte) 117:
                for (int i3 = 0; i3 < 4; i3++) {
                    if (this._inputPtr >= this._inputEnd && !loadMore()) {
                        _reportInvalidEOF(" in character escape sequence");
                    }
                    byte[] bArr2 = this._inputBuffer;
                    int i4 = this._inputPtr;
                    this._inputPtr = i4 + 1;
                    byte b2 = bArr2[i4];
                    i4 = CharTypes.charToHex(b2);
                    if (i4 < 0) {
                        _reportUnexpectedChar(b2, "expected a hex-digit for character escape sequence");
                    }
                    i = (i << 4) | i4;
                }
                return (char) i;
            default:
                return _handleUnrecognizedCharacterEscape((char) _decodeCharForError(b));
        }
    }

    protected String _finishAndReturnString() throws IOException {
        int i;
        int i2 = this._inputPtr;
        if (i2 >= this._inputEnd) {
            loadMoreGuaranteed();
            i2 = this._inputPtr;
        }
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = _icUTF8;
        int min = Math.min(this._inputEnd, emptyAndGetCurrentSegment.length + i2);
        byte[] bArr = this._inputBuffer;
        i2 = 0;
        for (i = i2; i < min; i++) {
            int i3 = bArr[i] & 255;
            if (iArr[i3] != 0) {
                if (i3 == 34) {
                    this._inputPtr = i + 1;
                    return this._textBuffer.setCurrentAndReturn(i2);
                }
                this._inputPtr = i;
                _finishString2(emptyAndGetCurrentSegment, i2);
                return this._textBuffer.contentsAsString();
            }
            emptyAndGetCurrentSegment[i2] = (char) i3;
            i2++;
        }
        this._inputPtr = i;
        _finishString2(emptyAndGetCurrentSegment, i2);
        return this._textBuffer.contentsAsString();
    }

    protected void _finishString() throws IOException {
        int i;
        int i2 = this._inputPtr;
        if (i2 >= this._inputEnd) {
            loadMoreGuaranteed();
            i2 = this._inputPtr;
        }
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = _icUTF8;
        int min = Math.min(this._inputEnd, emptyAndGetCurrentSegment.length + i2);
        byte[] bArr = this._inputBuffer;
        i2 = 0;
        for (i = i2; i < min; i++) {
            int i3 = bArr[i] & 255;
            if (iArr[i3] != 0) {
                if (i3 == 34) {
                    this._inputPtr = i + 1;
                    this._textBuffer.setCurrentLength(i2);
                    return;
                }
                this._inputPtr = i;
                _finishString2(emptyAndGetCurrentSegment, i2);
            }
            emptyAndGetCurrentSegment[i2] = (char) i3;
            i2++;
        }
        this._inputPtr = i;
        _finishString2(emptyAndGetCurrentSegment, i2);
    }

    protected final String _getText2(JsonToken jsonToken) {
        if (jsonToken == null) {
            return null;
        }
        switch (jsonToken.id()) {
            case 5:
                return this._parsingContext.getCurrentName();
            case 6:
            case 7:
            case 8:
                return this._textBuffer.contentsAsString();
            default:
                return jsonToken.asString();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected com.fasterxml.jackson.core.JsonToken _handleApos() throws java.io.IOException {
        /*
        r10 = this;
        r9 = 39;
        r2 = 0;
        r0 = r10._textBuffer;
        r0 = r0.emptyAndGetCurrentSegment();
        r5 = _icUTF8;
        r6 = r10._inputBuffer;
        r1 = r2;
    L_0x000e:
        r3 = r10._inputPtr;
        r4 = r10._inputEnd;
        if (r3 < r4) goto L_0x0017;
    L_0x0014:
        r10.loadMoreGuaranteed();
    L_0x0017:
        r3 = r0.length;
        if (r1 < r3) goto L_0x0021;
    L_0x001a:
        r0 = r10._textBuffer;
        r0 = r0.finishCurrentSegment();
        r1 = r2;
    L_0x0021:
        r4 = r10._inputEnd;
        r3 = r10._inputPtr;
        r7 = r0.length;
        r7 = r7 - r1;
        r3 = r3 + r7;
        if (r3 >= r4) goto L_0x00b2;
    L_0x002a:
        r4 = r10._inputPtr;
        if (r4 >= r3) goto L_0x000e;
    L_0x002e:
        r4 = r10._inputPtr;
        r7 = r4 + 1;
        r10._inputPtr = r7;
        r4 = r6[r4];
        r4 = r4 & 255;
        if (r4 == r9) goto L_0x0044;
    L_0x003a:
        r7 = r5[r4];
        if (r7 != 0) goto L_0x0044;
    L_0x003e:
        r4 = (char) r4;
        r0[r1] = r4;
        r1 = r1 + 1;
        goto L_0x002a;
    L_0x0044:
        if (r4 == r9) goto L_0x00a6;
    L_0x0046:
        r3 = r5[r4];
        switch(r3) {
            case 1: goto L_0x0068;
            case 2: goto L_0x006f;
            case 3: goto L_0x0074;
            case 4: goto L_0x0086;
            default: goto L_0x004b;
        };
    L_0x004b:
        r3 = 32;
        if (r4 >= r3) goto L_0x0054;
    L_0x004f:
        r3 = "string value";
        r10._throwUnquotedSpace(r4, r3);
    L_0x0054:
        r10._reportInvalidChar(r4);
    L_0x0057:
        r3 = r4;
    L_0x0058:
        r4 = r0.length;
        if (r1 < r4) goto L_0x00ae;
    L_0x005b:
        r0 = r10._textBuffer;
        r0 = r0.finishCurrentSegment();
        r4 = r2;
    L_0x0062:
        r1 = r4 + 1;
        r3 = (char) r3;
        r0[r4] = r3;
        goto L_0x000e;
    L_0x0068:
        if (r4 == r9) goto L_0x0057;
    L_0x006a:
        r3 = r10._decodeEscaped();
        goto L_0x0058;
    L_0x006f:
        r3 = r10._decodeUtf8_2(r4);
        goto L_0x0058;
    L_0x0074:
        r3 = r10._inputEnd;
        r7 = r10._inputPtr;
        r3 = r3 - r7;
        r7 = 2;
        if (r3 < r7) goto L_0x0081;
    L_0x007c:
        r3 = r10._decodeUtf8_3fast(r4);
        goto L_0x0058;
    L_0x0081:
        r3 = r10._decodeUtf8_3(r4);
        goto L_0x0058;
    L_0x0086:
        r4 = r10._decodeUtf8_4(r4);
        r3 = r1 + 1;
        r7 = 55296; // 0xd800 float:7.7486E-41 double:2.732E-319;
        r8 = r4 >> 10;
        r7 = r7 | r8;
        r7 = (char) r7;
        r0[r1] = r7;
        r1 = r0.length;
        if (r3 < r1) goto L_0x00b0;
    L_0x0098:
        r0 = r10._textBuffer;
        r0 = r0.finishCurrentSegment();
        r1 = r2;
    L_0x009f:
        r3 = 56320; // 0xdc00 float:7.8921E-41 double:2.7826E-319;
        r4 = r4 & 1023;
        r3 = r3 | r4;
        goto L_0x0058;
    L_0x00a6:
        r0 = r10._textBuffer;
        r0.setCurrentLength(r1);
        r0 = com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
        return r0;
    L_0x00ae:
        r4 = r1;
        goto L_0x0062;
    L_0x00b0:
        r1 = r3;
        goto L_0x009f;
    L_0x00b2:
        r3 = r4;
        goto L_0x002a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8StreamJsonParser._handleApos():com.fasterxml.jackson.core.JsonToken");
    }

    protected JsonToken _handleInvalidNumberStart(int i, boolean z) throws IOException {
        int i2 = i;
        while (i2 == 73) {
            String str;
            if (this._inputPtr >= this._inputEnd && !loadMore()) {
                _reportInvalidEOFInValue();
            }
            byte[] bArr = this._inputBuffer;
            int i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            byte b = bArr[i3];
            if (b != (byte) 78) {
                if (b != (byte) 110) {
                    i2 = b;
                    break;
                }
                str = z ? "-Infinity" : "+Infinity";
            } else {
                str = z ? "-INF" : "+INF";
            }
            _matchToken(str, 3);
            if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                return resetAsNaN(str, z ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            }
            _reportError("Non-standard token '" + str + "': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
            byte b2 = b;
        }
        reportUnexpectedNumberChar(i2, "expected digit (0-9) to follow minus sign, for valid numeric value");
        return null;
    }

    protected String _handleOddName(int i) throws IOException {
        int i2 = 0;
        if (i == 39 && isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return _parseAposName();
        }
        if (!isEnabled(Feature.ALLOW_UNQUOTED_FIELD_NAMES)) {
            _reportUnexpectedChar((char) _decodeCharForError(i), "was expecting double-quote to start field name");
        }
        int[] inputCodeUtf8JsNames = CharTypes.getInputCodeUtf8JsNames();
        if (inputCodeUtf8JsNames[i] != 0) {
            _reportUnexpectedChar(i, "was expecting either valid name character (for unquoted name) or double-quote (for quoted) to start field name");
        }
        int[] iArr = this._quadBuffer;
        int i3 = 0;
        int i4 = i;
        int i5 = 0;
        while (true) {
            if (i2 < 4) {
                i4 |= i3 << 8;
                i2++;
            } else {
                if (i5 >= iArr.length) {
                    iArr = growArrayBy(iArr, iArr.length);
                    this._quadBuffer = iArr;
                }
                iArr[i5] = i3;
                i2 = 1;
                i5++;
            }
            if (this._inputPtr >= this._inputEnd && !loadMore()) {
                _reportInvalidEOF(" in field name");
            }
            i = this._inputBuffer[this._inputPtr] & 255;
            if (inputCodeUtf8JsNames[i] != 0) {
                break;
            }
            this._inputPtr++;
            i3 = i4;
            i4 = i;
        }
        if (i2 > 0) {
            if (i5 >= iArr.length) {
                iArr = growArrayBy(iArr, iArr.length);
                this._quadBuffer = iArr;
            }
            iArr[i5] = i4;
            i5++;
        }
        String findName = this._symbols.findName(iArr, i5);
        return findName == null ? addName(iArr, i5, i2) : findName;
    }

    protected JsonToken _handleUnexpectedValue(int i) throws IOException {
        switch (i) {
            case 39:
                break;
            case 43:
                if (this._inputPtr >= this._inputEnd && !loadMore()) {
                    _reportInvalidEOFInValue();
                }
                byte[] bArr = this._inputBuffer;
                int i2 = this._inputPtr;
                this._inputPtr = i2 + 1;
                return _handleInvalidNumberStart(bArr[i2] & 255, false);
            case 73:
                _matchToken("Infinity", 1);
                if (!isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    _reportError("Non-standard token 'Infinity': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                    break;
                }
                return resetAsNaN("Infinity", Double.POSITIVE_INFINITY);
            case 78:
                _matchToken("NaN", 1);
                if (!isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                    _reportError("Non-standard token 'NaN': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
                    break;
                }
                return resetAsNaN("NaN", Double.NaN);
            case 93:
            case 125:
                _reportUnexpectedChar(i, "expected a value");
                break;
        }
        if (isEnabled(Feature.ALLOW_SINGLE_QUOTES)) {
            return _handleApos();
        }
        if (Character.isJavaIdentifierStart(i)) {
            _reportInvalidToken(((char) i), "('true', 'false' or 'null')");
        }
        _reportUnexpectedChar(i, "expected a valid value (number, String, array, object, 'true', 'false' or 'null')");
        return null;
    }

    protected final boolean _loadToHaveAtLeast(int i) throws IOException {
        if (this._inputStream == null) {
            return false;
        }
        int i2 = this._inputEnd - this._inputPtr;
        if (i2 <= 0 || this._inputPtr <= 0) {
            this._inputEnd = 0;
        } else {
            int i3 = this._inputPtr;
            this._currInputProcessed += (long) i3;
            this._currInputRowStart -= i3;
            this._nameStartOffset -= i3;
            System.arraycopy(this._inputBuffer, i3, this._inputBuffer, 0, i2);
            this._inputEnd = i2;
        }
        this._inputPtr = 0;
        while (this._inputEnd < i) {
            i3 = this._inputStream.read(this._inputBuffer, this._inputEnd, this._inputBuffer.length - this._inputEnd);
            if (i3 <= 0) {
                _closeInput();
                if (i3 != 0) {
                    return false;
                }
                throw new IOException("InputStream.read() returned 0 characters when trying to read " + i2 + " bytes");
            }
            this._inputEnd = i3 + this._inputEnd;
        }
        return true;
    }

    protected final void _matchToken(String str, int i) throws IOException {
        int length = str.length();
        if (this._inputPtr + length >= this._inputEnd) {
            _matchToken2(str, i);
            return;
        }
        do {
            if (this._inputBuffer[this._inputPtr] != str.charAt(i)) {
                _reportInvalidToken(str.substring(0, i));
            }
            this._inputPtr++;
            i++;
        } while (i < length);
        length = this._inputBuffer[this._inputPtr] & 255;
        if (length >= 48 && length != 93 && length != 125) {
            _checkMatchEnd(str, i, length);
        }
    }

    protected String _parseAposName() throws IOException {
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            _reportInvalidEOF(": was expecting closing ''' for name");
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        int i2 = bArr[i] & 255;
        if (i2 == 39) {
            return "";
        }
        int[] iArr = this._quadBuffer;
        int[] iArr2 = _icLatin1;
        int i3 = 0;
        int i4 = 0;
        i = 0;
        while (i2 != 39) {
            int i5;
            int i6;
            byte[] bArr2;
            if (!(i2 == 34 || iArr2[i2] == 0)) {
                if (i2 != 92) {
                    _throwUnquotedSpace(i2, "name");
                    i5 = i2;
                } else {
                    i5 = _decodeEscaped();
                }
                if (i5 > TransportMediator.KEYCODE_MEDIA_PAUSE) {
                    if (i4 >= 4) {
                        if (i >= iArr.length) {
                            iArr = growArrayBy(iArr, iArr.length);
                            this._quadBuffer = iArr;
                        }
                        iArr[i] = i3;
                        i3 = i + 1;
                        i = 0;
                        i4 = 0;
                    } else {
                        i6 = i3;
                        i3 = i;
                        i = i6;
                    }
                    if (i5 < 2048) {
                        i = (i << 8) | ((i5 >> 6) | 192);
                        i2 = i4 + 1;
                    } else {
                        i = (i << 8) | ((i5 >> 12) | 224);
                        i2 = i4 + 1;
                        if (i2 >= 4) {
                            if (i3 >= iArr.length) {
                                iArr = growArrayBy(iArr, iArr.length);
                                this._quadBuffer = iArr;
                            }
                            iArr[i3] = i;
                            i3++;
                            i = 0;
                            i2 = 0;
                        }
                        i = (i << 8) | (((i5 >> 6) & 63) | 128);
                        i2++;
                    }
                    i6 = i;
                    i = (i5 & 63) | 128;
                    i5 = i2;
                    i2 = i3;
                    i3 = i6;
                    if (i5 >= 4) {
                        i |= i3 << 8;
                        i3 = i5 + 1;
                    } else {
                        if (i2 >= iArr.length) {
                            iArr = growArrayBy(iArr, iArr.length);
                            this._quadBuffer = iArr;
                        }
                        iArr[i2] = i3;
                        i3 = 1;
                        i2++;
                    }
                    if (this._inputPtr >= this._inputEnd && !loadMore()) {
                        _reportInvalidEOF(" in field name");
                    }
                    bArr2 = this._inputBuffer;
                    i4 = this._inputPtr;
                    this._inputPtr = i4 + 1;
                    i4 = i3;
                    i3 = i;
                    i = i2;
                    i2 = bArr2[i4] & 255;
                } else {
                    i2 = i5;
                }
            }
            i5 = i4;
            i6 = i2;
            i2 = i;
            i = i6;
            if (i5 >= 4) {
                if (i2 >= iArr.length) {
                    iArr = growArrayBy(iArr, iArr.length);
                    this._quadBuffer = iArr;
                }
                iArr[i2] = i3;
                i3 = 1;
                i2++;
            } else {
                i |= i3 << 8;
                i3 = i5 + 1;
            }
            _reportInvalidEOF(" in field name");
            bArr2 = this._inputBuffer;
            i4 = this._inputPtr;
            this._inputPtr = i4 + 1;
            i4 = i3;
            i3 = i;
            i = i2;
            i2 = bArr2[i4] & 255;
        }
        if (i4 > 0) {
            if (i >= iArr.length) {
                iArr = growArrayBy(iArr, iArr.length);
                this._quadBuffer = iArr;
            }
            iArr[i] = pad(i3, i4);
            i++;
        }
        String findName = this._symbols.findName(iArr, i);
        return findName == null ? addName(iArr, i, i4) : findName;
    }

    protected final String _parseName(int i) throws IOException {
        if (i != 34) {
            return _handleOddName(i);
        }
        if (this._inputPtr + 13 > this._inputEnd) {
            return slowParseName();
        }
        byte[] bArr = this._inputBuffer;
        int[] iArr = _icLatin1;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        i2 = bArr[i2] & 255;
        if (iArr[i2] != 0) {
            return i2 == 34 ? "" : parseName(0, i2, 0);
        } else {
            int i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            i3 = bArr[i3] & 255;
            if (iArr[i3] != 0) {
                return i3 == 34 ? findName(i2, 1) : parseName(i2, i3, 1);
            } else {
                i2 = (i2 << 8) | i3;
                i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                i3 = bArr[i3] & 255;
                if (iArr[i3] != 0) {
                    return i3 == 34 ? findName(i2, 2) : parseName(i2, i3, 2);
                } else {
                    i2 = (i2 << 8) | i3;
                    i3 = this._inputPtr;
                    this._inputPtr = i3 + 1;
                    i3 = bArr[i3] & 255;
                    if (iArr[i3] != 0) {
                        return i3 == 34 ? findName(i2, 3) : parseName(i2, i3, 3);
                    } else {
                        i2 = (i2 << 8) | i3;
                        i3 = this._inputPtr;
                        this._inputPtr = i3 + 1;
                        int i4 = bArr[i3] & 255;
                        if (iArr[i4] != 0) {
                            return i4 == 34 ? findName(i2, 4) : parseName(i2, i4, 4);
                        } else {
                            this._quad1 = i2;
                            return parseMediumName(i4);
                        }
                    }
                }
            }
        }
    }

    protected JsonToken _parseNegNumber() throws IOException {
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        emptyAndGetCurrentSegment[0] = '-';
        if (this._inputPtr >= this._inputEnd) {
            loadMoreGuaranteed();
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        int i2 = bArr[i] & 255;
        if (i2 < 48 || i2 > 57) {
            return _handleInvalidNumberStart(i2, true);
        }
        int i3;
        if (i2 == 48) {
            i2 = _verifyNoLeadingZeroes();
        }
        i = 2;
        emptyAndGetCurrentSegment[1] = (char) i2;
        i2 = (this._inputPtr + emptyAndGetCurrentSegment.length) - 2;
        if (i2 > this._inputEnd) {
            i2 = this._inputEnd;
            i3 = 1;
        } else {
            i3 = 1;
        }
        while (this._inputPtr < i2) {
            byte[] bArr2 = this._inputBuffer;
            int i4 = this._inputPtr;
            this._inputPtr = i4 + 1;
            int i5 = bArr2[i4] & 255;
            if (i5 >= 48 && i5 <= 57) {
                i3++;
                emptyAndGetCurrentSegment[i] = (char) i5;
                i++;
            } else if (i5 == 46 || i5 == 101 || i5 == 69) {
                return _parseFloat(emptyAndGetCurrentSegment, i, i5, true, i3);
            } else {
                this._inputPtr--;
                this._textBuffer.setCurrentLength(i);
                if (this._parsingContext.inRoot()) {
                    _verifyRootSpace(i5);
                }
                return resetInt(true, i3);
            }
        }
        return _parseNumber2(emptyAndGetCurrentSegment, i, true, i3);
    }

    protected JsonToken _parsePosNumber(int i) throws IOException {
        int i2;
        int i3 = 1;
        char[] emptyAndGetCurrentSegment = this._textBuffer.emptyAndGetCurrentSegment();
        if (i == 48) {
            i = _verifyNoLeadingZeroes();
        }
        emptyAndGetCurrentSegment[0] = (char) i;
        int length = (this._inputPtr + emptyAndGetCurrentSegment.length) - 1;
        if (length > this._inputEnd) {
            length = this._inputEnd;
            i2 = 1;
        } else {
            i2 = 1;
        }
        while (this._inputPtr < length) {
            byte[] bArr = this._inputBuffer;
            int i4 = this._inputPtr;
            this._inputPtr = i4 + 1;
            int i5 = bArr[i4] & 255;
            if (i5 >= 48 && i5 <= 57) {
                emptyAndGetCurrentSegment[i3] = (char) i5;
                i3++;
                i2++;
            } else if (i5 == 46 || i5 == 101 || i5 == 69) {
                return _parseFloat(emptyAndGetCurrentSegment, i3, i5, false, i2);
            } else {
                this._inputPtr--;
                this._textBuffer.setCurrentLength(i3);
                if (this._parsingContext.inRoot()) {
                    _verifyRootSpace(i5);
                }
                return resetInt(false, i2);
            }
        }
        return _parseNumber2(emptyAndGetCurrentSegment, i3, false, i2);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected int _readBinary(com.fasterxml.jackson.core.Base64Variant r12, java.io.OutputStream r13, byte[] r14) throws java.io.IOException {
        /*
        r11 = this;
        r10 = 3;
        r9 = 34;
        r8 = -2;
        r1 = 0;
        r5 = r14.length;
        r0 = r1;
        r2 = r1;
    L_0x0008:
        r3 = r11._inputPtr;
        r4 = r11._inputEnd;
        if (r3 < r4) goto L_0x0011;
    L_0x000e:
        r11.loadMoreGuaranteed();
    L_0x0011:
        r3 = r11._inputBuffer;
        r4 = r11._inputPtr;
        r6 = r4 + 1;
        r11._inputPtr = r6;
        r3 = r3[r4];
        r4 = r3 & 255;
        r3 = 32;
        if (r4 <= r3) goto L_0x0008;
    L_0x0021:
        r3 = r12.decodeBase64Char(r4);
        if (r3 >= 0) goto L_0x002f;
    L_0x0027:
        if (r4 == r9) goto L_0x0088;
    L_0x0029:
        r3 = r11._decodeBase64Escape(r12, r4, r1);
        if (r3 < 0) goto L_0x0008;
    L_0x002f:
        r4 = r3;
        r3 = r5 + -3;
        if (r2 <= r3) goto L_0x0140;
    L_0x0034:
        r0 = r0 + r2;
        r13.write(r14, r1, r2);
        r3 = r1;
    L_0x0039:
        r2 = r11._inputPtr;
        r6 = r11._inputEnd;
        if (r2 < r6) goto L_0x0042;
    L_0x003f:
        r11.loadMoreGuaranteed();
    L_0x0042:
        r2 = r11._inputBuffer;
        r6 = r11._inputPtr;
        r7 = r6 + 1;
        r11._inputPtr = r7;
        r2 = r2[r6];
        r6 = r2 & 255;
        r2 = r12.decodeBase64Char(r6);
        if (r2 >= 0) goto L_0x0059;
    L_0x0054:
        r2 = 1;
        r2 = r11._decodeBase64Escape(r12, r6, r2);
    L_0x0059:
        r4 = r4 << 6;
        r4 = r4 | r2;
        r2 = r11._inputPtr;
        r6 = r11._inputEnd;
        if (r2 < r6) goto L_0x0065;
    L_0x0062:
        r11.loadMoreGuaranteed();
    L_0x0065:
        r2 = r11._inputBuffer;
        r6 = r11._inputPtr;
        r7 = r6 + 1;
        r11._inputPtr = r7;
        r2 = r2[r6];
        r6 = r2 & 255;
        r2 = r12.decodeBase64Char(r6);
        if (r2 >= 0) goto L_0x00da;
    L_0x0077:
        if (r2 == r8) goto L_0x0096;
    L_0x0079:
        if (r6 != r9) goto L_0x0091;
    L_0x007b:
        r2 = r12.usesPadding();
        if (r2 != 0) goto L_0x0091;
    L_0x0081:
        r2 = r3 + 1;
        r4 = r4 >> 4;
        r4 = (byte) r4;
        r14[r3] = r4;
    L_0x0088:
        r11._tokenIncomplete = r1;
        if (r2 <= 0) goto L_0x0090;
    L_0x008c:
        r0 = r0 + r2;
        r13.write(r14, r1, r2);
    L_0x0090:
        return r0;
    L_0x0091:
        r2 = 2;
        r2 = r11._decodeBase64Escape(r12, r6, r2);
    L_0x0096:
        if (r2 != r8) goto L_0x00da;
    L_0x0098:
        r2 = r11._inputPtr;
        r6 = r11._inputEnd;
        if (r2 < r6) goto L_0x00a1;
    L_0x009e:
        r11.loadMoreGuaranteed();
    L_0x00a1:
        r2 = r11._inputBuffer;
        r6 = r11._inputPtr;
        r7 = r6 + 1;
        r11._inputPtr = r7;
        r2 = r2[r6];
        r2 = r2 & 255;
        r6 = r12.usesPaddingChar(r2);
        if (r6 != 0) goto L_0x00d1;
    L_0x00b3:
        r0 = new java.lang.StringBuilder;
        r1 = "expected padding character '";
        r0.<init>(r1);
        r1 = r12.getPaddingChar();
        r0 = r0.append(r1);
        r1 = "'";
        r0 = r0.append(r1);
        r0 = r0.toString();
        r0 = r11.reportInvalidBase64Char(r12, r2, r10, r0);
        throw r0;
    L_0x00d1:
        r2 = r3 + 1;
        r4 = r4 >> 4;
        r4 = (byte) r4;
        r14[r3] = r4;
        goto L_0x0008;
    L_0x00da:
        r4 = r4 << 6;
        r4 = r4 | r2;
        r2 = r11._inputPtr;
        r6 = r11._inputEnd;
        if (r2 < r6) goto L_0x00e6;
    L_0x00e3:
        r11.loadMoreGuaranteed();
    L_0x00e6:
        r2 = r11._inputBuffer;
        r6 = r11._inputPtr;
        r7 = r6 + 1;
        r11._inputPtr = r7;
        r2 = r2[r6];
        r6 = r2 & 255;
        r2 = r12.decodeBase64Char(r6);
        if (r2 >= 0) goto L_0x0128;
    L_0x00f8:
        if (r2 == r8) goto L_0x0116;
    L_0x00fa:
        if (r6 != r9) goto L_0x0112;
    L_0x00fc:
        r2 = r12.usesPadding();
        if (r2 != 0) goto L_0x0112;
    L_0x0102:
        r4 = r4 >> 2;
        r5 = r3 + 1;
        r2 = r4 >> 8;
        r2 = (byte) r2;
        r14[r3] = r2;
        r2 = r5 + 1;
        r3 = (byte) r4;
        r14[r5] = r3;
        goto L_0x0088;
    L_0x0112:
        r2 = r11._decodeBase64Escape(r12, r6, r10);
    L_0x0116:
        if (r2 != r8) goto L_0x0128;
    L_0x0118:
        r4 = r4 >> 2;
        r6 = r3 + 1;
        r2 = r4 >> 8;
        r2 = (byte) r2;
        r14[r3] = r2;
        r2 = r6 + 1;
        r3 = (byte) r4;
        r14[r6] = r3;
        goto L_0x0008;
    L_0x0128:
        r4 = r4 << 6;
        r4 = r4 | r2;
        r2 = r3 + 1;
        r6 = r4 >> 16;
        r6 = (byte) r6;
        r14[r3] = r6;
        r3 = r2 + 1;
        r6 = r4 >> 8;
        r6 = (byte) r6;
        r14[r2] = r6;
        r2 = r3 + 1;
        r4 = (byte) r4;
        r14[r3] = r4;
        goto L_0x0008;
    L_0x0140:
        r3 = r2;
        goto L_0x0039;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.json.UTF8StreamJsonParser._readBinary(com.fasterxml.jackson.core.Base64Variant, java.io.OutputStream, byte[]):int");
    }

    protected void _releaseBuffers() throws IOException {
        super._releaseBuffers();
        this._symbols.release();
        if (this._bufferRecyclable) {
            byte[] bArr = this._inputBuffer;
            if (bArr != null) {
                this._inputBuffer = ByteArrayBuilder.NO_BYTES;
                this._ioContext.releaseReadIOBuffer(bArr);
            }
        }
    }

    protected void _reportInvalidChar(int i) throws JsonParseException {
        if (i < 32) {
            _throwInvalidSpace(i);
        }
        _reportInvalidInitial(i);
    }

    protected void _reportInvalidInitial(int i) throws JsonParseException {
        _reportError("Invalid UTF-8 start byte 0x" + Integer.toHexString(i));
    }

    protected void _reportInvalidOther(int i) throws JsonParseException {
        _reportError("Invalid UTF-8 middle byte 0x" + Integer.toHexString(i));
    }

    protected void _reportInvalidOther(int i, int i2) throws JsonParseException {
        this._inputPtr = i2;
        _reportInvalidOther(i);
    }

    protected void _reportInvalidToken(String str) throws IOException {
        _reportInvalidToken(str, "'null', 'true', 'false' or NaN");
    }

    protected void _reportInvalidToken(String str, String str2) throws IOException {
        StringBuilder stringBuilder = new StringBuilder(str);
        while (true) {
            if (this._inputPtr >= this._inputEnd && !loadMore()) {
                break;
            }
            byte[] bArr = this._inputBuffer;
            int i = this._inputPtr;
            this._inputPtr = i + 1;
            char _decodeCharForError = (char) _decodeCharForError(bArr[i]);
            if (!Character.isJavaIdentifierPart(_decodeCharForError)) {
                break;
            }
            stringBuilder.append(_decodeCharForError);
        }
        _reportError("Unrecognized token '" + stringBuilder.toString() + "': was expecting " + str2);
    }

    protected final void _skipCR() throws IOException {
        if ((this._inputPtr < this._inputEnd || loadMore()) && this._inputBuffer[this._inputPtr] == BYTE_LF) {
            this._inputPtr++;
        }
        this._currInputRow++;
        this._currInputRowStart = this._inputPtr;
    }

    protected void _skipString() throws IOException {
        this._tokenIncomplete = false;
        int[] iArr = _icUTF8;
        byte[] bArr = this._inputBuffer;
        while (true) {
            int i = this._inputPtr;
            int i2 = this._inputEnd;
            if (i >= i2) {
                loadMoreGuaranteed();
                i = this._inputPtr;
                i2 = this._inputEnd;
            }
            while (i < i2) {
                int i3 = i + 1;
                i = bArr[i] & 255;
                if (iArr[i] != 0) {
                    this._inputPtr = i3;
                    if (i != 34) {
                        switch (iArr[i]) {
                            case 1:
                                _decodeEscaped();
                                break;
                            case 2:
                                _skipUtf8_2(i);
                                break;
                            case 3:
                                _skipUtf8_3(i);
                                break;
                            case 4:
                                _skipUtf8_4(i);
                                break;
                            default:
                                if (i >= 32) {
                                    _reportInvalidChar(i);
                                    break;
                                } else {
                                    _throwUnquotedSpace(i, "string value");
                                    break;
                                }
                        }
                    }
                    return;
                }
                i = i3;
            }
            this._inputPtr = i;
        }
    }

    public byte[] getBinaryValue(Base64Variant base64Variant) throws IOException {
        if (this._currToken != JsonToken.VALUE_STRING && (this._currToken != JsonToken.VALUE_EMBEDDED_OBJECT || this._binaryValue == null)) {
            _reportError("Current token (" + this._currToken + ") not VALUE_STRING or VALUE_EMBEDDED_OBJECT, can not access as binary");
        }
        if (this._tokenIncomplete) {
            try {
                this._binaryValue = _decodeBase64(base64Variant);
                this._tokenIncomplete = false;
            } catch (IllegalArgumentException e) {
                throw _constructError("Failed to decode VALUE_STRING as base64 (" + base64Variant + "): " + e.getMessage());
            }
        } else if (this._binaryValue == null) {
            ByteArrayBuilder _getByteArrayBuilder = _getByteArrayBuilder();
            _decodeBase64(getText(), _getByteArrayBuilder, base64Variant);
            this._binaryValue = _getByteArrayBuilder.toByteArray();
        }
        return this._binaryValue;
    }

    public ObjectCodec getCodec() {
        return this._objectCodec;
    }

    public JsonLocation getCurrentLocation() {
        return new JsonLocation(this._ioContext.getSourceReference(), this._currInputProcessed + ((long) this._inputPtr), -1, this._currInputRow, (this._inputPtr - this._currInputRowStart) + 1);
    }

    public Object getInputSource() {
        return this._inputStream;
    }

    public String getText() throws IOException {
        if (this._currToken != JsonToken.VALUE_STRING) {
            return _getText2(this._currToken);
        }
        if (!this._tokenIncomplete) {
            return this._textBuffer.contentsAsString();
        }
        this._tokenIncomplete = false;
        return _finishAndReturnString();
    }

    public char[] getTextCharacters() throws IOException {
        if (this._currToken == null) {
            return null;
        }
        switch (this._currToken.id()) {
            case 5:
                if (!this._nameCopied) {
                    String currentName = this._parsingContext.getCurrentName();
                    int length = currentName.length();
                    if (this._nameCopyBuffer == null) {
                        this._nameCopyBuffer = this._ioContext.allocNameCopyBuffer(length);
                    } else if (this._nameCopyBuffer.length < length) {
                        this._nameCopyBuffer = new char[length];
                    }
                    currentName.getChars(0, length, this._nameCopyBuffer, 0);
                    this._nameCopied = true;
                }
                return this._nameCopyBuffer;
            case 6:
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    _finishString();
                    break;
                }
                break;
            case 7:
            case 8:
                break;
            default:
                return this._currToken.asCharArray();
        }
        return this._textBuffer.getTextBuffer();
    }

    public int getTextLength() throws IOException {
        if (this._currToken == null) {
            return 0;
        }
        switch (this._currToken.id()) {
            case 5:
                return this._parsingContext.getCurrentName().length();
            case 6:
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    _finishString();
                    break;
                }
                break;
            case 7:
            case 8:
                break;
            default:
                return this._currToken.asCharArray().length;
        }
        return this._textBuffer.size();
    }

    public int getTextOffset() throws IOException {
        if (this._currToken == null) {
            return 0;
        }
        switch (this._currToken.id()) {
            case 6:
                if (this._tokenIncomplete) {
                    this._tokenIncomplete = false;
                    _finishString();
                    break;
                }
                break;
            case 7:
            case 8:
                break;
            default:
                return 0;
        }
        return this._textBuffer.getTextOffset();
    }

    public JsonLocation getTokenLocation() {
        Object sourceReference = this._ioContext.getSourceReference();
        return this._currToken == JsonToken.FIELD_NAME ? new JsonLocation(sourceReference, this._currInputProcessed + ((long) (this._nameStartOffset - 1)), -1, this._nameStartRow, this._nameStartCol) : new JsonLocation(sourceReference, this._tokenInputTotal - 1, -1, this._tokenInputRow, this._tokenInputCol);
    }

    public int getValueAsInt() throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken != JsonToken.VALUE_NUMBER_INT && jsonToken != JsonToken.VALUE_NUMBER_FLOAT) {
            return super.getValueAsInt(0);
        }
        if ((this._numTypesValid & 1) == 0) {
            if (this._numTypesValid == 0) {
                return _parseIntValue();
            }
            if ((this._numTypesValid & 1) == 0) {
                convertNumberToInt();
            }
        }
        return this._numberInt;
    }

    public int getValueAsInt(int i) throws IOException {
        JsonToken jsonToken = this._currToken;
        if (jsonToken != JsonToken.VALUE_NUMBER_INT && jsonToken != JsonToken.VALUE_NUMBER_FLOAT) {
            return super.getValueAsInt(i);
        }
        if ((this._numTypesValid & 1) == 0) {
            if (this._numTypesValid == 0) {
                return _parseIntValue();
            }
            if ((this._numTypesValid & 1) == 0) {
                convertNumberToInt();
            }
        }
        return this._numberInt;
    }

    public String getValueAsString() throws IOException {
        if (this._currToken != JsonToken.VALUE_STRING) {
            return this._currToken == JsonToken.FIELD_NAME ? getCurrentName() : super.getValueAsString(null);
        } else {
            if (!this._tokenIncomplete) {
                return this._textBuffer.contentsAsString();
            }
            this._tokenIncomplete = false;
            return _finishAndReturnString();
        }
    }

    public String getValueAsString(String str) throws IOException {
        if (this._currToken != JsonToken.VALUE_STRING) {
            return this._currToken == JsonToken.FIELD_NAME ? getCurrentName() : super.getValueAsString(str);
        } else {
            if (!this._tokenIncomplete) {
                return this._textBuffer.contentsAsString();
            }
            this._tokenIncomplete = false;
            return _finishAndReturnString();
        }
    }

    protected final boolean loadMore() throws IOException {
        int i = this._inputEnd;
        this._currInputProcessed += (long) this._inputEnd;
        this._currInputRowStart -= this._inputEnd;
        this._nameStartOffset -= i;
        if (this._inputStream == null) {
            return false;
        }
        i = this._inputBuffer.length;
        if (i == 0) {
            return false;
        }
        i = this._inputStream.read(this._inputBuffer, 0, i);
        if (i > 0) {
            this._inputPtr = 0;
            this._inputEnd = i;
            return true;
        }
        _closeInput();
        if (i != 0) {
            return false;
        }
        throw new IOException("InputStream.read() returned 0 characters when trying to read " + this._inputBuffer.length + " bytes");
    }

    public Boolean nextBooleanValue() throws IOException {
        JsonToken jsonToken;
        if (this._currToken == JsonToken.FIELD_NAME) {
            this._nameCopied = false;
            jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken == JsonToken.VALUE_TRUE) {
                return Boolean.TRUE;
            }
            if (jsonToken == JsonToken.VALUE_FALSE) {
                return Boolean.FALSE;
            }
            if (jsonToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                return null;
            } else if (jsonToken != JsonToken.START_OBJECT) {
                return null;
            } else {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                return null;
            }
        }
        jsonToken = nextToken();
        return jsonToken == JsonToken.VALUE_TRUE ? Boolean.TRUE : jsonToken == JsonToken.VALUE_FALSE ? Boolean.FALSE : null;
    }

    public String nextFieldName() throws IOException {
        this._numTypesValid = 0;
        if (this._currToken == JsonToken.FIELD_NAME) {
            _nextAfterName();
            return null;
        }
        if (this._tokenIncomplete) {
            _skipString();
        }
        int _skipWSOrEnd = _skipWSOrEnd();
        if (_skipWSOrEnd < 0) {
            close();
            this._currToken = null;
            return null;
        }
        this._binaryValue = null;
        if (_skipWSOrEnd == 93) {
            _updateLocation();
            if (!this._parsingContext.inArray()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, '}');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            this._currToken = JsonToken.END_ARRAY;
            return null;
        } else if (_skipWSOrEnd == 125) {
            _updateLocation();
            if (!this._parsingContext.inObject()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, ']');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            this._currToken = JsonToken.END_OBJECT;
            return null;
        } else {
            if (this._parsingContext.expectComma()) {
                if (_skipWSOrEnd != 44) {
                    _reportUnexpectedChar(_skipWSOrEnd, "was expecting comma to separate " + this._parsingContext.getTypeDesc() + " entries");
                }
                _skipWSOrEnd = _skipWS();
            }
            if (this._parsingContext.inObject()) {
                _updateNameLocation();
                String _parseName = _parseName(_skipWSOrEnd);
                this._parsingContext.setCurrentName(_parseName);
                this._currToken = JsonToken.FIELD_NAME;
                _skipWSOrEnd = _skipColon();
                _updateLocation();
                if (_skipWSOrEnd == 34) {
                    this._tokenIncomplete = true;
                    this._nextToken = JsonToken.VALUE_STRING;
                    return _parseName;
                }
                JsonToken _parseNegNumber;
                switch (_skipWSOrEnd) {
                    case 45:
                        _parseNegNumber = _parseNegNumber();
                        break;
                    case 48:
                    case 49:
                    case 50:
                    case 51:
                    case 52:
                    case 53:
                    case 54:
                    case 55:
                    case 56:
                    case 57:
                        _parseNegNumber = _parsePosNumber(_skipWSOrEnd);
                        break;
                    case 91:
                        _parseNegNumber = JsonToken.START_ARRAY;
                        break;
                    case 102:
                        _matchToken("false", 1);
                        _parseNegNumber = JsonToken.VALUE_FALSE;
                        break;
                    case 110:
                        _matchToken("null", 1);
                        _parseNegNumber = JsonToken.VALUE_NULL;
                        break;
                    case 116:
                        _matchToken("true", 1);
                        _parseNegNumber = JsonToken.VALUE_TRUE;
                        break;
                    case 123:
                        _parseNegNumber = JsonToken.START_OBJECT;
                        break;
                    default:
                        _parseNegNumber = _handleUnexpectedValue(_skipWSOrEnd);
                        break;
                }
                this._nextToken = _parseNegNumber;
                return _parseName;
            }
            _updateLocation();
            _nextTokenNotInObject(_skipWSOrEnd);
            return null;
        }
    }

    public boolean nextFieldName(SerializableString serializableString) throws IOException {
        int i = 0;
        this._numTypesValid = 0;
        if (this._currToken == JsonToken.FIELD_NAME) {
            _nextAfterName();
            return false;
        }
        if (this._tokenIncomplete) {
            _skipString();
        }
        int _skipWSOrEnd = _skipWSOrEnd();
        if (_skipWSOrEnd < 0) {
            close();
            this._currToken = null;
            return false;
        }
        this._binaryValue = null;
        if (_skipWSOrEnd == 93) {
            _updateLocation();
            if (!this._parsingContext.inArray()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, '}');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            this._currToken = JsonToken.END_ARRAY;
            return false;
        } else if (_skipWSOrEnd == 125) {
            _updateLocation();
            if (!this._parsingContext.inObject()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, ']');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            this._currToken = JsonToken.END_OBJECT;
            return false;
        } else {
            if (this._parsingContext.expectComma()) {
                if (_skipWSOrEnd != 44) {
                    _reportUnexpectedChar(_skipWSOrEnd, "was expecting comma to separate " + this._parsingContext.getTypeDesc() + " entries");
                }
                _skipWSOrEnd = _skipWS();
            }
            if (this._parsingContext.inObject()) {
                _updateNameLocation();
                if (_skipWSOrEnd == 34) {
                    byte[] asQuotedUTF8 = serializableString.asQuotedUTF8();
                    int length = asQuotedUTF8.length;
                    if ((this._inputPtr + length) + 4 < this._inputEnd) {
                        int i2 = this._inputPtr + length;
                        if (this._inputBuffer[i2] == (byte) 34) {
                            length = this._inputPtr;
                            while (length != i2) {
                                if (asQuotedUTF8[i] == this._inputBuffer[length]) {
                                    i++;
                                    length++;
                                }
                            }
                            this._parsingContext.setCurrentName(serializableString.getValue());
                            _isNextTokenNameYes(_skipColonFast(length + 1));
                            return true;
                        }
                    }
                }
                return _isNextTokenNameMaybe(_skipWSOrEnd, serializableString);
            }
            _updateLocation();
            _nextTokenNotInObject(_skipWSOrEnd);
            return false;
        }
    }

    public int nextIntValue(int i) throws IOException {
        if (this._currToken != JsonToken.FIELD_NAME) {
            return nextToken() == JsonToken.VALUE_NUMBER_INT ? getIntValue() : i;
        } else {
            this._nameCopied = false;
            JsonToken jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken == JsonToken.VALUE_NUMBER_INT) {
                return getIntValue();
            }
            if (jsonToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                return i;
            } else if (jsonToken != JsonToken.START_OBJECT) {
                return i;
            } else {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                return i;
            }
        }
    }

    public long nextLongValue(long j) throws IOException {
        if (this._currToken != JsonToken.FIELD_NAME) {
            return nextToken() == JsonToken.VALUE_NUMBER_INT ? getLongValue() : j;
        } else {
            this._nameCopied = false;
            JsonToken jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken == JsonToken.VALUE_NUMBER_INT) {
                return getLongValue();
            }
            if (jsonToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                return j;
            } else if (jsonToken != JsonToken.START_OBJECT) {
                return j;
            } else {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                return j;
            }
        }
    }

    public String nextTextValue() throws IOException {
        if (this._currToken != JsonToken.FIELD_NAME) {
            return nextToken() == JsonToken.VALUE_STRING ? getText() : null;
        } else {
            this._nameCopied = false;
            JsonToken jsonToken = this._nextToken;
            this._nextToken = null;
            this._currToken = jsonToken;
            if (jsonToken == JsonToken.VALUE_STRING) {
                if (!this._tokenIncomplete) {
                    return this._textBuffer.contentsAsString();
                }
                this._tokenIncomplete = false;
                return _finishAndReturnString();
            } else if (jsonToken == JsonToken.START_ARRAY) {
                this._parsingContext = this._parsingContext.createChildArrayContext(this._tokenInputRow, this._tokenInputCol);
                return null;
            } else if (jsonToken != JsonToken.START_OBJECT) {
                return null;
            } else {
                this._parsingContext = this._parsingContext.createChildObjectContext(this._tokenInputRow, this._tokenInputCol);
                return null;
            }
        }
    }

    public JsonToken nextToken() throws IOException {
        if (this._currToken == JsonToken.FIELD_NAME) {
            return _nextAfterName();
        }
        this._numTypesValid = 0;
        if (this._tokenIncomplete) {
            _skipString();
        }
        int _skipWSOrEnd = _skipWSOrEnd();
        if (_skipWSOrEnd < 0) {
            close();
            this._currToken = null;
            return null;
        }
        this._binaryValue = null;
        JsonToken jsonToken;
        if (_skipWSOrEnd == 93) {
            _updateLocation();
            if (!this._parsingContext.inArray()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, '}');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            jsonToken = JsonToken.END_ARRAY;
            this._currToken = jsonToken;
            return jsonToken;
        } else if (_skipWSOrEnd == 125) {
            _updateLocation();
            if (!this._parsingContext.inObject()) {
                _reportMismatchedEndMarker(_skipWSOrEnd, ']');
            }
            this._parsingContext = this._parsingContext.clearAndGetParent();
            jsonToken = JsonToken.END_OBJECT;
            this._currToken = jsonToken;
            return jsonToken;
        } else {
            if (this._parsingContext.expectComma()) {
                if (_skipWSOrEnd != 44) {
                    _reportUnexpectedChar(_skipWSOrEnd, "was expecting comma to separate " + this._parsingContext.getTypeDesc() + " entries");
                }
                _skipWSOrEnd = _skipWS();
            }
            if (this._parsingContext.inObject()) {
                _updateNameLocation();
                this._parsingContext.setCurrentName(_parseName(_skipWSOrEnd));
                this._currToken = JsonToken.FIELD_NAME;
                _skipWSOrEnd = _skipColon();
                _updateLocation();
                if (_skipWSOrEnd == 34) {
                    this._tokenIncomplete = true;
                    this._nextToken = JsonToken.VALUE_STRING;
                    return this._currToken;
                }
                switch (_skipWSOrEnd) {
                    case 45:
                        jsonToken = _parseNegNumber();
                        break;
                    case 48:
                    case 49:
                    case 50:
                    case 51:
                    case 52:
                    case 53:
                    case 54:
                    case 55:
                    case 56:
                    case 57:
                        jsonToken = _parsePosNumber(_skipWSOrEnd);
                        break;
                    case 91:
                        jsonToken = JsonToken.START_ARRAY;
                        break;
                    case 102:
                        _matchToken("false", 1);
                        jsonToken = JsonToken.VALUE_FALSE;
                        break;
                    case 110:
                        _matchToken("null", 1);
                        jsonToken = JsonToken.VALUE_NULL;
                        break;
                    case 116:
                        _matchToken("true", 1);
                        jsonToken = JsonToken.VALUE_TRUE;
                        break;
                    case 123:
                        jsonToken = JsonToken.START_OBJECT;
                        break;
                    default:
                        jsonToken = _handleUnexpectedValue(_skipWSOrEnd);
                        break;
                }
                this._nextToken = jsonToken;
                return this._currToken;
            }
            _updateLocation();
            return _nextTokenNotInObject(_skipWSOrEnd);
        }
    }

    protected final String parseEscapedName(int[] iArr, int i, int i2, int i3, int i4) throws IOException {
        int[] iArr2 = _icLatin1;
        while (true) {
            int i5;
            int i6;
            int i7;
            byte[] bArr;
            if (iArr2[i3] != 0) {
                if (i3 == 34) {
                    break;
                }
                if (i3 != 92) {
                    _throwUnquotedSpace(i3, "name");
                } else {
                    i3 = _decodeEscaped();
                }
                if (i3 > TransportMediator.KEYCODE_MEDIA_PAUSE) {
                    int[] iArr3;
                    int[] iArr4;
                    if (i4 >= 4) {
                        if (i >= iArr.length) {
                            iArr = growArrayBy(iArr, iArr.length);
                            this._quadBuffer = iArr;
                        }
                        i5 = i + 1;
                        iArr[i] = i2;
                        i4 = 0;
                        i2 = 0;
                        iArr3 = iArr;
                    } else {
                        i5 = i;
                        iArr3 = iArr;
                    }
                    if (i3 < 2048) {
                        i6 = i5;
                        i5 = (i2 << 8) | ((i3 >> 6) | 192);
                        iArr4 = iArr3;
                        i7 = i4 + 1;
                    } else {
                        int i8;
                        i6 = ((i3 >> 12) | 224) | (i2 << 8);
                        int i9 = i4 + 1;
                        if (i9 >= 4) {
                            if (i5 >= iArr3.length) {
                                iArr3 = growArrayBy(iArr3, iArr3.length);
                                this._quadBuffer = iArr3;
                            }
                            iArr3[i5] = i6;
                            i6 = i5 + 1;
                            i8 = 0;
                            iArr4 = iArr3;
                            i7 = 0;
                        } else {
                            i8 = i9;
                            iArr4 = iArr3;
                            i7 = i6;
                            i6 = i5;
                        }
                        i5 = (((i3 >> 6) & 63) | 128) | (i7 << 8);
                        i7 = i8 + 1;
                    }
                    i2 = (i3 & 63) | 128;
                    i = i6;
                    iArr = iArr4;
                    if (i7 >= 4) {
                        i4 = i7 + 1;
                        i2 |= i5 << 8;
                    } else {
                        if (i >= iArr.length) {
                            iArr = growArrayBy(iArr, iArr.length);
                            this._quadBuffer = iArr;
                        }
                        iArr[i] = i5;
                        i4 = 1;
                        i++;
                    }
                    if (this._inputPtr >= this._inputEnd && !loadMore()) {
                        _reportInvalidEOF(" in field name");
                    }
                    bArr = this._inputBuffer;
                    i6 = this._inputPtr;
                    this._inputPtr = i6 + 1;
                    i3 = bArr[i6] & 255;
                }
            }
            i7 = i4;
            i5 = i2;
            i2 = i3;
            if (i7 >= 4) {
                if (i >= iArr.length) {
                    iArr = growArrayBy(iArr, iArr.length);
                    this._quadBuffer = iArr;
                }
                iArr[i] = i5;
                i4 = 1;
                i++;
            } else {
                i4 = i7 + 1;
                i2 |= i5 << 8;
            }
            _reportInvalidEOF(" in field name");
            bArr = this._inputBuffer;
            i6 = this._inputPtr;
            this._inputPtr = i6 + 1;
            i3 = bArr[i6] & 255;
        }
        if (i4 > 0) {
            if (i >= iArr.length) {
                iArr = growArrayBy(iArr, iArr.length);
                this._quadBuffer = iArr;
            }
            iArr[i] = pad(i2, i4);
            i++;
        }
        String findName = this._symbols.findName(iArr, i);
        return findName == null ? addName(iArr, i, i4) : findName;
    }

    protected final String parseLongName(int i, int i2, int i3) throws IOException {
        this._quadBuffer[0] = this._quad1;
        this._quadBuffer[1] = i2;
        this._quadBuffer[2] = i3;
        byte[] bArr = this._inputBuffer;
        int[] iArr = _icLatin1;
        int i4 = i;
        int i5 = 3;
        while (this._inputPtr + 4 <= this._inputEnd) {
            int i6 = this._inputPtr;
            this._inputPtr = i6 + 1;
            i6 = bArr[i6] & 255;
            if (iArr[i6] != 0) {
                return i6 == 34 ? findName(this._quadBuffer, i5, i4, 1) : parseEscapedName(this._quadBuffer, i5, i4, i6, 1);
            } else {
                i4 = (i4 << 8) | i6;
                i6 = this._inputPtr;
                this._inputPtr = i6 + 1;
                i6 = bArr[i6] & 255;
                if (iArr[i6] != 0) {
                    return i6 == 34 ? findName(this._quadBuffer, i5, i4, 2) : parseEscapedName(this._quadBuffer, i5, i4, i6, 2);
                } else {
                    i4 = (i4 << 8) | i6;
                    i6 = this._inputPtr;
                    this._inputPtr = i6 + 1;
                    i6 = bArr[i6] & 255;
                    if (iArr[i6] != 0) {
                        return i6 == 34 ? findName(this._quadBuffer, i5, i4, 3) : parseEscapedName(this._quadBuffer, i5, i4, i6, 3);
                    } else {
                        i4 = (i4 << 8) | i6;
                        i6 = this._inputPtr;
                        this._inputPtr = i6 + 1;
                        i6 = bArr[i6] & 255;
                        if (iArr[i6] != 0) {
                            return i6 == 34 ? findName(this._quadBuffer, i5, i4, 4) : parseEscapedName(this._quadBuffer, i5, i4, i6, 4);
                        } else {
                            if (i5 >= this._quadBuffer.length) {
                                this._quadBuffer = growArrayBy(this._quadBuffer, i5);
                            }
                            this._quadBuffer[i5] = i4;
                            i5++;
                            i4 = i6;
                        }
                    }
                }
            }
        }
        return parseEscapedName(this._quadBuffer, i5, 0, i4, 0);
    }

    protected final String parseMediumName(int i) throws IOException {
        byte[] bArr = this._inputBuffer;
        int[] iArr = _icLatin1;
        int i2 = this._inputPtr;
        this._inputPtr = i2 + 1;
        i2 = bArr[i2] & 255;
        if (iArr[i2] != 0) {
            return i2 == 34 ? findName(this._quad1, i, 1) : parseName(this._quad1, i, i2, 1);
        } else {
            i2 |= i << 8;
            int i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            i3 = bArr[i3] & 255;
            if (iArr[i3] != 0) {
                return i3 == 34 ? findName(this._quad1, i2, 2) : parseName(this._quad1, i2, i3, 2);
            } else {
                i2 = (i2 << 8) | i3;
                i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                i3 = bArr[i3] & 255;
                if (iArr[i3] != 0) {
                    return i3 == 34 ? findName(this._quad1, i2, 3) : parseName(this._quad1, i2, i3, 3);
                } else {
                    i2 = (i2 << 8) | i3;
                    i3 = this._inputPtr;
                    this._inputPtr = i3 + 1;
                    int i4 = bArr[i3] & 255;
                    return iArr[i4] != 0 ? i4 == 34 ? findName(this._quad1, i2, 4) : parseName(this._quad1, i2, i4, 4) : parseMediumName2(i4, i2);
                }
            }
        }
    }

    protected final String parseMediumName2(int i, int i2) throws IOException {
        byte[] bArr = this._inputBuffer;
        int[] iArr = _icLatin1;
        int i3 = this._inputPtr;
        this._inputPtr = i3 + 1;
        int i4 = bArr[i3] & 255;
        if (iArr[i4] != 0) {
            return i4 == 34 ? findName(this._quad1, i2, i, 1) : parseName(this._quad1, i2, i, i4, 1);
        } else {
            int i5 = (i << 8) | i4;
            i3 = this._inputPtr;
            this._inputPtr = i3 + 1;
            i4 = bArr[i3] & 255;
            if (iArr[i4] != 0) {
                return i4 == 34 ? findName(this._quad1, i2, i5, 2) : parseName(this._quad1, i2, i5, i4, 2);
            } else {
                i5 = (i5 << 8) | i4;
                i3 = this._inputPtr;
                this._inputPtr = i3 + 1;
                i4 = bArr[i3] & 255;
                if (iArr[i4] != 0) {
                    return i4 == 34 ? findName(this._quad1, i2, i5, 3) : parseName(this._quad1, i2, i5, i4, 3);
                } else {
                    i5 = (i5 << 8) | i4;
                    i3 = this._inputPtr;
                    this._inputPtr = i3 + 1;
                    i4 = bArr[i3] & 255;
                    return iArr[i4] != 0 ? i4 == 34 ? findName(this._quad1, i2, i5, 4) : parseName(this._quad1, i2, i5, i4, 4) : parseLongName(i4, i2, i5);
                }
            }
        }
    }

    public int readBinaryValue(Base64Variant base64Variant, OutputStream outputStream) throws IOException {
        if (this._tokenIncomplete && this._currToken == JsonToken.VALUE_STRING) {
            byte[] allocBase64Buffer = this._ioContext.allocBase64Buffer();
            try {
                int _readBinary = _readBinary(base64Variant, outputStream, allocBase64Buffer);
                return _readBinary;
            } finally {
                this._ioContext.releaseBase64Buffer(allocBase64Buffer);
            }
        } else {
            byte[] binaryValue = getBinaryValue(base64Variant);
            outputStream.write(binaryValue);
            return binaryValue.length;
        }
    }

    public int releaseBuffered(OutputStream outputStream) throws IOException {
        int i = this._inputEnd - this._inputPtr;
        if (i <= 0) {
            return 0;
        }
        outputStream.write(this._inputBuffer, this._inputPtr, i);
        return i;
    }

    public void setCodec(ObjectCodec objectCodec) {
        this._objectCodec = objectCodec;
    }

    protected String slowParseName() throws IOException {
        if (this._inputPtr >= this._inputEnd && !loadMore()) {
            _reportInvalidEOF(": was expecting closing '\"' for name");
        }
        byte[] bArr = this._inputBuffer;
        int i = this._inputPtr;
        this._inputPtr = i + 1;
        int i2 = bArr[i] & 255;
        return i2 == 34 ? "" : parseEscapedName(this._quadBuffer, 0, 0, i2, 0);
    }
}