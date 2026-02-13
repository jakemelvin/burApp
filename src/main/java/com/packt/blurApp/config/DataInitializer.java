package com.packt.blurApp.config;

import com.packt.blurApp.model.Car;
import com.packt.blurApp.model.Card;
import com.packt.blurApp.model.RaceParameters;
import com.packt.blurApp.model.Role;
import com.packt.blurApp.model.User;
import com.packt.blurApp.config.security.RoleNames;
import com.packt.blurApp.repository.CarRepository;
import com.packt.blurApp.repository.CardRepository;
import com.packt.blurApp.repository.RaceParametersRepository;
import com.packt.blurApp.repository.RoleRepository;
import com.packt.blurApp.repository.UserRepository;
import com.packt.blurApp.repository.PartyRepository;
import com.packt.blurApp.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PartyRepository partyRepository;
    private final RaceRepository raceRepository;
    private final CarRepository carRepository;
    private final CardRepository cardRepository;
    private final RaceParametersRepository raceParametersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing application data...");
        
        // Repair schema constraints that Hibernate `ddl-auto=update` cannot remove.
        repairSchemaConstraints();

        // Initialize roles
        initializeRoles();
        
        // Create default GREAT_ADMIN user if not exists
        createDefaultAdmin();

        // Initialize game data (cars, cards/maps, race parameters/bonuses)
        initializeCars();
        initializeCards();
        initializeRaceParameters();

        // Backfill existing DB rows (important when connecting to an existing Neon database)
        backfillExistingUsers();
        backfillExistingParties();
        backfillExistingRaces();

        log.info("Application data initialized successfully");
    }

    private void repairSchemaConstraints() {
        // Historically the DB had a CHECK constraint that only allowed a fixed set of role names.
        // That prevents creating custom roles and causes 409 errors like:
        //   violates check constraint "roles_name_check"
        try {
            jdbcTemplate.execute("ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_name_check");
            log.info("Schema repair: dropped constraint roles_name_check (if it existed)");
        } catch (Exception ex) {
            // Don't block app startup if the constraint doesn't exist / permissions issue.
            log.warn("Schema repair: unable to drop roles_name_check constraint: {}", ex.getMessage());
        }
    }

    private void initializeRoles() {
        log.info("Initializing roles...");
        
        // Create or update GREAT_ADMIN role
        Role existingGreatAdmin = roleRepository.findByName(RoleNames.GREAT_ADMIN).orElse(null);
        if (existingGreatAdmin == null) {
            Role greatAdmin = Role.createGreatAdminRole();
            roleRepository.save(greatAdmin);
            log.info("Created role: GREAT_ADMIN");
        } else {
            // Update existing GREAT_ADMIN role with all permissions
            Role templateRole = Role.createGreatAdminRole();
            existingGreatAdmin.setPermissions(templateRole.getPermissions());
            existingGreatAdmin.setDescription(templateRole.getDescription());
            roleRepository.save(existingGreatAdmin);
            log.info("Updated role: GREAT_ADMIN with latest permissions");
        }
        
        // Create or update RACER role as the default gameplay role.
        Role existingRacer = roleRepository.findByName("RACER").orElse(null);
        if (existingRacer == null) {
            roleRepository.save(Role.createRacerRole());
            log.info("Created role: RACER");
        } else {
            // Update existing RACER role with all permissions
            Role templateRole = Role.createRacerRole();
            existingRacer.setPermissions(templateRole.getPermissions());
            existingRacer.setDescription(templateRole.getDescription());
            roleRepository.save(existingRacer);
            log.info("Updated role: RACER with latest permissions");
        }

        log.info("Roles initialized successfully");
    }

    private void backfillExistingUsers() {
        log.info("Backfilling existing users (role + flags + timestamps)...");

        Role racerRole = roleRepository.findByName("RACER").orElse(null);
        if (racerRole == null) {
            // If RACER doesn't exist (because we no longer seed it), do not auto-assign it.
            // We'll only ensure admin has a role below.
            log.warn("RACER role not found; skipping auto role assignment for non-admin users");
        }
        Role greatAdminRole = roleRepository.findByName(RoleNames.GREAT_ADMIN)
                .orElseThrow(() -> new RuntimeException("GREAT_ADMIN role not found"));

        userRepository.findAll().forEach(u -> {
            boolean changed = false;

            if (u.getRole() == null) {
                // if username is admin, make it GREAT_ADMIN, else RACER
                if ("admin".equalsIgnoreCase(u.getUsername())) {
                    u.setRole(greatAdminRole);
                } else if (racerRole != null) {
                    u.setRole(racerRole);
                }
                changed = true;
            }

            if (u.getEnabled() == null) {
                u.setEnabled(true);
                changed = true;
            }
            if (u.getAccountNonExpired() == null) {
                u.setAccountNonExpired(true);
                changed = true;
            }
            if (u.getAccountNonLocked() == null) {
                u.setAccountNonLocked(true);
                changed = true;
            }
            if (u.getCredentialsNonExpired() == null) {
                u.setCredentialsNonExpired(true);
                changed = true;
            }

            if (u.getCreatedAt() == null) {
                u.setCreatedAt(java.time.LocalDateTime.now());
                changed = true;
            }
            if (u.getUpdatedAt() == null) {
                u.setUpdatedAt(java.time.LocalDateTime.now());
                changed = true;
            }

            if (changed) userRepository.save(u);
        });

        log.info("Backfill users completed");
    }

    private void backfillExistingParties() {
        log.info("Backfilling existing parties (active + dates)...");

        var parties = partyRepository.findAll();
        if (parties.isEmpty()) {
            log.info("No parties found to backfill");
            return;
        }

        // Ensure createdAt and active are set first (in-memory), without saving yet.
        for (var p : parties) {
            if (p.getActive() == null) {
                p.setActive(true);
            }
            if (p.getCreatedAt() == null) {
                p.setCreatedAt(java.time.LocalDateTime.now());
            }
        }

        // Build a set of used dates and resolve duplicates deterministically.
        java.util.Map<java.time.LocalDate, java.util.List<com.packt.blurApp.model.Party>> byDate = new java.util.HashMap<>();
        java.util.List<com.packt.blurApp.model.Party> nullDateParties = new java.util.ArrayList<>();

        for (var p : parties) {
            if (p.getPartyDate() == null) {
                nullDateParties.add(p);
            } else {
                byDate.computeIfAbsent(p.getPartyDate(), k -> new java.util.ArrayList<>()).add(p);
            }
        }

        java.util.Set<java.time.LocalDate> usedDates = new java.util.HashSet<>();

        // Handle parties that already have a date.
        // If a date appears multiple times, keep one (oldest createdAt), shift the others to unique past dates.
        for (var entry : byDate.entrySet()) {
            var date = entry.getKey();
            var list = entry.getValue();
            list.sort(java.util.Comparator.comparing(com.packt.blurApp.model.Party::getCreatedAt));

            // keep first as canonical
            usedDates.add(date);

            for (int i = 1; i < list.size(); i++) {
                var p = list.get(i);
                java.time.LocalDate candidate = date.minusDays(i);
                while (usedDates.contains(candidate)) {
                    candidate = candidate.minusDays(1);
                }
                p.setPartyDate(candidate);
                usedDates.add(candidate);
            }
        }

        // Handle parties that had null date: assign based on createdAt date; if conflict, shift backward.
        nullDateParties.sort(java.util.Comparator.comparing(com.packt.blurApp.model.Party::getCreatedAt));
        for (var p : nullDateParties) {
            java.time.LocalDate candidate = p.getCreatedAt() != null
                    ? p.getCreatedAt().toLocalDate()
                    : java.time.LocalDate.now();

            while (usedDates.contains(candidate)) {
                candidate = candidate.minusDays(1);
            }

            p.setPartyDate(candidate);
            usedDates.add(candidate);
        }

        // Persist all changes. If any constraint existed, we already ensured uniqueness.
        partyRepository.saveAll(parties);
        log.info("Backfill parties completed");
    }

    private void backfillExistingRaces() {
        log.info("Backfilling existing races (status + attributionType)...");
        raceRepository.findAll().forEach(r -> {
            boolean changed = false;
            if (r.getStatus() == null) {
                r.setStatus(com.packt.blurApp.model.enums.RaceStatus.PENDING);
                changed = true;
            }
            if (r.getAttributionType() == null) {
                r.setAttributionType(com.packt.blurApp.model.enums.AttributionType.PER_USER);
                changed = true;
            }
            if (changed) raceRepository.save(r);
        });
        log.info("Backfill races completed");
    }

    private void createDefaultAdmin() {
        log.info("Checking for default admin user...");
        
        String defaultAdminUsername = "admin";
        
        if (!userRepository.existsByUserName(defaultAdminUsername)) {
            Role greatAdminRole = roleRepository.findByName(RoleNames.GREAT_ADMIN)
                    .orElseThrow(() -> new RuntimeException("GREAT_ADMIN role not found"));
            
            User admin = User.builder()
                    .userName(defaultAdminUsername)
                    .email("admin@blurapp.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(greatAdminRole)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            
            userRepository.save(admin);
            log.info("Default admin user created: username='{}', password='admin123'", defaultAdminUsername);
            log.warn("IMPORTANT: Please change the default admin password after first login!");
        } else {
            log.info("Default admin user already exists");
        }
    }

    private void initializeCars() {
        log.info("Initializing cars...");
        
        if (carRepository.count() > 0) {
            log.info("Cars already exist, skipping initialization");
            return;
        }

        // Car data from cars.csv
        String[][] carsData = {
            {"Opel Astra Extreme", "https://static.wikia.nocookie.net/blurgame/images/a/ab/Opel_Astra_Extreme.jpg/revision/latest?cb=20100912135528"},
            {"Ford FR-100", "https://static.wikia.nocookie.net/blurgame/images/d/d0/Cars_ford_fr_100_download.jpg/revision/latest/scale-to-width-down/1000?cb=20100316191358"},
            {"Audi R8 4.2 FSI quattro", "https://static.wikia.nocookie.net/blurgame/images/0/04/Audi_R8_4.2_FSI_Quattro.jpg/revision/latest?cb=20100912142022"},
            {"Corvette C3 (Drag)", "https://static.wikia.nocookie.net/blurgame/images/f/f0/Corvette_C3_%28Drag%29.jpg/revision/latest/scale-to-width-down/1000?cb=20120721051708"},
            {"Chevrolet Camaro SS (Drag)", "https://static.wikia.nocookie.net/blurgame/images/1/13/Chevrolet_Camaro_SS_%28Drag%29.jpg/revision/latest/scale-to-width-down/1000?cb=20120721021315"},
            {"Ford Mustang GT-R Concept", "https://static.wikia.nocookie.net/blurgame/images/5/54/Ford_Mustang_GT-R_Concept.jpg/revision/latest/scale-to-width-down/1000?cb=20120722081429"},
            {"Vauxhall Monaro VXR (Drift)", "https://static.wikia.nocookie.net/blurgame/images/0/0a/Vauxhall_Monaro_VXR_%28Drift%29.jpg/revision/latest/scale-to-width-down/1000?cb=20120723104448"},
            {"Dodge Viper GTSR", "https://static.wikia.nocookie.net/blurgame/images/a/ad/Dodge_Viper_GTSR.jpg/revision/latest/scale-to-width-down/1000?cb=20120722041003"},
            {"Renault Mégane Trophy", "https://static.wikia.nocookie.net/blurgame/images/9/9a/Ingame_renault_megane.JPG/revision/latest?cb=20100316184747"},
            {"Nissan Skyline GT-R NISMO Z-tune", "https://static.wikia.nocookie.net/blurgame/images/b/bb/Nissan_Skyline_GT-R_NISMO_Z-tune.jpg/revision/latest/scale-to-width-down/1000?cb=20120722145426"},
            {"Ford Transit Supervan3", "https://static.wikia.nocookie.net/blurgame/images/d/dd/60072_10150270979520302_880460301_14709902_514768_n.jpg/revision/latest?cb=20100912141412"},
            {"Corvette C3 (Race)", "https://static.wikia.nocookie.net/blurgame/images/c/c0/Corvette_C3_%28Race%29.jpg/revision/latest?cb=20120719160930"},
            {"Dodge Challenger SRT8 (Race)", "https://static.wikia.nocookie.net/blurgame/images/6/6a/Dodge_Challenger_%28Race%29.jpg/revision/latest/scale-to-width-down/1000?cb=20120722040406"},
            {"Dodge Viper Venom 1000", "https://static.wikia.nocookie.net/blurgame/images/0/0b/Dodge_Viper_Venom_1000.jpg/revision/latest/scale-to-width-down/1000?cb=20120722041202"},
            {"Koenigsegg CCX-R", "https://static.wikia.nocookie.net/blurgame/images/0/03/46831_10150270978980302_880460301_14709888_3561956_n.jpg/revision/latest?cb=20100912141201"}
        };

        for (String[] carData : carsData) {
            Car car = new Car();
            car.setName(carData[0]);
            car.setImageUrl(carData[1]);
            carRepository.save(car);
        }

        log.info("Initialized {} cars", carsData.length);
    }

    private void initializeCards() {
        log.info("Initializing cards (maps/tracks)...");
        
        if (cardRepository.count() > 0) {
            log.info("Cards already exist, skipping initialization");
            return;
        }

        // Card data from cards.csv (location, track, imageUrl)
        String[][] cardsData = {
            {"Amboy", "Badlands Traverse", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FAmboy-badlandsTraverse.png?alt=media&token=dc4d07cc-5e43-4e16-b1d0-dc32bed8aeaf"},
            {"Amboy", "Route 66", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FAmboy-Route66.png?alt=media&token=03c6e87a-9f21-4abf-8ba9-971c7fd565c5"},
            {"Barcelona Garcia", "Catalan Climb", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBarcelonaGracia-CatalanClimb.png?alt=media&token=4d7c1023-b209-40da-9e49-afce5565d1dc"},
            {"Barcelona Gracia", "El Carmel Heights", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBarcelonaGracia-ElCarmelHeights.png?alt=media&token=9973ad8f-d038-4b9f-b9f5-098d77dc225f"},
            {"Barcelona Garcia", "Passeig de Gracia", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBarcelonaGracia-PasseigdeGracia.png?alt=media&token=32d94552-e3d2-4554-948d-37f46f91a74f"},
            {"Barcelona Oval", "City Breach", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBarcelonaOval-CityBreach.png?alt=media&token=5152ab84-128f-44d6-b2c6-fede7ec35285"},
            {"Barcelona Oval", "Speedway", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBarcelonaOval-Speedway.png?alt=media&token=c09246cd-f733-48de-b6d3-831f4c478d9c"},
            {"Brighton", "Coastal Cruise", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBrighton-CoastalCruise.png?alt=media&token=29895549-9efe-4a72-a623-0ae175cb5ef7"},
            {"Brighton", "Promenade Loop", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBrighton-PromenadeLoop.png?alt=media&token=407b683d-5f47-4363-8971-8410e7dca36e"},
            {"Brighton", "Seafront Strip", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FBrighton-SeafrontStrip.png?alt=media&token=753c2258-847a-4cb8-8349-8da4229b45c8"},
            {"Hackney", "Central Sprint", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FHackney-CentralSprint.png?alt=media&token=d5c01796-7769-40c0-ba58-d8badd5466cf"},
            {"Hackney", "Shoreditch Sweep", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FHackney-ShoreditchSweep.png?alt=media&token=d90ad9c8-5455-4b68-a58f-6556f1ecbe95"},
            {"Hackney", "Urban Belt", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FHackney-UrbanBelt.png?alt=media&token=9125a8ca-9aba-46ed-932c-6ac025718006"},
            {"Hollywood Hills", "Downtown Vista", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FHollywoodHills-DowntownVista.png?alt=media&token=9339f6bc-435d-4542-87e7-890450098515"},
            {"Hollywood Hills", "Hollywood Rift", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FHollywoodHills-HollywoodRift.png?alt=media&token=2f9137cd-6b94-4efb-b5a6-9d082753b2f3"},
            {"LA Docks", "Cargo Run", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLADocks-CargoRun.png?alt=media&token=100e35e6-fec5-4cef-9e80-d2e15cd2febf"},
            {"LA Docks", "Pacific Reach", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLADocks-PacificReach.png?alt=media&token=312b19df-ebe4-458c-84aa-432a736eed64"},
            {"LA Downtown", "Harbor Freeway", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLADowntown-HarborFreeway.png?alt=media&token=3a9d7206-1c09-428e-aea9-f87268683227"},
            {"LA Downtown", "Highrise Ring", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLADowntown-HighriseRing.png?alt=media&token=f34e0090-bd7a-4d4e-9b1c-8e60d6da8ea4"},
            {"LA Downtown", "The Money Run", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLADowntown-TheMoneyRun.png?alt=media&token=b55229a2-d099-4165-b498-23ca01207f29"},
            {"LA River", "Concrete Basin", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLARiver-ConcreteBasin.png?alt=media&token=df96a971-0998-46ad-a7a1-e7eba983655a"},
            {"LA River", "Stormdrain Surge", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FLARiver-StormdrainSurge.png?alt=media&token=f97171cb-3291-40a2-b19c-b713b5c8ebb3"},
            {"Mount Haruna", "Descent", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FMountHaruna-Descent.png?alt=media&token=15c22c0d-6e57-4c8f-b218-7bba22ee600c"},
            {"NY Dumbo", "Brooklyn Tour", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FNYDumbo-BrooklynTour.png?alt=media&token=555c86ab-36ca-492d-ab45-9d78134aabdf"},
            {"NY Dumbo", "Manhattan View", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FNYDumbo-ManhattanView.png?alt=media&token=ee9cc357-2e1a-4864-a582-896897f9201f"},
            {"SanFran Russian Hill", "Russian Steppes", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FSanFranRussianHill-RussianSteppes.png?alt=media&token=9b3e2eac-f9b2-43be-98b2-86ba39743959"},
            {"SanFran Sausalito", "Bay Area Tour", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FSanFranSausalito-BayAreaTour.png?alt=media&token=b0fcc3e3-c248-4ad4-b223-838aba033603"},
            {"SanFran Sausalito", "Golden Gate Rush", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FSanFranSausalito-GoldenGateRush.png?alt=media&token=b52946f0-5ecf-48a8-9da9-f64d3322b496"},
            {"Tokyo Shutoko", "Bayshore Route", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FTokyoShutoko-BayshoreRoute.png?alt=media&token=cc473ca9-e8eb-44a3-b997-83788d510052"},
            {"Tokyo Shutoko", "Wangan-sen", "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2FTokyoShutoko-Wangan-sen.png?alt=media&token=14b27b90-4c35-486b-a94b-40c84b58407b"}
        };

        for (String[] cardData : cardsData) {
            Card card = new Card();
            card.setLocation(cardData[0]);
            card.setTrack(cardData[1]);
            card.setImageUrl(cardData[2]);
            cardRepository.save(card);
        }

        log.info("Initialized {} cards (maps/tracks)", cardsData.length);
    }

    private void initializeRaceParameters() {
        log.info("Initializing race parameters (bonuses/power-ups)...");
        
        if (raceParametersRepository.count() > 0) {
            log.info("Race parameters already exist, skipping initialization");
            return;
        }

        // Race parameters data from race_parameters.csv (name, isActive, downloadUrl)
        Object[][] paramsData = {
            {"Barge", false, "https://static.wikia.nocookie.net/blurgame/images/5/5b/Barge.png/revision/latest/scale-to-width-down/150?cb=20100510210429"},
            {"Bolt", true, "https://static.wikia.nocookie.net/blurgame/images/8/8e/Bolt.png/revision/latest/scale-to-width-down/150?cb=20100510210430"},
            {"Handicap", false, "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2Fhandicap.png?alt=media&token=dafc433f-8cf3-4024-83d7-0b3b0e1a46b0"},
            {"Mine", true, "https://static.wikia.nocookie.net/blurgame/images/a/a0/Mine.png/revision/latest/scale-to-width-down/150?cb=20100510210431"},
            {"Mods", false, "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2Fmods.png?alt=media&token=32fc2e5e-feb3-46da-bc51-62fa007c3d70"},
            {"Nitro", true, "https://static.wikia.nocookie.net/blurgame/images/e/ea/Nitro.png/revision/latest/scale-to-width-down/150?cb=20100510210432"},
            {"Repair", true, "https://static.wikia.nocookie.net/blurgame/images/e/e2/Repair.png/revision/latest/scale-to-width-down/150?cb=20100510210433"},
            {"Respawn", false, "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2Frespawn.png?alt=media&token=d8b36eef-ebe3-4f20-b176-719907a443be"},
            {"Shield", false, "https://static.wikia.nocookie.net/blurgame/images/c/cf/Shield.png/revision/latest/scale-to-width-down/150?cb=20100510210434"},
            {"Shock", false, "https://static.wikia.nocookie.net/blurgame/images/f/f9/Shock.png/revision/latest/scale-to-width-down/150?cb=20100510210434"},
            {"Shunt", true, "https://static.wikia.nocookie.net/blurgame/images/6/6c/Shunt.png/revision/latest/scale-to-width-down/150?cb=20100510210434"},
            {"Time Out", false, "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2Ftime_out.png?alt=media&token=cf7b6465-54e5-488a-9b83-fa1b88cb2bc1"},
            {"Upgrades", true, "https://firebasestorage.googleapis.com/v0/b/jtm-cuisine.appspot.com/o/BlurApp%20Icons%2Fupgrades.png?alt=media&token=11acb7c9-1c7f-4b13-b975-2fc3c81c434c"}
        };

        for (Object[] paramData : paramsData) {
            RaceParameters param = new RaceParameters();
            param.setName((String) paramData[0]);
            param.setIsActive((Boolean) paramData[1]);
            param.setDownloadUrl((String) paramData[2]);
            raceParametersRepository.save(param);
        }

        log.info("Initialized {} race parameters", paramsData.length);
    }
}
