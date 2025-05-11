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

    initMap();

    loadExistingImages();

    document.getElementById('imageUpload').addEventListener('change', handleImageUpload);
    document.getElementById('saveButton').addEventListener('click', showSaveConfirmation);
    document.getElementById('cancelButton').addEventListener('click', showCancelConfirmation);
    document.getElementById('confirmSave').addEventListener('click', saveRoute);
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
     * Shows a toast message
     */
    function showToast(title, message) {
        let toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'toastContainer';
            toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            document.body.appendChild(toastContainer);
        }

        const toastId = 'toast-' + Date.now();
        const toastHtml = `
            <div id="${toastId}" class="toast bg-dark text-white" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="toast-header bg-dark text-white">
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
            delay: 3000
        });
        toast.show();
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

            // Mostrar un mensaje de confirmación
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
    }    /**
     * Submits the form to save the route
     */

    function saveRoute() {
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
            console.log('Sending deleted image URLs:', deletedImageUrls);
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
    window.addEventListener('resize', autoResize);
});
