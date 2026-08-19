package com.sanskar.libracore.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class CatalogModels {
    private CatalogModels() {
    }

    public record CreateBranchRequest(
            @NotBlank @Size(max = 32) @Pattern(regexp = "[A-Za-z0-9_-]+") String code,
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 64) String timezone
    ) {
    }

    public record BranchView(
            UUID id,
            String code,
            String name,
            String timezone,
            boolean active,
            OffsetDateTime createdAt
    ) {
    }

    public record CreateShelfRequest(
            @NotNull UUID branchId,
            @NotBlank @Size(max = 64) String code,
            @NotBlank @Size(max = 160) String label,
            @Size(max = 500) String locationNote
    ) {
    }

    public record ShelfView(
            UUID id,
            UUID branchId,
            String code,
            String label,
            String locationNote,
            boolean active
    ) {
    }

    public record CreateBookRequest(
            @NotBlank @Size(max = 400) String title,
            @Size(max = 400) String subtitle,
            @Size(max = 32) String isbn13,
            @Size(max = 5000) String description,
            @NotBlank @Size(max = 16) String languageCode,
            @Min(1000) @Max(9999) Integer publicationYear,
            @Size(max = 120) String editionLabel,
            @Size(max = 200) String publisherName,
            @Size(max = 20) List<@NotBlank @Size(max = 200) String> authors,
            @Size(max = 30) List<@NotBlank @Size(max = 120) String> categories
    ) {
    }

    public record UpdateBookRequest(
            @NotBlank @Size(max = 400) String title,
            @Size(max = 400) String subtitle,
            @Size(max = 5000) String description,
            @NotBlank @Size(max = 16) String languageCode,
            @Min(1000) @Max(9999) Integer publicationYear,
            @Size(max = 120) String editionLabel,
            @Size(max = 200) String publisherName,
            @Size(max = 20) List<@NotBlank @Size(max = 200) String> authors,
            @Size(max = 30) List<@NotBlank @Size(max = 120) String> categories
    ) {
    }

    public record CreateCopyRequest(
            @NotNull UUID branchId,
            UUID shelfId,
            @NotBlank @Size(max = 80) String accessionCode,
            @Size(max = 160) String barcodeValue,
            @Size(max = 300) String qrValue,
            LocalDate acquiredOn,
            @DecimalMin("0.00") BigDecimal purchasePrice,
            @Pattern(regexp = "[A-Z]{3}") String currencyCode,
            @Size(max = 500) String conditionNote
    ) {
    }

    public record UpdateCopyStatusRequest(
            @NotBlank @Pattern(regexp = "AVAILABLE|LOST|REPAIR|WITHDRAWN") String status,
            @Size(max = 500) String conditionNote,
            UUID shelfId
    ) {
    }

    public record BookSummary(
            UUID id,
            String isbn13,
            String title,
            String subtitle,
            String languageCode,
            Integer publicationYear,
            String editionLabel,
            String publisherName,
            List<String> authors,
            List<String> categories,
            int totalCopies,
            int availableCopies
    ) {
    }

    public record BookDetail(
            BookSummary summary,
            String description,
            List<CopyView> copies,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
    }

    public record CopyView(
            UUID id,
            UUID bookId,
            UUID branchId,
            String branchName,
            UUID shelfId,
            String shelfLabel,
            String accessionCode,
            String barcodeValue,
            String qrValue,
            String status,
            LocalDate acquiredOn,
            BigDecimal purchasePrice,
            String currencyCode,
            String conditionNote
    ) {
    }

    public record SearchPage<T>(
            List<T> items,
            int limit,
            int offset,
            boolean hasMore
    ) {
    }
}
