package com.google.zxing.aztec.encoder;

import com.google.zxing.common.BitMatrix;

public final class AztecCode {
    private int codeWords;
    private boolean compact;
    private int layers;
    private BitMatrix matrix;
    private int size;

    public final int getCodeWords() {
        return this.codeWords;
    }

    public final int getLayers() {
        return this.layers;
    }

    public final BitMatrix getMatrix() {
        return this.matrix;
    }

    public final int getSize() {
        return this.size;
    }

    public final boolean isCompact() {
        return this.compact;
    }

    public final void setCodeWords(int i) {
        this.codeWords = i;
    }

    public final void setCompact(boolean z) {
        this.compact = z;
    }

    public final void setLayers(int i) {
        this.layers = i;
    }

    public final void setMatrix(BitMatrix bitMatrix) {
        this.matrix = bitMatrix;
    }

    public final void setSize(int i) {
        this.size = i;
    }
}
