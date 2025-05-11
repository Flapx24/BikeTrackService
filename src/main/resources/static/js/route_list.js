/**
 * Route list management functionality
 * Handles sorting and route deletion confirmation
 */
document.addEventListener('DOMContentLoaded', function () {
    // Sorting functionality
    const sortButton = document.getElementById('sort-button');
    const filterForm = document.getElementById('filterForm');
    let currentSort = sortButton.classList.contains('asc') ? 'asc' :
        (sortButton.classList.contains('desc') ? 'desc' : 'none');

    sortButton.addEventListener('click', function () {
        // Toggle sort state: none -> asc -> desc -> none
        if (currentSort === 'none') {
            currentSort = 'asc';
        } else if (currentSort === 'asc') {
            currentSort = 'desc';
        } else {
            currentSort = 'none';
        }

        // Update hidden sort input and submit form
        document.querySelector('input[name="sort"]').value = currentSort;
        filterForm.submit();
    });

    // Confirmation modal to delete route
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal'));

    // Handle delete buttons
    document.querySelectorAll('.delete-route-btn').forEach(button => {
        button.addEventListener('click', function () {
            const routeId = this.getAttribute('data-route-id');
            const routeTitle = this.getAttribute('data-route-title');

            document.getElementById('routeTitle').textContent = routeTitle;
            document.getElementById('routeIdInput').value = routeId;

            deleteModal.show();
        });
    });
});
