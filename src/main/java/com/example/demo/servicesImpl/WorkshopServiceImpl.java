package com.example.demo.servicesImpl;

import java.text.Normalizer;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.Workshop;
import com.example.demo.repositories.WorkshopRepository;
import com.example.demo.services.WorkshopService;

@Service("workshopService")
public class WorkshopServiceImpl implements WorkshopService {

    @Autowired
    @Qualifier("workshopRepository")
    private WorkshopRepository workshopRepository;

    @Override
    public Workshop findById(Long id) {
        return workshopRepository.findById(id).orElse(null);
    }

    @Override
    public List<Workshop> findByCity(String city) {
        return workshopRepository.findByCity(normalizeCity(city));
    }

    @Override
    public Workshop saveWorkshop(Workshop workshop) {
        return workshopRepository.save(workshop);
    }

    @Override
    public List<Workshop> findAll() {
        return workshopRepository.findAll();
    }

    @Override
    @Transactional
    public boolean deleteWorkshop(Long id) {
        if (workshopRepository.existsById(id)) {
            workshopRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public String normalizeCity(String city) {
        return normalizeString(city);
    }

    @Override
    public String normalizeString(String text) {
        if (text == null) {
            return "";
        }

        String normalized = text.toLowerCase();

        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "");

        return normalized;
    }

    @Override
    public Page<Workshop> getFilteredWorkshopsPaginated(String city, String name, Pageable pageable) {
        return workshopRepository.findByCityContainingAndNameContainingIgnoreCasePaginated(city, name, pageable);
    }
}