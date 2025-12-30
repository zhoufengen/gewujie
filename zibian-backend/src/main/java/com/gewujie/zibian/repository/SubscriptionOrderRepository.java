package com.gewujie.zibian.repository;

import com.gewujie.zibian.model.SubscriptionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubscriptionOrderRepository extends JpaRepository<SubscriptionOrder, Long> {
    List<SubscriptionOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    SubscriptionOrder findByOrderId(String orderId);
}
