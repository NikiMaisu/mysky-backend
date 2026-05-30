package ge.mysky.backend.service;

import ge.mysky.backend.domain.Material;
import ge.mysky.backend.dto.MaterialRequest;
import ge.mysky.backend.repository.MaterialRepository;
import ge.mysky.backend.web.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialService {

    private final MaterialRepository materials;

    public MaterialService(MaterialRepository materials) {
        this.materials = materials;
    }

    @Transactional(readOnly = true)
    public List<Material> list(boolean includeInactive) {
        return includeInactive ? materials.findAll() : materials.findAllByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Material get(Long id) {
        return materials.findById(id)
                .orElseThrow(() -> new NotFoundException("Material " + id + " not found"));
    }

    @Transactional
    public Material create(MaterialRequest req) {
        var material = Material.builder()
                .name(req.name().trim())
                .pricePerM2(req.pricePerM2())
                .timePerM2Minutes(req.timePerM2Minutes())
                .active(true)
                .build();
        return materials.save(material);
    }

    @Transactional
    public Material update(Long id, MaterialRequest req) {
        var material = get(id);
        material.setName(req.name().trim());
        material.setPricePerM2(req.pricePerM2());
        material.setTimePerM2Minutes(req.timePerM2Minutes());
        return materials.save(material);
    }

    @Transactional
    public void delete(Long id) {
        var material = get(id);
        material.setActive(false);
        materials.save(material);
    }
}
