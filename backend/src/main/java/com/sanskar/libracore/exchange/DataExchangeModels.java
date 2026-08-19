package com.sanskar.libracore.exchange;

import java.util.List;

public final class DataExchangeModels {
    private DataExchangeModels() {
    }

    public record ImportResult(
            String resource,
            int importedRows,
            List<String> warnings
    ) {
    }
}
