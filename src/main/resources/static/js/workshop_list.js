/**
 * Workshop list management functionality
 * Handles workshop deletion confirmation
 */
document.addEventListener('DOMContentLoaded', function () {
    // Confirmation modal to delete workshop
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal')); const filterForm = document.getElementById('filterForm');

    // Ensure when filtering we always go to the first page
    if (filterForm) {
        filterForm.addEventListener('submit', function () {
            document.getElementById('pageInput').value = 0;
        });
    }

    // Handle delete buttons
    document.querySelectorAll('.delete-workshop-btn').forEach(button => {
        button.addEventListener('click', function () {
            const workshopId = this.getAttribute('data-workshop-id');
            const workshopName = this.getAttribute('data-workshop-name');

            document.getElementById('workshopName').textContent = workshopName;
            document.getElementById('workshopIdInput').value = workshopId;

            deleteModal.show();
        });
    });
});