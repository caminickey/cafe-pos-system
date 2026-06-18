package com.cafe.pos.repository;

import com.cafe.pos.model.CafeOrder;
import com.cafe.pos.model.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CafeOrder, Long> {
    List<CafeOrder> findByStatusOrderByUpdatedAtDesc(OrderStatus status);
}
