package ge.mysky.backend.service;

import ge.mysky.backend.domain.AddonCategory;
import ge.mysky.backend.domain.AddonInstance;
import ge.mysky.backend.dto.AddonRequest;
import ge.mysky.backend.repository.AddonInstanceRepository;
import ge.mysky.backend.web.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddonService {

    private final AddonInstanceRepository addons;

    public AddonService(AddonInstanceRepository addons) {
        this.addons = addons;
    }

    @Transactional(readOnly = true)
    public List<AddonInstance> list(AddonCategory category, boolean includeInactive) {
        if (includeInactive) {
            return addons.findAll();
        }
        return category == null
                ? addons.findAllByActiveTrueOrderByNameAsc()
                : addons.findAllByActiveTrueAndCategoryOrderByNameAsc(category);
    }

    @Transactional(readOnly = true)
    public AddonInstance get(Long id) {
        return addons.findById(id)
                .orElseThrow(() -> new NotFoundException("Add-on " + id + " not found"));
    }

    @Transactional
    public AddonInstance create(AddonRequest req) {
        var addon = AddonInstance.builder()
                .name(req.name().trim())
                .category(req.category() != null ? req.category() : AddonCategory.OTHER)
                .cost(req.cost())
                .installTimeMinutes(req.installTimeMinutes())
                .active(true)
                .build();
        return addons.save(addon);
    }

    @Transactional
    public AddonInstance update(Long id, AddonRequest req) {
        var addon = get(id);
        addon.setName(req.name().trim());
        addon.setCategory(req.category() != null ? req.category() : AddonCategory.OTHER);
        addon.setCost(req.cost());
        addon.setInstallTimeMinutes(req.installTimeMinutes());
        return addons.save(addon);
    }

    @Transactional
    public void delete(Long id) {
        var addon = get(id);
        addon.setActive(false);
        addons.save(addon);
    }
}
