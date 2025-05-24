/**
 * Common pagination functionality
 * Handles pagination controls for all admin list pages
 */
function initializePagination() {
    const filterForm = document.getElementById('filterForm');
    const prevPageBtn = document.getElementById('prevPageBtn');
    const nextPageBtn = document.getElementById('nextPageBtn');
    const currentPageInput = document.getElementById('currentPageInput');
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    const pageInput = document.getElementById('pageInput');
    const pageSizeInput = document.getElementById('pageSizeInput');

    // Previous page button
    if (prevPageBtn) {
        prevPageBtn.addEventListener('click', function () {
            const currentPage = parseInt(this.getAttribute('data-current-page'));
            if (currentPage > 0) {
                pageInput.value = currentPage - 1;
                filterForm.submit();
            }
        });
    }

    // Next page button
    if (nextPageBtn) {
        nextPageBtn.addEventListener('click', function () {
            const currentPage = parseInt(this.getAttribute('data-current-page'));
            const totalPages = parseInt(currentPageInput.getAttribute('max'));
            if (currentPage + 1 < totalPages) {
                pageInput.value = currentPage + 1;
                filterForm.submit();
            }
        });
    }

    // Page number input
    if (currentPageInput) {
        currentPageInput.addEventListener('change', function () {
            const actualPage = parseInt(this.getAttribute('data-actual-page'));
            let newPage = parseInt(this.value) - 1;
            const maxPage = parseInt(this.getAttribute('max'));

            // Validate input
            if (isNaN(newPage) || newPage < 0) {
                newPage = 0;
                this.value = 1;
            } else if (newPage >= maxPage) {
                newPage = maxPage - 1;
                this.value = maxPage;
            }

            if (newPage !== actualPage) {
                pageInput.value = newPage;
                filterForm.submit();
            }
        });
    }

    // Page size selector
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', function () {
            pageSizeInput.value = this.value;
            pageInput.value = 0; // Reset to first page when changing page size
            filterForm.submit();
        });
    }
}

// Auto-initialize pagination when DOM is loaded
document.addEventListener('DOMContentLoaded', function () {
    initializePagination();
});
