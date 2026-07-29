-- Baseline: the schema as it stood when Flyway was introduced, i.e. everything
-- Hibernate's ddl-auto=update had accumulated up to that point.
--
-- Databases that already existed are stamped at this version by
-- spring.flyway.baseline-on-migrate and never execute this script.
-- Fresh databases are built from it.

CREATE TABLE IF NOT EXISTS users (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) DEFAULT NULL,
    password VARCHAR(255) DEFAULT NULL,
    role     VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS flight (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    flight_number   VARCHAR(255) DEFAULT NULL,
    airline         VARCHAR(255) DEFAULT NULL,
    source          VARCHAR(255) DEFAULT NULL,
    destination     VARCHAR(255) DEFAULT NULL,
    total_seats     INT          NOT NULL,
    available_seats INT          NOT NULL,
    price           DOUBLE       NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS passenger (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) DEFAULT NULL,
    email      VARCHAR(255) DEFAULT NULL,
    age        INT          NOT NULL,
    contact    VARCHAR(255) DEFAULT NULL,
    created_by VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS booking (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    flight_id    BIGINT       DEFAULT NULL,
    passenger_id BIGINT       DEFAULT NULL,
    booking_date DATE         DEFAULT NULL,
    created_by   VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_booking_flight FOREIGN KEY (flight_id) REFERENCES flight (id),
    CONSTRAINT fk_booking_passenger FOREIGN KEY (passenger_id) REFERENCES passenger (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
