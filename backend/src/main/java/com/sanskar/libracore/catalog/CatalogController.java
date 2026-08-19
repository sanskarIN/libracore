package com.sanskar.libracore.catalog;

import com.sanskar.libracore.catalog.CatalogModels.BookDetail;
import com.sanskar.libracore.catalog.CatalogModels.BookSummary;
import com.sanskar.libracore.catalog.CatalogModels.BranchView;
import com.sanskar.libracore.catalog.CatalogModels.CopyView;
import com.sanskar.libracore.catalog.CatalogModels.CreateBookRequest;
import com.sanskar.libracore.catalog.CatalogModels.CreateBranchRequest;
import com.sanskar.libracore.catalog.CatalogModels.CreateCopyRequest;
import com.sanskar.libracore.catalog.CatalogModels.CreateShelfRequest;
import com.sanskar.libracore.catalog.CatalogModels.SearchPage;
import com.sanskar.libracore.catalog.CatalogModels.ShelfView;
import com.sanskar.libracore.catalog.CatalogModels.UpdateBookRequest;
import com.sanskar.libracore.catalog.CatalogModels.UpdateCopyStatusRequest;
import com.sanskar.libracore.common.ApiException;
import com.sanskar.libracore.security.AppPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/books")
    public SearchPage<BookSummary> searchBooks(
            @RequestParam(defaultValue = "") @Size(max = 200) String q,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset
    ) {
        return catalogService.searchBooks(q, branchId, availableOnly, limit, offset);
    }

    @PostMapping("/books")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDetail createBook(
            @Valid @RequestBody CreateBookRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return catalogService.createBook(request, principal.userId());
    }

    @GetMapping("/books/{bookId}")
    public BookDetail getBook(@PathVariable UUID bookId) {
        return catalogService.getBook(bookId);
    }

    @PutMapping("/books/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public BookDetail updateBook(
            @PathVariable UUID bookId,
            @Valid @RequestBody UpdateBookRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return catalogService.updateBook(bookId, request, principal.userId());
    }

    @PostMapping("/books/{bookId}/copies")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CopyView addCopy(
            @PathVariable UUID bookId,
            @Valid @RequestBody CreateCopyRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return catalogService.addCopy(bookId, request, principal.userId());
    }

    @GetMapping("/copies/{copyId}")
    public CopyView getCopy(@PathVariable UUID copyId) {
        return catalogService.getCopy(copyId);
    }

    @GetMapping("/copies/lookup")
    public CopyView findCopy(@RequestParam @Size(min = 1, max = 300) String code) {
        return catalogService.findCopyByCode(code)
                .orElseThrow(() -> ApiException.notFound("copy_not_found", "Book copy was not found."));
    }

    @PatchMapping("/copies/{copyId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public CopyView updateCopyStatus(
            @PathVariable UUID copyId,
            @Valid @RequestBody UpdateCopyStatusRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return catalogService.updateCopyStatus(copyId, request, principal.userId());
    }

    @GetMapping("/branches")
    public List<BranchView> listBranches() {
        return catalogService.listBranches();
    }

    @PostMapping("/branches")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public BranchView createBranch(
            @Valid @RequestBody CreateBranchRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return catalogService.createBranch(request, principal.userId());
    }

    @GetMapping("/shelves")
    public List<ShelfView> listShelves(@RequestParam(required = false) UUID branchId) {
        return catalogService.listShelves(branchId);
    }

    @PostMapping("/shelves")
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ShelfView createShelf(
            @Valid @RequestBody CreateShelfRequest request,
            @AuthenticationPrincipal AppPrincipal principal
    ) {
        return catalogService.createShelf(request, principal.userId());
    }
}
