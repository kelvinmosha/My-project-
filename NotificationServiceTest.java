package hostel_management_system.uhms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.university.hostel.entity.NotificationType;

class NotificationServiceTest {

    @Test
    void shouldHaveApplicationStatusType() {
        assertEquals("APPLICATION_STATUS", NotificationType.APPLICATION_STATUS.name());
    }
}
