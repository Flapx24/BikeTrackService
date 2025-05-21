document.addEventListener('DOMContentLoaded', function () {    // Global variables
    let map;
    let routeMarkers = [];
    let routeLine;
    let selectedPointIndex = -1;
    let routeImages = [];
    let newImageFiles = [];
    let draggedCard = null;
    let deletedImageUrls = [];
    const MAX_ROUTE_POINTS = 50;
    const DEFAULT_LAT = 40.416775; // Madrid as default point
    const DEFAULT_LNG = -3.703790;
    let calculatedRouteLine;

    initMap();
    
    // Ensure counter has correct color from start
    updateRoutePointsCount();

    loadExistingImages(); document.getElementById('imageUpload').addEventListener('change', handleImageUpload);
    document.getElementById('saveButton').addEventListener('click', showSaveConfirmation);
    document.getElementById('cancelButton').addEventListener('click', showCancelConfirmation); document.getElementById('confirmSave').addEventListener('click', saveRoute);
    document.getElementById('confirmCancel').addEventListener('click', cancelEditing);
    document.getElementById('confirmDeletePoint').addEventListener('click', confirmDeletePoint);
    /**
     * Initializes the map with Leaflet
     */
    function initMap() {
        // Determine the initial map position:
        // If there are existing route points, use the first point
        // If not, use Madrid as default location
        let initialLat = DEFAULT_LAT;
        let initialLng = DEFAULT_LNG;
        let initialZoom = 13;

        if (typeof routePoints !== 'undefined' && routePoints.length > 0) {
            initialLat = routePoints[0].lat;
            initialLng = routePoints[0].lng;
        }
        // Initialize the map with the determined position
        map = L.map('map').setView([initialLat, initialLng], initialZoom);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        // Add route points from existing data if available
        if (typeof routePoints !== 'undefined' && routePoints.length > 0) {
            routePoints.forEach((point, index) => {
                addRouteMarker(L.latLng(point.lat, point.lng), index + 1);
            });
            drawRouteLine();
            updateRoutePointsCount(); // Update counter to reflect existing points
        }

        // Add click event to add points
        map.on('click', function (e) {
            addRoutePoint(e.latlng);
        });

        setTimeout(() => {
            map.invalidateSize();
        }, 100);
    }
    /**
    * Adds a point to the route
    */
    function addRoutePoint(latlng) {
        if (routePoints.length >= MAX_ROUTE_POINTS) {
            return;
        }

        routePoints.push(latlng);
        const pointNumber = routePoints.length;
        addRouteMarker(latlng, pointNumber);
        drawRouteLine();
        updateRoutePointsCount();
    }

    /**
     * Creates a marker for a route point and adds it to the map
     */
    function addRouteMarker(latlng, number) {
        // Create custom icon with number
        const pointIcon = L.divIcon({
            className: 'route-point-icon',
            html: number,
            iconSize: [24, 24],
            iconAnchor: [12, 12]
        });

        const marker = L.marker(latlng, {
            icon: pointIcon,
            draggable: true,
            pointNumber: number
        }).addTo(map);

        // Right-click to delete point
        marker.on('contextmenu', function (e) {
            L.DomEvent.stopPropagation(e);
            selectedPointIndex = routeMarkers.findIndex(m => m === marker);
            showDeletePointConfirmation();
        });

        // Update position when dragged
        marker.on('drag', function (e) {
            const index = routeMarkers.findIndex(m => m === e.target);
            if (index !== -1) {
                routePoints[index] = e.target.getLatLng();
                drawRouteLine();
            }
        });

        routeMarkers.push(marker);
    }

    /**
     * Shows a modal to confirm point deletion
     */
    function showDeletePointConfirmation() {
        const deleteModal = new bootstrap.Modal(document.getElementById('deletePointModal'));
        deleteModal.show();
    }
    /**
     * Deletes the currently selected route point
     */
    function confirmDeletePoint() {
        if (selectedPointIndex >= 0 && selectedPointIndex < routeMarkers.length) {
            map.removeLayer(routeMarkers[selectedPointIndex]);
            routeMarkers.splice(selectedPointIndex, 1);
            routePoints.splice(selectedPointIndex, 1);

            // Renumber remaining points
            routeMarkers.forEach((marker, idx) => {
                const newNumber = idx + 1;
                marker.options.pointNumber = newNumber;

                // Update the icon with new number
                marker.setIcon(L.divIcon({
                    className: 'route-point-icon',
                    html: newNumber,
                    iconSize: [24, 24],
                    iconAnchor: [12, 12]
                }));
            });

            drawRouteLine();
            updateRoutePointsCount();
            bootstrap.Modal.getInstance(document.getElementById('deletePointModal')).hide();
        }
    }

    /**
     * Updates the route points counter
     */    function updateRoutePointsCount() {
        const countElement = document.getElementById('routePointsCount');
        countElement.textContent = `${routePoints.length}/${MAX_ROUTE_POINTS} puntos`;

        // Set warning color if close to max, red if at max or below minimum
        if (routePoints.length >= MAX_ROUTE_POINTS) {
            countElement.classList.remove('bg-info', 'bg-warning');
            countElement.classList.add('bg-danger');
        } else if (routePoints.length >= MAX_ROUTE_POINTS - 5) {
            countElement.classList.remove('bg-info', 'bg-danger');
            countElement.classList.add('bg-warning');
        } else if (routePoints.length < 2) {
            // Not enough points - show as danger
            countElement.classList.remove('bg-info', 'bg-warning');
            countElement.classList.add('bg-danger');
        } else {
            countElement.classList.remove('bg-warning', 'bg-danger');
            countElement.classList.add('bg-info');
        }
    }

    /**
     * Draws a polyline connecting all route points
     */
    function drawRouteLine() {
        // Remove existing line
        if (routeLine) {
            map.removeLayer(routeLine);
        }

        // Draw new line if there are at least 2 points
        if (routePoints.length >= 2) {
            routeLine = L.polyline(routePoints, {
                color: '#007bff',
                weight: 4,
                opacity: 0.7,
                className: 'route-line'
            }).addTo(map);
        }

        // Update hidden input with coordinates
        const coordinatesInput = document.createElement('input');
        coordinatesInput.type = 'hidden';
        coordinatesInput.name = 'coordinatesInput';
        coordinatesInput.value = JSON.stringify(routePoints.map(point => ({
            lat: point.lat,
            lng: point.lng
        })));

        // Replace existing input if any
        const existingInput = document.querySelector('input[name="coordinatesInput"]');
        if (existingInput) {
            existingInput.parentNode.replaceChild(coordinatesInput, existingInput);
        } else {
            document.getElementById('routeForm').appendChild(coordinatesInput);
        }
    }

    /**
     * Shows an error message
     */
    function showError(message) {
        // Check if there's already an error message
        let errorElement = document.querySelector('.route-error');
        if (!errorElement) {
            errorElement = document.createElement('div');
            errorElement.className = 'route-error';
            document.getElementById('map').parentNode.appendChild(errorElement);
        }

        errorElement.textContent = message;

        // Hide after 3 seconds
        setTimeout(() => {
            errorElement.textContent = '';
        }, 3000);
    }
    /**
     * Shows a toast message with animation
     */
    function showToast(title, message, type = 'info') {
        let toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'toastContainer';
            toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            toastContainer.style.zIndex = '1100'; // Make sure it appears above other elements
            document.body.appendChild(toastContainer);
        }

        const toastId = 'toast-' + Date.now();

        // Determine toast background color based on type
        let bgClass = 'bg-info';
        let iconClass = 'fa-info-circle';

        switch (type) {
            case 'success':
                bgClass = 'bg-success';
                iconClass = 'fa-check-circle';
                break;
            case 'error':
                bgClass = 'bg-danger';
                iconClass = 'fa-exclamation-circle';
                break;
            case 'warning':
                bgClass = 'bg-warning';
                iconClass = 'fa-exclamation-triangle';
                break;
        }

        const toastHtml = `
            <div id="${toastId}" class="toast ${bgClass} text-white toast-animated" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="toast-header ${bgClass} text-white">
                    <i class="fas ${iconClass} me-2"></i>
                    <strong class="me-auto">${title}</strong>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
                <div class="toast-body">
                    ${message}
                </div>
            </div>
        `;

        toastContainer.insertAdjacentHTML('beforeend', toastHtml);

        const toast = new bootstrap.Toast(document.getElementById(toastId), {
            autohide: true,
            delay: 5000 // Display for 5 seconds
        });

        toast.show();

        // Add class for disappear animation before hiding
        document.getElementById(toastId).addEventListener('hide.bs.toast', function () {
            this.classList.add('hide');
        });

        // Remove the toast element after it's hidden
        document.getElementById(toastId).addEventListener('hidden.bs.toast', function () {
            setTimeout(() => {
                this.remove();
            }, 300);
        });
    }

    /**
     * Loads existing route images from the global variable
     * which is defined in the HTML using Thymeleaf
     */
    function loadExistingImages() {
        if (typeof existingImages !== 'undefined' && existingImages.length > 0) {
            routeImages = [...existingImages];
            renderImageCards();
        }
        updateNoImagesMessage();
        renderImageCards();
    }
    // Check the status of the no images message
    updateNoImagesMessage();

    // Reset deleted images when loading
    deletedImageUrls = [];

    /**
     * Handles the upload of new images
     */
    function handleImageUpload(event) {
        const files = event.target.files;
        if (!files || files.length === 0) {
            return;
        }


        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            newImageFiles.push(file);        // Create an object with the temporary URL and an isNewImage property
            const imageObj = {
                url: URL.createObjectURL(file),
                isNewImage: true
            };

            routeImages.push(imageObj);
        }

        renderImageCards();
        updateNoImagesMessage();

        event.target.value = '';
    }

    /**
     * Renders the image cards in the container
     */    function renderImageCards() {
        const container = document.getElementById('imagesContainer');
        container.innerHTML = '';
        // Only update the display of images
        // Hidden inputs will be created when saving


        routeImages.forEach((imageItem, index) => {
            // Determine if we are dealing with a direct URL string or an object
            let imageUrl;
            if (typeof imageItem === 'string') {
                imageUrl = imageItem;
            } else if (imageItem && imageItem.url) {
                imageUrl = imageItem.url;
            } else {
                console.error('Invalid image format:', imageItem);
                return; // Skip this iteration if the format is invalid
            }

            const card = document.createElement('div');
            card.className = 'image-card';
            card.draggable = true;
            card.dataset.index = index;

            card.addEventListener('dragstart', handleDragStart);
            card.addEventListener('dragend', handleDragEnd);
            card.addEventListener('dragover', handleDragOver);
            card.addEventListener('dragleave', handleDragLeave);
            card.addEventListener('drop', handleDrop);

            const img = document.createElement('img');
            img.src = imageUrl;
            img.alt = `Imagen ${index + 1}`;
            card.appendChild(img); const overlay = document.createElement('div');
            overlay.className = 'image-overlay';

            const viewBtn = createActionButton('action-view', 'fa-eye', () => {
                showImagePreview(imageUrl);
            });

            const deleteBtn = createActionButton('action-delete', 'fa-trash', () => {
                deleteImage(index);
            });

            overlay.appendChild(viewBtn);
            overlay.appendChild(deleteBtn);
            card.appendChild(overlay);

            const position = document.createElement('div');
            position.className = 'image-position';
            position.textContent = `${index + 1}`;
            card.appendChild(position);

            container.appendChild(card);
        });
    }

    /**
     * Handles the start of dragging an image
     */
    function handleDragStart(e) {
        draggedCard = this;
        draggedCard.classList.add('dragging');
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/html', this.innerHTML);
    }

    /**
     * Handles the end of dragging an image
     */
    function handleDragEnd(e) {
        draggedCard.classList.remove('dragging');

        const cards = document.querySelectorAll('.image-card');
        cards.forEach(card => {
            card.classList.remove('drag-over');
        });

        draggedCard = null;
    }

    /**
     * Handles when an image is dragged over another image
     */
    function handleDragOver(e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';

        if (this !== draggedCard) {
            this.classList.add('drag-over');
        }

        return false;
    }

    /**
     * Handles when an image leaves the drop target area
     */
    function handleDragLeave(e) {
        this.classList.remove('drag-over');
    }

    /**
     * Handles when an image is dropped onto another image
     */
    function handleDrop(e) {
        e.preventDefault();

        if (draggedCard && this !== draggedCard) {
            const fromIndex = parseInt(draggedCard.dataset.index);
            const toIndex = parseInt(this.dataset.index);

            const [imageUrl] = routeImages.splice(fromIndex, 1);
            routeImages.splice(toIndex, 0, imageUrl);

            renderImageCards();

            showToast('Imagen reordenada', `La imagen ha sido movida a la posición ${toIndex + 1}.`);
        }

        return false;
    }

    /**
     * Creates an action button for the image card
     */
    function createActionButton(className, iconClass, clickHandler) {
        const button = document.createElement('button');
        button.className = `image-action-btn ${className}`;
        button.type = 'button';

        const icon = document.createElement('i');
        icon.className = `fas ${iconClass}`;
        button.appendChild(icon);

        button.addEventListener('click', clickHandler);
        return button;
    }

    /**
     * Shows/hides the "no images" message
     */    function updateNoImagesMessage() {
        const noImagesMessage = document.getElementById('noImagesMessage');
        const dragInstructionMessage = document.getElementById('dragInstructionMessage');

        if (routeImages.length === 0) {
            // No images, show the "no images" message
            if (noImagesMessage) noImagesMessage.style.display = 'block';
            if (dragInstructionMessage) dragInstructionMessage.style.display = 'none';

        } else {
            // There are images, show drag instructions
            if (noImagesMessage) noImagesMessage.style.display = 'none';
            if (dragInstructionMessage) dragInstructionMessage.style.display = 'block';

        }
    }

    /**
     * Shows an image preview in a modal
     */
    function showImagePreview(url) {
        const previewModal = new bootstrap.Modal(document.getElementById('imagePreviewModal'));
        document.getElementById('previewImage').src = url;
        previewModal.show();
    }

    /**
     * Deletes an image from the array
     */    function deleteImage(index) {
        if (confirm('¿Estás seguro de que deseas eliminar esta imagen?')) {
            const deletedImage = routeImages[index];

            // If it's an existing image (not a newly uploaded one), save its URL
            if (!deletedImage.isNewImage && typeof deletedImage === 'string') {
                // Add to the list of deleted images only if it's not already there
                if (!deletedImageUrls.includes(deletedImage)) {
                    deletedImageUrls.push(deletedImage);
                }
            } else if (deletedImage.isNewImage) {
                // If it's a new image, remove from the array of new files
                const fileIndex = newImageFiles.findIndex((file, i) =>
                    i === routeImages.findIndex(img => img === deletedImage) - routeImages.filter(img => !img.isNewImage).length
                );

                if (fileIndex !== -1) {
                    newImageFiles.splice(fileIndex, 1);
                }
            }
            // Remove the image from the images array
            routeImages.splice(index, 1);

            // Update the display
            renderImageCards();
            updateNoImagesMessage();

            // Display a confirmation message
            showToast('Imagen eliminada', 'La imagen ha sido eliminada correctamente.');
        }
    }

    /**
     * Shows the save confirmation modal
     */
    function showSaveConfirmation() {
        if (!validateForm()) {
            return;
        }

        const saveModal = new bootstrap.Modal(document.getElementById('saveConfirmModal'));
        saveModal.show();
    }

    /**
     * Shows the cancel confirmation modal
     */
    function showCancelConfirmation() {
        const cancelModal = new bootstrap.Modal(document.getElementById('cancelConfirmModal'));
        cancelModal.show();
    }
    /**
     * Submits the form to save the route
     */

    function saveRoute() {
        const form = document.getElementById('routeForm');

        if (!validateForm()) {
            return;
        }

        // Check if we need to calculate the route first (if route points exist but calculated route doesn't)
        if (routePoints.length >= 2 && (!window.calculatedRoutePoints || !document.getElementById('routeInfoPanel'))) {
            const saveBtn = document.getElementById('confirmSave');
            const originalText = saveBtn.innerHTML;
            saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Calculando ruta...';
            saveBtn.disabled = true;

            const headers = {
                'Content-Type': 'application/json',
            };

            try {
                const csrfToken = document.querySelector("meta[name='_csrf']");
                const csrfHeader = document.querySelector("meta[name='_csrf_header']");

                if (csrfToken && csrfHeader) {
                    headers[csrfHeader.getAttribute("content")] = csrfToken.getAttribute("content");
                }
            } catch (error) {
                console.log("CSRF token not found, proceeding without it");
            }

            // Calculate route before saving
            fetch('/admin/routes/calculate', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({
                    points: routePoints.map(point => ({
                        lat: point.lat,
                        lng: point.lng
                    }))
                })
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(`Error del servidor: ${response.status} - ${text || response.statusText}`);
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    saveBtn.innerHTML = originalText;
                    saveBtn.disabled = false;

                    if (data.success && data.data) {
                        window.calculatedRoutePoints = data.data.routePoints;
                        window.calculatedEstimatedTimeMinutes = data.data.estimatedTimeMinutes;
                        window.calculatedTotalDistanceKm = data.data.totalDistanceKm;

                        // Continue with form submission after successful calculation
                        submitFormWithCalculatedRoute();
                    } else {
                        showToast('Error', data.message || 'Error al calcular la ruta', 'error');
                        // Hide the confirmation modal if it's still open
                        const saveModal = bootstrap.Modal.getInstance(document.getElementById('saveConfirmModal'));
                        if (saveModal) {
                            saveModal.hide();
                        }
                    }
                })
                .catch(error => {
                    saveBtn.innerHTML = originalText;
                    saveBtn.disabled = false;
                    showToast('Error', 'Error al calcular la ruta: ' + error.message, 'error');
                    // Hide the confirmation modal if it's still open
                    const saveModal = bootstrap.Modal.getInstance(document.getElementById('saveConfirmModal'));
                    if (saveModal) {
                        saveModal.hide();
                    }
                });
        } else {
            // Route already calculated or not enough points, proceed with form submission
            submitFormWithCalculatedRoute();
        }
    }

    /**
     * Helper function to actually submit the form with all data
     */
    function submitFormWithCalculatedRoute() {
        const form = document.getElementById('routeForm');
        const fileInput = document.getElementById('imageUpload');

        // Add the new images to the file input
        if (newImageFiles.length > 0) {
            const dataTransfer = new DataTransfer();

            newImageFiles.forEach(file => {
                dataTransfer.items.add(file);
            });

            fileInput.files = dataTransfer.files;
        }

        // Create hidden fields for existing images (not new or deleted ones)
        document.querySelectorAll('input[name="existingImageUrls"]').forEach(el => el.remove());

        // Only save URLs of existing images (strings), never objects with blob URLs
        const existingUrls = routeImages.filter(img => typeof img === 'string');

        // Add existing URLs as hidden inputs
        existingUrls.forEach(url => {
            const existingInput = document.createElement('input');
            existingInput.type = 'hidden';
            existingInput.name = 'existingImageUrls';
            existingInput.value = url;
            form.appendChild(existingInput);
        });

        // Add deleted image URLs as hidden input fields
        if (deletedImageUrls && deletedImageUrls.length > 0) {
            // Remove any old deleted URL inputs first
            document.querySelectorAll('input[name="deletedImageUrls"]').forEach(el => el.remove());

            // Add new hidden fields for each deleted URL
            deletedImageUrls.forEach(url => {
                const deletedInput = document.createElement('input');
                deletedInput.type = 'hidden';
                deletedInput.name = 'deletedImageUrls';
                deletedInput.value = url;
                form.appendChild(deletedInput);
            });
        }

        // Create a hidden field for route points coordinates if it doesn't exist
        let coordinatesInput = document.getElementById('coordinatesInput');
        if (!coordinatesInput) {
            coordinatesInput = document.createElement('input');
            coordinatesInput.type = 'hidden';
            coordinatesInput.id = 'coordinatesInput';
            coordinatesInput.name = 'coordinatesInput';
            form.appendChild(coordinatesInput);
        }

        // Convert the route points to JSON and set the value
        const routePointsJson = JSON.stringify(routePoints.map(point => ({
            lat: point.lat,
            lng: point.lng
        })));
        coordinatesInput.value = routePointsJson;

        // Add calculated route data if available
        if (window.calculatedRoutePoints) {
            // Add calculated route points
            let calculatedRouteInput = document.getElementById('calculatedRouteInput');
            if (!calculatedRouteInput) {
                calculatedRouteInput = document.createElement('input');
                calculatedRouteInput.type = 'hidden';
                calculatedRouteInput.id = 'calculatedRouteInput';
                calculatedRouteInput.name = 'calculatedRouteInput';
                form.appendChild(calculatedRouteInput);
            }

            // Ensure that the calculated waypoints are formatted correctly
            // If they are [lng, lat] arrays, we convert them to {lat, lng} objects
            const formattedPoints = window.calculatedRoutePoints.map(point => {
                if (Array.isArray(point)) {
                    // Convert from GeoJSON format [lng, lat] to {lat, lng} format
                    return {
                        lat: point[1],
                        lng: point[0]
                    };
                } else if (point.lat !== undefined && point.lng !== undefined) {
                    return {
                        lat: point.lat,
                        lng: point.lng
                    };
                }
                return null;
            }).filter(point => point !== null);

            calculatedRouteInput.value = JSON.stringify(formattedPoints);

            // Add estimated time
            if (window.calculatedEstimatedTimeMinutes) {
                let timeInput = document.getElementById('calculatedTimeInput');
                if (!timeInput) {
                    timeInput = document.createElement('input');
                    timeInput.type = 'hidden';
                    timeInput.id = 'calculatedTimeInput';
                    timeInput.name = 'calculatedEstimatedTimeMinutes';
                    form.appendChild(timeInput);
                }
                timeInput.value = window.calculatedEstimatedTimeMinutes;
            }

            // Add total distance
            if (window.calculatedTotalDistanceKm) {
                let distanceInput = document.getElementById('calculatedDistanceInput');
                if (!distanceInput) {
                    distanceInput = document.createElement('input');
                    distanceInput.type = 'hidden';
                    distanceInput.id = 'calculatedDistanceInput';
                    distanceInput.name = 'calculatedTotalDistanceKm';
                    form.appendChild(distanceInput);
                }
                distanceInput.value = window.calculatedTotalDistanceKm;
            }
        }

        // Submit the form
        form.submit();
    }

    /**
     * Cancels the editing and redirects to the routes list
     */
    function cancelEditing() {
        window.location.href = '/admin/routes';
    }    /**
     * Validates that the form has all the required fields
     */
    function validateForm() {
        const requiredFields = ['title', 'city', 'difficulty', 'description'];
        let isValid = true;

        requiredFields.forEach(field => {
            const element = document.getElementById(field);
            if (!element.value.trim()) {
                element.classList.add('is-invalid');
                isValid = false;
            } else {
                element.classList.remove('is-invalid');
            }
        });        // Validate that there's at least two route points
        if (routePoints.length < 2) {
            showError('Debes añadir al menos dos puntos a la ruta');
            isValid = false;
        }

        if (!isValid) {
            alert('Por favor, completa todos los campos obligatorios y añade al menos dos puntos a la ruta.');
        }

        return isValid;
    }

    // Description field handling
    const descriptionElement = document.getElementById('description');
    const charCountElement = document.getElementById('charCount');
    const maxLength = descriptionElement.getAttribute('maxlength') || 1500;

    /**
     * Updates the character counter display with appropriate styling
     */
    function updateCharCount() {
        const currentLength = descriptionElement.value.length;
        charCountElement.textContent = `${currentLength}/${maxLength} caracteres`;

        if (currentLength >= maxLength) {
            charCountElement.classList.remove('bg-info', 'bg-warning');
            charCountElement.classList.add('bg-danger');
        } else if (currentLength >= maxLength - 150) {
            charCountElement.classList.remove('bg-info', 'bg-danger');
            charCountElement.classList.add('bg-warning');
        } else {
            charCountElement.classList.remove('bg-warning', 'bg-danger');
            charCountElement.classList.add('bg-info');
        }
    }

    /**
     * Adjusts the textarea height to fit its content and removes the scrollbar
     */
    function autoResize() {
        descriptionElement.style.height = 'auto';
        descriptionElement.style.height = (descriptionElement.scrollHeight) + 'px';
        descriptionElement.style.overflowY = 'hidden';
    }

    // Initialize the counter and sizing
    updateCharCount();
    autoResize();

    // Set up event listeners
    descriptionElement.addEventListener('input', function () {
        updateCharCount();
        autoResize();
    });

    // Also handle size changes when window resizes
    window.addEventListener('resize', autoResize);    /**
     * Calculates a realistic route based on the current points
     */    function calculateRoute() {
        if (routePoints.length < 2) {
            showToast('Error', 'Se necesitan al menos 2 puntos para calcular una ruta', 'error');
            return;
        }

        if (routePoints.length > 50) {
            showToast('Error', 'Máximo 50 puntos permitidos para calcular una ruta', 'error');
            return;
        }

        // Show loading indicator
        const calculateBtn = document.getElementById('calculateRouteButton');
        const originalText = calculateBtn.innerHTML;
        calculateBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Calculando...';
        calculateBtn.disabled = true;

        // Build headers with CSRF token
        const headers = {
            'Content-Type': 'application/json',
        };

        // More robust CSRF token handling
        let csrfToken = null;
        let csrfHeaderName = null;

        // First try to get it from meta tags
        try {
            const metaToken = document.querySelector("meta[name='_csrf']");
            const metaHeader = document.querySelector("meta[name='_csrf_header']");

            if (metaToken && metaHeader) {
                csrfToken = metaToken.getAttribute("content");
                csrfHeaderName = metaHeader.getAttribute("content");
                console.log("CSRF Token found in meta tags:", csrfToken ? "yes" : "no");
            }
        } catch (error) {
            console.warn("Error getting CSRF token from meta tags:", error);
        }

        // If not found in meta, try to find it in form inputs
        if (!csrfToken) {
            try {
                const inputToken = document.querySelector("input[name='_csrf']");
                if (inputToken) {
                    csrfToken = inputToken.value;
                    csrfHeaderName = "X-CSRF-TOKEN"; // Default header name in Spring Security
                    console.log("CSRF Token found in form input");
                }
            } catch (error) {
                console.warn("Error getting CSRF token from form inputs:", error);
            }
        }

        // Add token to headers if found
        if (csrfToken && csrfHeaderName) {
            headers[csrfHeaderName] = csrfToken;
        } else {
            console.warn("CSRF tokens not found, proceeding without CSRF protection");
        }

        // Log headers for debugging
        console.log("Request headers:", Object.keys(headers).join(", "));

        // Send request to the server to calculate the route
        fetch('/admin/routes/calculate', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                points: routePoints.map(point => ({
                    lat: point.lat,
                    lng: point.lng
                }))
            }),
            // Include credentials for CSRF
            credentials: 'same-origin'
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        // Try to parse as JSON first
                        try {
                            const errorJson = JSON.parse(text);
                            throw new Error(`Error: ${errorJson.message || response.statusText}`);
                        } catch (e) {
                            // If not JSON, use as text
                            throw new Error(`Error del servidor: ${response.status} - ${text || response.statusText}`);
                        }
                    });
                }
                return response.json();
            })
            .then(data => {
                // Reset button state
                calculateBtn.innerHTML = originalText;
                calculateBtn.disabled = false;

                if (data.success && data.data && data.data.routePoints && data.data.routePoints.length > 0) {
                    // Store calculated data for form submission
                    window.calculatedRoutePoints = data.data.routePoints;
                    window.calculatedEstimatedTimeMinutes = data.data.estimatedTimeMinutes;
                    window.calculatedTotalDistanceKm = data.data.totalDistanceKm;

                    // Display route info
                    displayCalculatedRouteInfo(data.data);

                    // Draw the calculated route on the map
                    drawCalculatedRoute(data.data.routePoints);

                    showToast('Ruta calculada', 'La ruta ha sido calculada exitosamente', 'success');
                } else {
                    showToast('Error', data.message || 'Error al calcular la ruta', 'error');
                }
            })
            .catch(error => {
                console.error("Route calculation error:", error);
                // Always reset button state on error
                calculateBtn.innerHTML = originalText;
                calculateBtn.disabled = false;
                showToast('Error', 'Error al calcular la ruta: ' + error.message, 'error');
            });
    }/**
     * Displays the route information panel with time and distance
     */
    function displayCalculatedRouteInfo(routeData) {
        // Remove existing panel if there is one
        const existingPanel = document.getElementById('routeInfoPanel');
        if (existingPanel) {
            existingPanel.remove();
        }

        // Create the panel to show calculated route information
        const panel = document.createElement('div');
        panel.id = 'routeInfoPanel';
        panel.className = 'route-info-panel mt-3';

        const timeInMinutes = routeData.estimatedTimeMinutes;
        let displayTime = '';

        if (timeInMinutes >= 60) {
            const hours = Math.floor(timeInMinutes / 60);
            const minutes = Math.round(timeInMinutes % 60);
            displayTime = `${hours} h ${minutes} min`;
        } else {
            displayTime = `${Math.round(timeInMinutes)} min`;
        }

        const distanceInKm = routeData.totalDistanceKm.toFixed(2);

        panel.innerHTML = `
            <div class="route-info-content">
                <div class="route-info-detail">
                    <i class="fas fa-clock"></i>
                    <span>Tiempo estimado: ${displayTime}</span>
                </div>
                <div class="route-info-detail">
                    <i class="fas fa-route"></i>
                    <span>Distancia total: ${distanceInKm} km</span>
                </div>
            </div>
        `;
        // Add it after the map card
        const mapElement = document.getElementById('map');
        const mapCard = mapElement ? mapElement.closest('.card') : null;
        if (mapCard) {
            mapCard.parentNode.insertBefore(panel, mapCard.nextSibling);
        }
    }

    /**
     * Draws the calculated route as a dashed green line on the map
     */
    function drawCalculatedRoute(routePoints) {
        // Remove existing calculated route line
        if (calculatedRouteLine) {
            map.removeLayer(calculatedRouteLine);
        }

        if (!routePoints || routePoints.length < 2) {
            return;
        }

        try {
            // Convert the GeoJSON coordinates [lng, lat] format to Leaflet's [lat, lng] format
            const leafletLatLngs = routePoints.map(point => {
                // Check if this is a GeoJSON [lng, lat] format or if it already has lat/lng properties
                if (Array.isArray(point)) {
                    return [point[1], point[0]]; // Convert [lng, lat] to [lat, lng]
                } else if (point.lat !== undefined && point.lng !== undefined) {
                    return [point.lat, point.lng];
                }
                return null;
            }).filter(point => point !== null);

            // Create the polyline
            calculatedRouteLine = L.polyline(leafletLatLngs, {
                color: 'green',
                weight: 5,
                opacity: 0.7,
                className: 'calculated-route-line'
            }).addTo(map);

            // Ensure the whole route is visible
            map.fitBounds(calculatedRouteLine.getBounds(), {
                padding: [30, 30]
            });
        } catch (error) {
            console.error('Error drawing calculated route:', error);
            showToast('Error', 'Error al dibujar la ruta calculada', 'error');
        }
    }
    // Global event listener for the calculate route button
    document.getElementById('calculateRouteButton').addEventListener('click', calculateRoute);
});
