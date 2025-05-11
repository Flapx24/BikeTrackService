document.addEventListener('DOMContentLoaded', function () {    // Global variables
    let map;
    let marker;
    let workshopImages = [];
    let newImageFiles = [];
    let currentImageIndex = -1;
    let deletedImageUrls = [];
    const DEFAULT_LAT = 40.416775; // Madrid as default point
    const DEFAULT_LNG = -3.703790;
    let tempMarker = null;
    let isCoordinateIterationMode = false;
    let draggedCard = null;
    let dragOverIndex = -1;

    initMap();

    loadExistingImages();

    document.getElementById('imageUpload').addEventListener('change', handleImageUpload);
    document.getElementById('saveButton').addEventListener('click', showSaveConfirmation);
    document.getElementById('cancelButton').addEventListener('click', showCancelConfirmation);
    document.getElementById('confirmSave').addEventListener('click', saveWorkshop);
    document.getElementById('confirmCancel').addEventListener('click', cancelEditing);
    document.getElementById('confirmCoordinates').addEventListener('click', confirmCoordinateSelection);
    document.getElementById('cancelCoordinates').addEventListener('click', cancelCoordinateSelection);

    /**
     * Initializes the map with Leaflet
     */
    function initMap() {
        const latElement = document.getElementById('latitude');
        const lngElement = document.getElementById('longitude');

        const lat = latElement.value ? parseFloat(latElement.value) : DEFAULT_LAT;
        const lng = lngElement.value ? parseFloat(lngElement.value) : DEFAULT_LNG;

        map = L.map('map').setView([lat, lng], 13);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        if (latElement.value && lngElement.value) {
            marker = L.marker([lat, lng]).addTo(map);
        }

        map.on('click', function (e) {
            showCoordinateConfirmation(e.latlng);
        });

        setTimeout(() => {
            map.invalidateSize();
        }, 100);
    }

    /**
     * Shows a confirmation dialog for the selected coordinates
     */
    function showCoordinateConfirmation(latlng) {
        if (tempMarker) {
            tempMarker.setLatLng(latlng);
        } else {
            tempMarker = L.marker(latlng, { opacity: 0.6 }).addTo(map);
        }

        document.getElementById('selectedLatitude').textContent = latlng.lat.toFixed(6);
        document.getElementById('selectedLongitude').textContent = latlng.lng.toFixed(6);

        tempMarker.tempLatLng = latlng;

        const modalTitle = document.getElementById('coordinateConfirmModalLabel');
        const modalBodyText = document.getElementById('coordinateConfirmModal').querySelector('.modal-body p:last-child');
        const cancelButton = document.getElementById('cancelCoordinates');


        modalTitle.textContent = 'Confirmar coordenadas';
        modalBodyText.textContent = '¿Deseas confirmar estas coordenadas o elegir otra ubicación?';
        cancelButton.textContent = 'Elegir otra ubicación';

        const coordModal = new bootstrap.Modal(document.getElementById('coordinateConfirmModal'));
        coordModal.show();
    }

    /**
     * Confirms the coordinate selection
     */
    function confirmCoordinateSelection() {
        if (tempMarker && tempMarker.tempLatLng) {
            setMarkerPosition(tempMarker.tempLatLng);

            if (tempMarker !== marker) {
                map.removeLayer(tempMarker);
                tempMarker = null;
            }

            isCoordinateIterationMode = false;
        }

        bootstrap.Modal.getInstance(document.getElementById('coordinateConfirmModal')).hide();
    }

    /**
     * Cancels the current coordinate selection and enters iteration mode
     */
    function cancelCoordinateSelection() {
        if (tempMarker) {
            tempMarker.setOpacity(0.4);

            isCoordinateIterationMode = true;

            showToast('Modo de edición activado', 'Haz clic en el mapa para seleccionar otras coordenadas.');
        }

        bootstrap.Modal.getInstance(document.getElementById('coordinateConfirmModal')).hide();
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
     * Sets the marker position on the map
     */
    function setMarkerPosition(latlng) {
        document.getElementById('latitude').value = latlng.lat.toFixed(6);
        document.getElementById('longitude').value = latlng.lng.toFixed(6);

        if (marker) {
            marker.setLatLng(latlng);
        } else {
            marker = L.marker(latlng).addTo(map);
        }
    }
    /**
    * Loads existing workshop images from the global variable
    * which is defined in the HTML using Thymeleaf
    */
    function loadExistingImages() {
        const existingImagesStr = document.getElementById('existingImages')?.value;
        if (typeof existingImages !== 'undefined' && existingImages.length > 0) {
            workshopImages = [...existingImages];
            renderImageCards();
        }
        updateNoImagesMessage();
    }
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
            newImageFiles.push(file); // Create an object for the image instead of directly modifying the URL
            const imageObj = {
                url: URL.createObjectURL(file),
                isNewImage: true,
                file: file
            };

            workshopImages.push(imageObj);
        }

        renderImageCards();
        updateNoImagesMessage();

        event.target.value = '';
    }

    /**
    * Renders the image cards in the container
    */
    function renderImageCards() {
        const container = document.getElementById('imagesContainer');
        container.innerHTML = '';
        // Create a hidden field to send all existing URLs as JSON
        const imageUrlsInput = document.getElementById('imageUrlsInput');
        const existingUrls = workshopImages
            .filter(img => !img.isNewImage && typeof img === 'string')
            .map(url => url);

        if (imageUrlsInput) {
            imageUrlsInput.value = JSON.stringify(existingUrls);
        }

        workshopImages.forEach((img, index) => {
            const card = document.createElement('div');
            card.className = 'image-card';
            card.draggable = true;
            card.dataset.index = index;

            card.addEventListener('dragstart', handleDragStart);
            card.addEventListener('dragend', handleDragEnd);
            card.addEventListener('dragover', handleDragOver);
            card.addEventListener('dragleave', handleDragLeave);
            card.addEventListener('drop', handleDrop);

            const imgElement = document.createElement('img');
            // Determine the image source based on its type            
            if (typeof img === 'string') {
                imgElement.src = img; // Existing URL
            } else if (img && img.url) {
                imgElement.src = img.url; // Object with temporary URL
            }

            imgElement.alt = `Image ${index + 1}`;
            card.appendChild(imgElement);

            const overlay = document.createElement('div');
            overlay.className = 'image-overlay';

            const viewBtn = createActionButton('action-view', 'fa-eye', () => {
                // Determine which URL to use for the preview
                const previewUrl = typeof img === 'string' ? img : img.url;
                showImagePreview(previewUrl);
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
        dragOverIndex = -1;
    }

    /**
     * Handles when an image is dragged over another image
     */
    function handleDragOver(e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';

        if (this !== draggedCard) {
            this.classList.add('drag-over');
            dragOverIndex = parseInt(this.dataset.index);
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

            const [imageUrl] = workshopImages.splice(fromIndex, 1);
            workshopImages.splice(toIndex, 0, imageUrl);

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
    */
    function updateNoImagesMessage() {
        const noImagesMessage = document.getElementById('noImagesMessage');
        const dragInstructionMessage = document.getElementById('dragInstructionMessage');

        if (workshopImages.length === 0) {
            if (noImagesMessage) noImagesMessage.style.display = 'block';
            if (dragInstructionMessage) dragInstructionMessage.style.display = 'none';
        } else {
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
    }    /**
     * Deletes an image from the array
     */
    function deleteImage(index) {
        if (confirm('¿Estás seguro de que deseas eliminar esta imagen?')) {
            const deletedImage = workshopImages[index];

            if (deletedImage && deletedImage.isNewImage) {
                // If it's a new image, simply remove it from the array of new files
                const fileIndex = newImageFiles.findIndex((file) => {
                    return deletedImage.file === file;
                });

                if (fileIndex !== -1) {
                    newImageFiles.splice(fileIndex, 1);
                }

                // Revoke the temporary URL to free memory
                if (deletedImage.url) {
                    URL.revokeObjectURL(deletedImage.url);
                }
            } else if (typeof deletedImage === 'string') {
                // If it's an existing image, add it to the list of deleted images
                if (!deletedImageUrls.includes(deletedImage)) {
                    deletedImageUrls.push(deletedImage);
                }
            }

            workshopImages.splice(index, 1);
            renderImageCards();
            updateNoImagesMessage();

            showToast('Imagen eliminada', 'La imagen ha sido eliminada correctamente.');
        }
    }

    /**
     * Shows the save confirmation modal
     */
    function showSaveConfirmation() {
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
    * Submits the form to save the workshop
    */
    function saveWorkshop() {
        if (!validateForm()) {
            bootstrap.Modal.getInstance(document.getElementById('saveConfirmModal')).hide();
            return;
        }

        const form = document.getElementById('workshopForm');
        const fileInput = document.getElementById('imageUpload');

        // Add new files to the file input
        if (newImageFiles.length > 0) {
            const dataTransfer = new DataTransfer();

            newImageFiles.forEach(file => {
                dataTransfer.items.add(file);
            });

            fileInput.files = dataTransfer.files;
        }

        // Clean existing inputs if any
        document.querySelectorAll('input[name="existingImageUrls"]').forEach(el => el.remove());

        // Only save URLs of existing images (strings), never objects with blob URLs
        const existingUrls = workshopImages.filter(img => typeof img === 'string');
        // Add existing URLs as hidden inputs
        existingUrls.forEach(url => {
            const existingInput = document.createElement('input');
            existingInput.type = 'hidden';
            existingInput.name = 'existingImageUrls';
            existingInput.value = url;
            form.appendChild(existingInput);
        });

        // Add deleted image URLs as hidden inputs
        if (deletedImageUrls && deletedImageUrls.length > 0) {
            // Remove old inputs if they exist
            document.querySelectorAll('input[name="deletedImageUrls"]').forEach(el => el.remove());

            // Add inputs for each deleted URL
            deletedImageUrls.forEach(url => {
                const deletedInput = document.createElement('input');
                deletedInput.type = 'hidden';
                deletedInput.name = 'deletedImageUrls';
                deletedInput.value = url;
                form.appendChild(deletedInput);
            });
        }

        form.submit();
    }

    /**
     * Cancels the editing and redirects to the workshops list
     */
    function cancelEditing() {
        window.location.href = '/admin/workshops';
    }

    /**
     * Validates that the form has all the required fields
     */
    function validateForm() {
        const requiredFields = ['name', 'city', 'address', 'latitude', 'longitude'];
        let isValid = true;

        requiredFields.forEach(field => {
            const element = document.getElementById(field);
            if (!element.value.trim()) {
                element.classList.add('is-invalid');
                isValid = false;
            } else {
                element.classList.remove('is-invalid');
            }
        });

        if (!isValid) {
            alert('Por favor, completa todos los campos obligatorios.');
        }

        return isValid;
    }
});