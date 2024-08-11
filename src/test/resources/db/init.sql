-- App schema
CREATE TABLE IF NOT EXISTS bundle
(
    id              INT AUTO_INCREMENT,
    name            VARCHAR(255) UNIQUE,
    description     VARCHAR(255) NOT NULL,
    coins           INT          NOT NULL,
    expiration_date TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS currency
(
    id        INT AUTO_INCREMENT,
    name      VARCHAR(255) UNIQUE,
    ron_value DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE category
(
    id               INT AUTO_INCREMENT,
    name             VARCHAR(255) UNIQUE,
    label            VARCHAR(255),
    icon             VARCHAR(255),
    parent_name      VARCHAR(255),
    category_details VARCHAR(255),
    PRIMARY KEY (id)
);

-- Public schema
CREATE TABLE IF NOT EXISTS users
(
    id        INT AUTO_INCREMENT NOT NULL,
    username  VARCHAR(255)       NOT NULL,
    password  VARCHAR(255)       NOT NULL,
    email     VARCHAR(255)       NOT NULL,
    role      VARCHAR(255)       NOT NULL,
    is_active TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uc_user_name UNIQUE (username),
    CONSTRAINT uc_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS user_details
(
    id                INT AUTO_INCREMENT,
    first_name        VARCHAR(255) NOT NULL,
    last_name         VARCHAR(255) NOT NULL,
    username          VARCHAR(255) NOT NULL,
    avatar_url        VARCHAR(255),
    gender            VARCHAR(255),
    cui               VARCHAR(255),
    type              VARCHAR(255),
    last_seen         TIMESTAMP,
    available_coins   INT,
    user_id           INT,
    is_google_account TINYINT(1),
    PRIMARY KEY (id),
    CONSTRAINT uc_username UNIQUE (username),
    CONSTRAINT uc_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_resource FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS offer
(
    id                      INT AUTO_INCREMENT,
    name                    VARCHAR(255)   NOT NULL,
    description             VARCHAR(255)   NOT NULL,
    deep_link               VARCHAR(255)   DEFAULT '',
    category_id             BIGINT         NOT NULL,
    publish_date            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    price                   DECIMAL(10, 2) NOT NULL,
    is_on_auction           TINYINT(1),
    is_available            TINYINT(1),
    promote                 TINYINT(1),
    auto_extend             TINYINT(1),
    `condition`             VARCHAR(255), -- Escaping the column name
    currency_type           VARCHAR(255),
    auction_start_date      TIMESTAMP,
    promote_expiration_date TIMESTAMP,
    auction_end_date        TIMESTAMP,
    owner_id                INT,
    coins_to_extend         INT,
    expiration_date         TIMESTAMP      NOT NULL,
    views_number            INT                     DEFAULT 0,
    auction_winner          INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_owner FOREIGN KEY (owner_id)
        REFERENCES user_details (id)
);

CREATE TABLE IF NOT EXISTS contact
(
    id       INT AUTO_INCREMENT,
    type     VARCHAR(255) NOT NULL,
    value    VARCHAR(255) NOT NULL,
    user_id  INT,
    offer_id INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_contact FOREIGN KEY (user_id)
        REFERENCES user_details (id),
    CONSTRAINT fk_offer_contact FOREIGN KEY (offer_id)
        REFERENCES offer (id)
);

CREATE TABLE IF NOT EXISTS address
(
    id       INT AUTO_INCREMENT,
    city     VARCHAR(255) NOT NULL,
    country  VARCHAR(255) NOT NULL,
    street   VARCHAR(255) NOT NULL,
    user_id  INT,
    offer_id INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_address FOREIGN KEY (user_id)
        REFERENCES user_details (id),
    CONSTRAINT fk_offer_address FOREIGN KEY (offer_id)
        REFERENCES offer (id)
);

CREATE TABLE IF NOT EXISTS favorite
(
    id       INT AUTO_INCREMENT,
    user_id  INT,
    offer_id INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id)
        REFERENCES user_details (id),
    CONSTRAINT fk_favorite_offer FOREIGN KEY (offer_id)
        REFERENCES offer (id)
);

CREATE TABLE IF NOT EXISTS bid
(
    id            INT AUTO_INCREMENT,
    date          TIMESTAMP      NOT NULL,
    price         DECIMAL(10, 2) NOT NULL,
    user_id       INT,
    offer_id      INT,
    is_winner     TINYINT(1) DEFAULT 0,
    charged_coins INT        DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_bid_user FOREIGN KEY (user_id)
        REFERENCES user_details (id),
    CONSTRAINT fk_bid_offer FOREIGN KEY (offer_id)
        REFERENCES offer (id)
);

CREATE TABLE IF NOT EXISTS files
(
    id         INT AUTO_INCREMENT,
    url        VARCHAR(255),
    is_primary TINYINT(1),
    offer_id   INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_file_product FOREIGN KEY (offer_id)
        REFERENCES offer (id)
);

CREATE TABLE IF NOT EXISTS offer_data
(
    id       INT AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    value    VARCHAR(255) NOT NULL,
    field_id VARCHAR(255) NOT NULL,
    offer_id INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_data FOREIGN KEY (offer_id)
        REFERENCES offer (id)
);

CREATE TABLE IF NOT EXISTS payments_history
(
    id             INT AUTO_INCREMENT,
    payment_secret VARCHAR(255) NOT NULL,
    user_id        VARCHAR(255) NOT NULL,
    status         VARCHAR(255),
    intent_date    TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_devices
(
    id         INT AUTO_INCREMENT,
    device_key VARCHAR(255) NOT NULL,
    user_id    INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_device FOREIGN KEY (user_id)
        REFERENCES user_details (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS confirmation_token
(
    token_id     INT AUTO_INCREMENT NOT NULL,
    token        VARCHAR(255)       NOT NULL,
    created_date TIMESTAMP          NOT NULL,
    user_id      INT                NOT NULL,
    PRIMARY KEY (token_id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

INSERT INTO bundle (name, description, coins)
VALUES ('BASIC', 'BASIC bundle', 1000);

CREATE UNIQUE INDEX favorite_user_offer on favorite (user_id, offer_id);

CREATE TABLE IF NOT EXISTS room
(
    id         INT AUTO_INCREMENT,
    room_name  VARCHAR(255) NOT NULL,
    activity   TIMESTAMP    NOT NULL,
    host_id    INT          NOT NULL,
    guest_id   INT          NOT NULL,
    offer_id   INT          NOT NULL,
    chat_image VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_room_user FOREIGN KEY (host_id)
        REFERENCES user_details (id)
);

CREATE TABLE IF NOT EXISTS message
(
    id           INT AUTO_INCREMENT,
    room_id      INT       NOT NULL,
    message      TEXT      NOT NULL,
    sender_id    INT       NOT NULL,
    message_time TIMESTAMP NOT NULL,
    sender_email VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_room_message FOREIGN KEY (room_id)
        REFERENCES room (id),
    CONSTRAINT fk_user_message FOREIGN KEY (sender_id)
        REFERENCES user_details (id)
);

CREATE UNIQUE INDEX chat_members_room_name_offer on room (room_name, host_id, guest_id, offer_id);

CREATE TABLE IF NOT EXISTS notification
(
    id       INT AUTO_INCREMENT,
    date     DATE         NOT NULL,
    type     VARCHAR(255) NOT NULL,
    title    VARCHAR(255) NOT NULL,
    message  VARCHAR(255) NOT NULL,
    offer_id INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_favorite_product FOREIGN KEY (offer_id)
        REFERENCES offer (id)
);

ALTER TABLE notification
    ADD COLUMN user_id INTEGER;

ALTER TABLE notification
    ADD CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id)
            REFERENCES user_details (id);

INSERT INTO bundle (name, description, coins)
VALUES ('GOLD', 'GOLD bundle', 2500);

INSERT INTO bundle (name, description, coins)
VALUES ('DIAMOND', 'DIAMOND bundle', 5000);

UPDATE bundle
SET name = 'SILVER'
WHERE name = 'BASIC';

ALTER TABLE bundle
    ADD COLUMN price DECIMAL;

UPDATE bundle
SET price = 50
WHERE name = 'SILVER';

UPDATE bundle
SET price = 100
WHERE name = 'GOLD';

UPDATE bundle
SET price = 150
WHERE name = 'DIAMOND';

ALTER TABLE category
    ADD COLUMN add_offer_cost INT DEFAULT 0;
ALTER TABLE category
    ADD COLUMN place_bid_cost INT DEFAULT 0;

ALTER TABLE notification
    MODIFY COLUMN offer_id INT;

ALTER TABLE notification
    DROP CONSTRAINT fk_favorite_product;

ALTER TABLE notification
    DROP CONSTRAINT fk_notification_user;

-- Drop the existing foreign key constraint
ALTER TABLE user_details
    DROP CONSTRAINT fk_user_resource;

-- Recreate the foreign key constraint with ON DELETE CASCADE
ALTER TABLE user_details
    ADD CONSTRAINT fk_user_details
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

-- Drop table
DROP TABLE IF EXISTS confirmation_token;

-- Recreate table
CREATE TABLE IF NOT EXISTS confirmation_token
(
    token_id     INT AUTO_INCREMENT PRIMARY KEY,
    token        VARCHAR(255) NOT NULL,
    created_date TIMESTAMP    NOT NULL,
    user_id      INT          NOT NULL,
    CONSTRAINT fk_token_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Recreate the foreign key constraint with ON DELETE CASCADE
ALTER TABLE bid
    ADD CONSTRAINT fk_offer_user
        FOREIGN KEY (user_id) REFERENCES user_details (id) ON DELETE CASCADE;

-- Drop the existing foreign key constraint
ALTER TABLE room
    DROP CONSTRAINT fk_room_user;

-- Recreate the foreign key constraint with ON DELETE CASCADE
ALTER TABLE room
    ADD CONSTRAINT fk_room_user
        FOREIGN KEY (host_id) REFERENCES user_details (id) ON DELETE CASCADE;

-- Drop the existing foreign key constraint
ALTER TABLE message
    DROP CONSTRAINT fk_room_message;

-- Recreate the foreign key constraint with ON DELETE CASCADE
ALTER TABLE message
    ADD CONSTRAINT fk_room_message
        FOREIGN KEY (room_id) REFERENCES room (id) ON DELETE CASCADE;

-- Drop the existing foreign key constraint
ALTER TABLE message
    DROP CONSTRAINT fk_user_message;

-- Recreate the foreign key constraint with ON DELETE CASCADE
ALTER TABLE message
    ADD CONSTRAINT fk_user_message
        FOREIGN KEY (sender_id) REFERENCES user_details (id) ON DELETE CASCADE;

-- Drop the existing foreign key constraint
ALTER TABLE offer_data
    DROP CONSTRAINT fk_product_data;

-- Recreate the foreign key constraint with ON DELETE CASCADE
ALTER TABLE offer_data
    ADD CONSTRAINT fk_product_data
        FOREIGN KEY (offer_id) REFERENCES offer (id) ON DELETE CASCADE;

ALTER TABLE category
    MODIFY COLUMN category_details JSON;

ALTER TABLE category
    MODIFY COLUMN label JSON;

ALTER TABLE notification
    ADD COLUMN room_id INTEGER;

DROP TABLE IF EXISTS payments_history;

CREATE TABLE IF NOT EXISTS transaction
(
    id               INT AUTO_INCREMENT,
    amount           DECIMAL(10, 2) NOT NULL,
    user_id          INT            NOT NULL,
    products         VARCHAR(255)   NOT NULL,
    customer_name    VARCHAR(255)   NOT NULL,
    billing_address  VARCHAR(255)   NOT NULL,
    invoice_number   VARCHAR(255)   NOT NULL,
    transaction_date TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id)
        REFERENCES user_details (id)
);

-- Drop the existing foreign key constraint
ALTER TABLE bid
    DROP CONSTRAINT fk_bid_user;

-- Recreate the foreign key constraint with ON DELETE CASCADE
ALTER TABLE bid
    ADD CONSTRAINT fk_bid_user
        FOREIGN KEY (user_id) REFERENCES user_details (id) ON DELETE CASCADE;

ALTER TABLE user_details
    ADD COLUMN subscribed_to_newsletter BOOLEAN DEFAULT TRUE NOT NULL;

ALTER TABLE user_details
    ADD COLUMN registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;

ALTER TABLE user_details
    ADD COLUMN ratings VARCHAR(255) DEFAULT '[{"reviewer":"sys_default","rate":5}]' NOT NULL;

CREATE TABLE IF NOT EXISTS system_configuration
(
    id              INT AUTO_INCREMENT,
    name            VARCHAR(255) UNIQUE,
    code            VARCHAR(255),
    description     VARCHAR(255) NOT NULL,
    expiration_date TIMESTAMP,
    value           VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- INSERT DB DATA
INSERT INTO users (username, password, email, role, is_active)
VALUES ('joe.doe@gmail.com', 'password1', 'joe.doe@gmail.com', 'USER', 1),
       ('mary.jones@gmail.com', 'password2', 'mary.jones@gmail.com', 'USER', 1),
       ('sam.hardy@gmail.com', 'password2', 'sam.hardy@gmail.com', 'USER', 1);

INSERT INTO user_details (first_name, last_name, username, avatar_url, gender, cui, type, last_seen, available_coins,
                          user_id, is_google_account)
VALUES ('John', 'Doe', 'joe.doe@gmail.com', 'http://example.com/avatar1.jpg', 'Male', NULL, NULL, NOW(), 100,
        1, 0),
       ('Mary', 'Jones', 'mary.jones@gmail.com', 'http://example.com/avatar2.jpg', 'Male', NULL, NULL, NOW(),
        150, 2, 0),
       ('Sam', 'Hardy', 'sam.hardy@gmail.com', 'http://example.com/avatar2.jpg', 'Male', NULL, NULL, NOW(),
        150, 3, 0);

-- Prepare 7 offers - 5 fixed priced , 2 on auction
-- Fixed priced offers available for the next 30 days
INSERT INTO offer (name, description, category_id, publish_date, price, is_on_auction, `condition`, currency_type,
                   owner_id, expiration_date, is_available, auto_extend, promote)
VALUES ('Vintage Watch', 'A rare vintage watch from the 1950s.', 1, CURRENT_TIMESTAMP(), 250.00, 0, 'USED', 'EURO', 1,
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 30 DAY), 1, 0, 0),
       ('Antique Vase', 'An exquisite antique vase in excellent condition.', 2, CURRENT_TIMESTAMP(), 500.00, 0, 'USED',
        'EURO', 2, DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 30 DAY), 1, 0, 0),
       ('Art Print', 'Limited edition print by a renowned artist.', 3, CURRENT_TIMESTAMP(), 150.00, 0, 'USED', 'EURO',
        2, DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 30 DAY), 1, 0, 0);

-- Fixed priced offers that are expired because the were published 32 days ago
INSERT INTO offer (name, description, category_id, publish_date, price, is_on_auction, `condition`, currency_type,
                   owner_id, expiration_date, is_available, auto_extend, promote)
VALUES ('Vintage Camera', 'A classic camera from the early 20th century, in working condition.', 4,
        DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 32 DAY), 300.00, 0, 'USED', 'EURO', 1,
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 1 DAY), 1, 0, 0),
       ('Vintage Nintendo Auction', 'A classic nintendo from the early 20th century, in working condition.', 4,
        DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 32 DAY), 300.00, 0, 'USED', 'EURO', 1,
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 1 DAY), 1, 0, 0);

--  Auction offers that should end now
INSERT INTO offer (name, description, category_id, publish_date, price, is_on_auction, `condition`, currency_type,
                   owner_id, expiration_date, auction_start_date, auction_end_date, promote, promote_expiration_date,
                   is_available, auto_extend)
VALUES ('Collector’s Edition Book', 'Rare first edition of a famous literary work, in mint condition.', 5,
        DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 10 DAY), 1200.00, 1, 'USED', 'EURO', 1,
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 1 DAY),
        DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 10 DAY),
        DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 DAY), 1, NULL, 1, 0),
       ('Antique Desk Clock', 'An elegant desk clock from the Victorian era, fully functional.', 6,
        DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 10 DAY), 1400.00, 1, 'USED', 'EURO', 1,
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 1 DAY),
        DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 10 DAY),
        DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 DAY), 0, NULL, 1, 0);

--  Auction offers that are still available
INSERT INTO offer (name, description, category_id, publish_date, price, is_on_auction, `condition`, currency_type,
                   owner_id, expiration_date, auction_start_date, auction_end_date, promote, promote_expiration_date,
                   is_available, auto_extend)
VALUES ('Vintage Typewriter', 'A classic typewriter from the 1950s, in working condition.', 6,
        DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 10 DAY), 1000.00, 1, 'USED', 'EURO', 2,
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 1 DAY),
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 DAY),
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 1 DAY), 0, NULL, 1, 0),
       ('PS3', 'A PS3 in working condition.', 8,
        DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 10 DAY), 1100.00, 1, 'USED', 'EURO', 1,
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 1 DAY),
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 10 DAY),
        DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 1 DAY), 0, NULL, 1, 0);

-- Insert Bids for Offers on auction
INSERT INTO bid (date, price, user_id, offer_id, is_winner, charged_coins)
VALUES (DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 3 DAY), 1250.00, 2, 6, 0, 50),
       (DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 3 DAY), 1450.00, 2, 7, 0, 50),
       (DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 3 DAY), 1400.00, 3, 7, 0, 50);

-- Insert System Configured currencies
INSERT INTO system_configuration (name, code, description, value)
VALUES ('RON', 'APP_CURRENCY_RON', 'Ron currency', '1');

INSERT INTO system_configuration (name, code, description, value)
VALUES ('EURO', 'APP_CURRENCY_EUR', 'Euro currency', '5');

INSERT INTO system_configuration (name, code, description, value)
VALUES ('USD', 'APP_CURRENCY_USD', 'US Dollar currency', '4.6');