-- BookingRepository.findByCreatedBy and PassengerRepository.findByCreatedBy back
-- the "show me my records" path for every non-admin request. Without these the
-- lookup is a full table scan.

CREATE INDEX idx_booking_created_by ON booking (created_by);

CREATE INDEX idx_passenger_created_by ON passenger (created_by);
