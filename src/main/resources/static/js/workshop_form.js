document.addEventListener('DOMContentLoaded', function() {
    // Global variables
    let map;
    let marker;
    let workshopImages = [];
    let newImageFiles = [];
    let currentImageIndex = -1;
    const DEFAULT_LAT = 40.416775; // Madrid as default point
    const DEFAULT_LNG = -3.703790;
    let tempMarker = null;
    let isCoordinateIterationMode = false;
    
    initMap();
    
    loadExistingImages();
    
    document.getElementById('imageUpload').addEventListener('change', handleImageUpload);
    document.getElementById('saveButton').addEventListener('click', showSaveConfirmation);
    document.getElementById('cancelButton').addEventListener('click', showCancelConfirmation);
    document.getElementById('confirmSave').addEventListener('click', saveWorkshop);
    document.getElementById('confirmCancel').addEventListener('click', cancelEditing);
    document.getElementById('confirmReorder').addEventListener('click', reorderImage);
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
        
        map.on('click', function(e) {
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
            tempMarker = L.marker(latlng, {opacity: 0.6}).addTo(map);
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
        if (typeof existingImages !== 'undefined' && existingImages.length > 0) {
            workshopImages = [...existingImages];
            renderImageCards();
            updateNoImagesMessage();
        }
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
            newImageFiles.push(file);

            const tempUrl = URL.createObjectURL(file);
            tempUrl.isNewImage = true;
            workshopImages.push(tempUrl);
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

        workshopImages.forEach((url) => {
            if (!url.isNewImage && typeof url === 'string') {
                const hiddenInput = document.createElement('input');
                hiddenInput.type = 'hidden';
                hiddenInput.name = 'existingImageUrls';
                hiddenInput.value = url;
                container.appendChild(hiddenInput);
            }
        });

        workshopImages.forEach((url, index) => {
            const card = document.createElement('div');
            card.className = 'image-card';

            const img = document.createElement('img');
            img.src = url;
            img.alt = `Imagen ${index + 1}`;
            card.appendChild(img);

            const overlay = document.createElement('div');
            overlay.className = 'image-overlay';

            const viewBtn = createActionButton('action-view', 'fa-eye', () => {
                showImagePreview(url);
            });

            const reorderBtn = createActionButton('action-reorder', 'fa-arrows-up-down-left-right', () => {
                showReorderModal(index);
            });

            const deleteBtn = createActionButton('action-delete', 'fa-trash', () => {
                deleteImage(index);
            });
            
            overlay.appendChild(viewBtn);
            overlay.appendChild(reorderBtn);
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
        if (noImagesMessage) {
            noImagesMessage.style.display = workshopImages.length === 0 ? 'block' : 'none';
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
     * Shows the modal to reorder an image
     */
    function showReorderModal(index) {
        currentImageIndex = index;
        const reorderModal = new bootstrap.Modal(document.getElementById('reorderModal'));
        const newPositionInput = document.getElementById('newPosition');

        newPositionInput.value = index + 1;
        newPositionInput.min = 1;
        newPositionInput.max = workshopImages.length;
        
        reorderModal.show();
    }
    
    /**
     * Reorders the image to the new position
     */
    function reorderImage() {
        const newPosition = parseInt(document.getElementById('newPosition').value);
        
        if (isNaN(newPosition) || newPosition < 1 || newPosition > workshopImages.length) {
            alert('Por favor, introduce una posición válida.');
            return;
        }

        const newIndex = newPosition - 1;
        
        if (currentImageIndex !== newIndex) {

            const [imageUrl] = workshopImages.splice(currentImageIndex, 1);
            workshopImages.splice(newIndex, 0, imageUrl);

            renderImageCards();
        }

        bootstrap.Modal.getInstance(document.getElementById('reorderModal')).hide();
    }
    
    /**
     * Deletes an image from the array
     */
    function deleteImage(index) {
        if (confirm('¿Estás seguro de que deseas eliminar esta imagen?')) {
            const deletedImage = workshopImages[index];

            if (deletedImage.isNewImage) {
                const fileIndex = newImageFiles.findIndex((file, i) => 
                    i === workshopImages.findIndex(img => img === deletedImage) - workshopImages.filter(img => !img.isNewImage).length
                );
                
                if (fileIndex !== -1) {
                    newImageFiles.splice(fileIndex, 1);
                }
            }

            workshopImages.splice(index, 1);
            renderImageCards();
            updateNoImagesMessage();
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

        if (newImageFiles.length > 0) {
            const dataTransfer = new DataTransfer();

            newImageFiles.forEach(file => {
                dataTransfer.items.add(file);
            });

            fileInput.files = dataTransfer.files;
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