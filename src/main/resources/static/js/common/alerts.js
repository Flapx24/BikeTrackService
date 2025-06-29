/**
 * Auto-hides alert messages after a set time
 * 
 * This script detects all Bootstrap alerts on the page
 * and closes them automatically after a specified time.
 */
document.addEventListener("DOMContentLoaded", function() {
    // Auto hide alerts after 7 seconds
    const alerts = document.querySelectorAll(".alert-success, .alert-danger");
    alerts.forEach(function(alert) {
        setTimeout(function() {
            // Create a fade out effect and then remove the alert
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 7000);
    });
});
