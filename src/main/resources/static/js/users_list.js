document.addEventListener('DOMContentLoaded', function () {
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email'); const filterForm = document.getElementById('filterForm');

    // Ensure when filtering we always go to the first page
    if (filterForm) {
        filterForm.addEventListener('submit', function () {
            document.getElementById('pageInput').value = 0;
        });
    }

    // Function to check inputs and disable/enable as needed
    function checkInputs() {
        if (usernameInput.value.trim() !== '') {
            emailInput.disabled = true;
            emailInput.value = ''; // Clear the other field
        } else if (emailInput.value.trim() !== '') {
            usernameInput.disabled = true;
            usernameInput.value = ''; // Clear the other field
        } else {
            emailInput.disabled = false;
            usernameInput.disabled = false;
        }
    }

    // Initial check when page loads
    checkInputs();

    // Add event listeners for input changes
    usernameInput.addEventListener('input', function () {
        if (this.value.trim() === '') {
            emailInput.disabled = false;
        } else {
            emailInput.disabled = true;
            emailInput.value = '';
        }
    });

    emailInput.addEventListener('input', function () {
        if (this.value.trim() === '') {
            usernameInput.disabled = false;
        } else {
            usernameInput.disabled = true;
            usernameInput.value = '';
        }
    });
});