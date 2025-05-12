document.addEventListener('DOMContentLoaded', function () {
    // Configuration of the delete confirmation modal
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmationModal'));

    // Handle delete buttons
    document.querySelectorAll('.delete-update-btn').forEach(button => {
        button.addEventListener('click', function () {
            const updateId = this.getAttribute('data-update-id');
            const routeId = this.getAttribute('data-route-id');
            const updateDesc = this.getAttribute('data-update-desc');

            document.getElementById('updateDescription').textContent = updateDesc;
            document.getElementById('updateIdInput').value = updateId;
            document.getElementById('routeIdInput').value = routeId;

            deleteModal.show();
        });
    });
});
