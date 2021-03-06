package com.google.zxing.datamatrix.encoder;

final class DataMatrixSymbolInfo144 extends SymbolInfo {
    DataMatrixSymbolInfo144() {
        super(false, 1558, 620, 22, 22, 36);
        this.rsBlockData = -1;
        this.rsBlockError = 62;
    }

    public final int getDataLengthForInterleavedBlock(int i) {
        return i <= 8 ? 156 : 155;
    }

    public final int getInterleavedBlockCount() {
        return 10;
    }
}
