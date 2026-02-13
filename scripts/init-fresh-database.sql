-- =====================================================
-- BlurApp Fresh Database Initialization Script
-- Run this in Neon SQL Editor for a fresh start
-- =====================================================

-- =====================================================
-- STEP 1: Drop all existing tables (clean slate)
-- =====================================================
-- Drop in reverse dependency order

-- Junction/link tables first
DROP TABLE IF EXISTS race_race_parameters CASCADE;
DROP TABLE IF EXISTS race_participants CASCADE;
DROP TABLE IF EXISTS user_race_participation CASCADE;
DROP TABLE IF EXISTS party_members CASCADE;
DROP TABLE IF EXISTS party_managers CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;

-- Entity tables
DROP TABLE IF EXISTS attribution CASCADE;
DROP TABLE IF EXISTS score CASCADE;
DROP TABLE IF EXISTS race CASCADE;
DROP TABLE IF EXISTS party CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS car CASCADE;
DROP TABLE IF EXISTS card CASCADE;
DROP TABLE IF EXISTS race_parameters CASCADE;

-- =====================================================
-- STEP 2: Create Tables
-- =====================================================

-- Roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Role permissions table
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_id, permission)
);

-- Users table
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT REFERENCES roles(id),
    enabled BOOLEAN DEFAULT true,
    account_non_expired BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    credentials_non_expired BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    last_login TIMESTAMP
);

-- User roles (many-to-many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Cars table
CREATE TABLE car (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    image_url TEXT
);

-- Cards (maps/tracks) table
CREATE TABLE card (
    id BIGSERIAL PRIMARY KEY,
    location VARCHAR(255),
    track VARCHAR(255),
    image_url TEXT
);

-- Race parameters (bonuses/power-ups) table
CREATE TABLE race_parameters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    is_active BOOLEAN DEFAULT false,
    download_url TEXT
);

-- Party table
CREATE TABLE party (
    id BIGSERIAL PRIMARY KEY,
    party_date DATE UNIQUE,
    created_at TIMESTAMP,
    creator_id BIGINT REFERENCES app_user(id),
    active BOOLEAN DEFAULT true
);

-- Party members (many-to-many)
CREATE TABLE party_members (
    party_id BIGINT NOT NULL REFERENCES party(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    PRIMARY KEY (party_id, user_id)
);

-- Party managers (many-to-many)
CREATE TABLE party_managers (
    party_id BIGINT NOT NULL REFERENCES party(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    PRIMARY KEY (party_id, user_id)
);

-- Race table
CREATE TABLE race (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    attribution_type VARCHAR(20) DEFAULT 'PER_USER',
    party_id BIGINT REFERENCES party(id) ON DELETE CASCADE,
    creator_id BIGINT REFERENCES app_user(id),
    score_collector_id BIGINT REFERENCES app_user(id),
    card_id BIGINT NOT NULL REFERENCES card(id),
    favorite_card VARCHAR(255),
    confidence_points INTEGER
);

-- Race participants (many-to-many)
CREATE TABLE race_participants (
    race_id BIGINT NOT NULL REFERENCES race(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    PRIMARY KEY (race_id, user_id)
);

-- Race race parameters (many-to-many)
CREATE TABLE race_race_parameters (
    race_id BIGINT NOT NULL REFERENCES race(id) ON DELETE CASCADE,
    parameter_id BIGINT NOT NULL REFERENCES race_parameters(id) ON DELETE CASCADE,
    PRIMARY KEY (race_id, parameter_id)
);

-- Score table
CREATE TABLE score (
    id BIGSERIAL PRIMARY KEY,
    value INTEGER NOT NULL,
    rank INTEGER,
    race_id BIGINT NOT NULL REFERENCES race(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    submitted_by_id BIGINT NOT NULL REFERENCES app_user(id),
    submitted_at TIMESTAMP NOT NULL,
    notes TEXT
);

-- Attribution table
CREATE TABLE attribution (
    id BIGSERIAL PRIMARY KEY,
    race_id BIGINT NOT NULL REFERENCES race(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES app_user(id),
    car_id BIGINT REFERENCES car(id),
    notes TEXT
);

-- =====================================================
-- STEP 3: Initialize Roles
-- =====================================================

-- Insert GREAT_ADMIN role
INSERT INTO roles (name, description) VALUES 
('GREAT_ADMIN', 'Great Administrator with all permissions');

-- Insert GREAT_ADMIN permissions
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, perm
FROM roles r
CROSS JOIN (VALUES 
    ('ALL_PERMISSIONS'),
    ('CREATE_USER'),
    ('UPDATE_USER'),
    ('DELETE_USER'),
    ('VIEW_ALL_USERS'),
    ('ASSIGN_ROLES'),
    ('CREATE_PARTY'),
    ('JOIN_PARTY'),
    ('MANAGE_PARTY'),
    ('DELETE_PARTY'),
    ('VIEW_PARTY'),
    ('CREATE_RACE'),
    ('START_RACE'),
    ('JOIN_RACE'),
    ('LEAVE_RACE'),
    ('DELETE_RACE'),
    ('VIEW_RACE'),
    ('SUBMIT_SCORE'),
    ('VIEW_SCORE'),
    ('EDIT_SCORE'),
    ('VIEW_CARS'),
    ('VIEW_MAPS'),
    ('VIEW_STATISTICS'),
    ('VIEW_HISTORY'),
    ('UPDATE_OWN_PROFILE'),
    ('VIEW_OWN_PROFILE')
) AS perms(perm)
WHERE r.name = 'GREAT_ADMIN';

-- Insert RACER role
INSERT INTO roles (name, description) VALUES 
('RACER', 'Racer can create/manage parties and participate in races');

-- Insert RACER permissions
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, perm
FROM roles r
CROSS JOIN (VALUES 
    ('CREATE_PARTY'),
    ('JOIN_PARTY'),
    ('MANAGE_PARTY'),
    ('VIEW_PARTY'),
    ('CREATE_RACE'),
    ('START_RACE'),
    ('JOIN_RACE'),
    ('LEAVE_RACE'),
    ('VIEW_RACE'),
    ('DELETE_RACE'),
    ('SUBMIT_SCORE'),
    ('VIEW_SCORE'),
    ('VIEW_CARS'),
    ('VIEW_MAPS'),
    ('VIEW_STATISTICS'),
    ('VIEW_HISTORY'),
    ('UPDATE_OWN_PROFILE'),
    ('VIEW_OWN_PROFILE')
) AS perms(perm)
WHERE r.name = 'RACER';

-- =====================================================
-- STEP 4: Create Default Admin User
-- Password: admin123 (bcrypt hash)
-- =====================================================

INSERT INTO app_user (user_name, email, password, role_id, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
SELECT 
    'admin',
    'admin@blurapp.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqWpIqF.YFTUEDuH.SrPJkLfTq9Aq',
    r.id,
    true,
    true,
    true,
    true,
    NOW(),
    NOW()
FROM roles r
WHERE r.name = 'GREAT_ADMIN';

-- =====================================================
-- STEP 5: Initialize Cars
-- =====================================================

INSERT INTO car (name, image_url) VALUES
('Opel Astra Extreme', 'https://static.wikia.nocookie.net/blurgame/images/a/ab/Opel_Astra_Extreme.jpg/revision/latest?cb=20100912135528'),
('Ford FR-100', 'https://static.wikia.nocookie.net/blurgame/images/d/d0/Cars_ford_fr_100_download.jpg/revision/latest/scale-to-width-down/1000?cb=20100316191358'),
('Audi R8 4.2 FSI quattro', 'https://static.wikia.nocookie.net/blurgame/images/0/04/Audi_R8_4.2_FSI_Quattro.jpg/revision/latest?cb=20100912142022'),
('Corvette C3 (Drag)', 'https://static.wikia.nocookie.net/blurgame/images/f/f0/Corvette_C3_%28Drag%29.jpg/revision/latest/scale-to-width-down/1000?cb=20120721051708'),
('Chevrolet Camaro SS (Drag)', 'https://static.wikia.nocookie.net/blurgame/images/1/13/Chevrolet_Camaro_SS_%28Drag%29.jpg/revision/latest/scale-to-width-down/1000?cb=20120721021315'),
('Ford Mustang GT-R Concept', 'https://static.wikia.nocookie.net/blurgame/images/5/54/Ford_Mustang_GT-R_Concept.jpg/revision/latest/scale-to-width-down/1000?cb=20120722081429'),
('Vauxhall Monaro VXR (Drift)', 'https://static.wikia.nocookie.net/blurgame/images/0/0a/Vauxhall_Monaro_VXR_%28Drift%29.jpg/revision/latest/scale-to-width-down/1000?cb=20120723104448'),
('Dodge Viper GTSR', 'https://static.wikia.nocookie.net/blurgame/images/a/ad/Dodge_Viper_GTSR.jpg/revision/latest/scale-to-width-down/1000?cb=20120722041003'),
('Renault Mégane Trophy', 'https://static.wikia.nocookie.net/blurgame/images/9/9a/Ingame_renault_megane.JPG/revision/latest?cb=20100316184747'),
('Nissan Skyline GT-R NISMO Z-tune', 'https://static.wikia.nocookie.net/blurgame/images/b/bb/Nissan_Skyline_GT-R_NISMO_Z-tune.jpg/revision/latest/scale-to-width-down/1000?cb=20120722145426'),
('Ford Transit Supervan3', 'https://static.wikia.nocookie.net/blurgame/images/d/dd/60072_10150270979520302_880460301_14709902_514768_n.jpg/revision/latest?cb=20100912141412'),
('Corvette C3 (Race)', 'https://static.wikia.nocookie.net/blurgame/images/c/c0/Corvette_C3_%28Race%29.jpg/revision/latest?cb=20120719160930'),
('Dodge Challenger SRT8 (Race)', 'https://static.wikia.nocookie.net/blurgame/images/6/6a/Dodge_Challenger_%28Race%29.jpg/revision/latest/scale-to-width-down/1000?cb=20120722040406'),
('Dodge Viper Venom 1000', 'https://static.wikia.nocookie.net/blurgame/images/0/0b/Dodge_Viper_Venom_1000.jpg/revision/latest/scale-to-width-down/1000?cb=20120722041202'),
('Koenigsegg CCX-R', 'https://static.wikia.nocookie.net/blurgame/images/0/03/46831_10150270978980302_880460301_14709888_3561956_n.jpg/revision/latest?cb=20100912141201');

-- =====================================================
-- STEP 6: Initialize Cards (Maps/Tracks)
-- =====================================================

INSERT INTO card (location, track, image_url) VALUES
('Amboy', 'Badlands Traverse', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FAmboy-badlandsTraverse.png?alt=media&token=dc4d07cc-5e43-4e16-b1d0-dc32bed8aeaf'),
('Amboy', 'Route 66', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FAmboy-Route66.png?alt=media&token=03c6e87a-9f21-4abf-8ba9-971c7fd565c5'),
('Barcelona Garcia', 'Catalan Climb', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBarcelonaGracia-CatalanClimb.png?alt=media&token=4d7c1023-b209-40da-9e49-afce5565d1dc'),
('Barcelona Gracia', 'El Carmel Heights', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBarcelonaGracia-ElCarmelHeights.png?alt=media&token=9973ad8f-d038-4b9f-b9f5-098d77dc225f'),
('Barcelona Garcia', 'Passeig de Gracia', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBarcelonaGracia-PasseigdeGracia.png?alt=media&token=32d94552-e3d2-4554-948d-37f46f91a74f'),
('Barcelona Oval', 'City Breach', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBarcelonaOval-CityBreach.png?alt=media&token=5152ab84-128f-44d6-b2c6-fede7ec35285'),
('Barcelona Oval', 'Speedway', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBarcelonaOval-Speedway.png?alt=media&token=c09246cd-f733-48de-b6d3-831f4c478d9c'),
('Brighton', 'Coastal Cruise', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBrighton-CoastalCruise.png?alt=media&token=29895549-9efe-4a72-a623-0ae175cb5ef7'),
('Brighton', 'Promenade Loop', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBrighton-PromenadeLoop.png?alt=media&token=407b683d-5f47-4363-8971-8410e7dca36e'),
('Brighton', 'Seafront Strip', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBrighton-SeafrontStrip.png?alt=media&token=753c2258-847a-4cb8-8349-8da4229b45c8'),
('Hackney', 'Central Sprint', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FHackney-CentralSprint.png?alt=media&token=d5c01796-7769-40c0-ba58-d8badd5466cf'),
('Hackney', 'Shoreditch Sweep', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FHackney-ShoreditchSweep.png?alt=media&token=d90ad9c8-5455-4b68-a58f-6556f1ecbe95'),
('Hackney', 'Urban Belt', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FHackney-UrbanBelt.png?alt=media&token=9125a8ca-9aba-46ed-932c-6ac025718006'),
('Hollywood Hills', 'Downtown Vista', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FHollywoodHills-DowntownVista.png?alt=media&token=9339f6bc-435d-4542-87e7-890450098515'),
('Hollywood Hills', 'Hollywood Rift', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FHollywoodHills-HollywoodRift.png?alt=media&token=2f9137cd-6b94-4efb-b5a6-9d082753b2f3'),
('LA Docks', 'Cargo Run', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLADocks-CargoRun.png?alt=media&token=100e35e6-fec5-4cef-9e80-d2e15cd2febf'),
('LA Docks', 'Pacific Reach', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLADocks-PacificReach.png?alt=media&token=312b19df-ebe4-458c-84aa-432a736eed64'),
('LA Downtown', 'Harbor Freeway', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLADowntown-HarborFreeway.png?alt=media&token=3a9d7206-1c09-428e-aea9-f87268683227'),
('LA Downtown', 'Highrise Ring', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLADowntown-HighriseRing.png?alt=media&token=f34e0090-bd7a-4d4e-9b1c-8e60d6da8ea4'),
('LA Downtown', 'The Money Run', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLADowntown-TheMoneyRun.png?alt=media&token=b55229a2-d099-4165-b498-23ca01207f29'),
('LA River', 'Concrete Basin', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLARiver-ConcreteBasin.png?alt=media&token=df96a971-0998-46ad-a7a1-e7eba983655a'),
('LA River', 'Stormdrain Surge', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLARiver-StormdrainSurge.png?alt=media&token=f97171cb-3291-40a2-b19c-b713b5c8ebb3'),
('Mount Haruna', 'Descent', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FMountHaruna-Descent.png?alt=media&token=15c22c0d-6e57-4c8f-b218-7bba22ee600c'),
('NY Dumbo', 'Brooklyn Tour', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FNYDumbo-BrooklynTour.png?alt=media&token=555c86ab-36ca-492d-ab45-9d78134aabdf'),
('NY Dumbo', 'Manhattan View', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FNYDumbo-ManhattanView.png?alt=media&token=ee9cc357-2e1a-4864-a582-896897f9201f'),
('SanFran Russian Hill', 'Russian Steppes', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FSanFranRussianHill-RussianSteppes.png?alt=media&token=9b3e2eac-f9b2-43be-98b2-86ba39743959'),
('SanFran Sausalito', 'Bay Area Tour', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FSanFranSausalito-BayAreaTour.png?alt=media&token=b0fcc3e3-c248-4ad4-b223-838aba033603'),
('SanFran Sausalito', 'Golden Gate Rush', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FSanFranSausalito-GoldenGateRush.png?alt=media&token=b52946f0-5ecf-48a8-9da9-f64d3322b496'),
('Tokyo Shutoko', 'Bayshore Route', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FTokyoShutoko-BayshoreRoute.png?alt=media&token=cc473ca9-e8eb-44a3-b997-83788d510052'),
('Tokyo Shutoko', 'Wangan-sen', 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FTokyoShutoko-Wangan-sen.png?alt=media&token=14b27b90-4c35-486b-a94b-40c84b58407b');

-- =====================================================
-- STEP 7: Initialize Race Parameters (Bonuses/Power-ups)
-- =====================================================

INSERT INTO race_parameters (name, is_active, download_url) VALUES
('Barge', false, 'https://static.wikia.nocookie.net/blurgame/images/5/5b/Barge.png/revision/latest/scale-to-width-down/150?cb=20100510210429'),
('Bolt', true, 'https://static.wikia.nocookie.net/blurgame/images/8/8e/Bolt.png/revision/latest/scale-to-width-down/150?cb=20100510210430'),
('Handicap', false, 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2Fhandicap.png?alt=media&token=dafc433f-8cf3-4024-83d7-0b3b0e1a46b0'),
('Mine', true, 'https://static.wikia.nocookie.net/blurgame/images/a/a0/Mine.png/revision/latest/scale-to-width-down/150?cb=20100510210431'),
('Mods', false, 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2Fmods.png?alt=media&token=32fc2e5e-feb3-46da-bc51-62fa007c3d70'),
('Nitro', true, 'https://static.wikia.nocookie.net/blurgame/images/e/ea/Nitro.png/revision/latest/scale-to-width-down/150?cb=20100510210432'),
('Repair', true, 'https://static.wikia.nocookie.net/blurgame/images/e/e2/Repair.png/revision/latest/scale-to-width-down/150?cb=20100510210433'),
('Respawn', false, 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2Frespawn.png?alt=media&token=d8b36eef-ebe3-4f20-b176-719907a443be'),
('Shield', false, 'https://static.wikia.nocookie.net/blurgame/images/c/cf/Shield.png/revision/latest/scale-to-width-down/150?cb=20100510210434'),
('Shock', false, 'https://static.wikia.nocookie.net/blurgame/images/f/f9/Shock.png/revision/latest/scale-to-width-down/150?cb=20100510210434'),
('Shunt', true, 'https://static.wikia.nocookie.net/blurgame/images/6/6c/Shunt.png/revision/latest/scale-to-width-down/150?cb=20100510210434'),
('Time Out', false, 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2Ftime_out.png?alt=media&token=cf7b6465-54e5-488a-9b83-fa1b88cb2bc1'),
('Upgrades', true, 'https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2Fupgrades.png?alt=media&token=11acb7c9-1c7f-4b13-b975-2fc3c81c434c');

-- =====================================================
-- STEP 8: Verify initialization
-- =====================================================

SELECT 'Roles' as entity, COUNT(*) as count FROM roles
UNION ALL
SELECT 'Users' as entity, COUNT(*) as count FROM app_user
UNION ALL
SELECT 'Cars' as entity, COUNT(*) as count FROM car
UNION ALL
SELECT 'Cards (Maps)' as entity, COUNT(*) as count FROM card
UNION ALL
SELECT 'Race Parameters' as entity, COUNT(*) as count FROM race_parameters;

-- =====================================================
-- INITIALIZATION COMPLETE!
-- Default admin credentials:
--   Username: admin
--   Password: admin123
-- =====================================================
