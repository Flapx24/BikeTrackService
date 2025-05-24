/**
 * Reviews list management functionality
 * Handles review deletion confirmation and date picker
 */
document.addEventListener('DOMContentLoaded', function () {
    // Confirmation modal to delete review
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal'));

    const filterForm = document.getElementById('filterForm');

    // Ensure when filtering we always go to the first page
    if (filterForm) {
        filterForm.addEventListener('submit', function () {
            document.getElementById('pageInput').value = 0;
        });
    }

    // Handle delete buttons
    document.querySelectorAll('.delete-review-btn').forEach(button => {
        button.addEventListener('click', function () {
            const reviewId = this.getAttribute('data-review-id');
            const reviewUsername = this.getAttribute('data-review-username');
            const reviewRouteName = this.getAttribute('data-review-route');

            document.getElementById('reviewUsername').textContent = reviewUsername;
            document.getElementById('reviewRouteName').textContent = reviewRouteName;
            document.getElementById('reviewIdInput').value = reviewId;

            deleteModal.show();
        });
    });
    // Date picker
    const dateInput = document.getElementById('date');
    const datePickerBtn = document.getElementById('datePickerBtn');
    const clearDateBtn = document.getElementById('clearDateBtn');

    if (dateInput && datePickerBtn) {
        const datePicker = flatpickr(dateInput, {
            dateFormat: "d/m/Y",
            allowInput: true,
            static: true
        });

        datePickerBtn.addEventListener('click', function () {
            datePicker.open();
        });
        // Clear date button functionality
        if (clearDateBtn) {
            clearDateBtn.addEventListener('click', function () {
                datePicker.clear(); // Clear the flatpickr instance
                dateInput.value = ''; // Clear the input field
            });
        }
    }
});