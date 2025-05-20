// Update date display
function updateDate() {
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    const now = new Date();
    document.getElementById('currentDate').textContent = now.toLocaleDateString('es-ES', options);
}

// Update time display
function updateTime() {
    const now = new Date();
    const timeString = now.toLocaleTimeString('es-ES');
    document.getElementById('currentTime').textContent = timeString;
}

// Initialize and set interval updates
document.addEventListener('DOMContentLoaded', function() {
    updateDate();
    updateTime();
    setInterval(updateTime, 1000);
});
