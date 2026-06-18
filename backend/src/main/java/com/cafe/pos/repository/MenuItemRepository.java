package com.cafe.pos.repository;

import com.cafe.pos.model.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByActiveTrueOrderByCategoryAscNameAsc();
}
