package com.cafe.pos.service;

import com.cafe.pos.dto.Dtos.MenuItemResponse;
import com.cafe.pos.repository.MenuItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {
    private final MenuItemRepository menuItemRepository;

    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> activeMenu() {
        return menuItemRepository.findByActiveTrueOrderByCategoryAscNameAsc().stream()
                .map(item -> new MenuItemResponse(item.getId(), item.getName(), item.getCategory(), item.getDescription(), item.getPrice()))
                .toList();
    }
}
